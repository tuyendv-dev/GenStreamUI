package network.ermis.genstreamui.presentation.auth.register

import android.text.InputType
import android.widget.Toast
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import network.ermis.genstreamui.R
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.common.base.BaseFragment
import network.ermis.genstreamui.common.base.ext.collectWhenStarted
import network.ermis.genstreamui.databinding.FragmentSignUpBinding
import network.ermis.genstreamui.presentation.auth.verifi.VerificationFragment

/**
 * Màn đăng ký — wire end-to-end qua RegisterViewModel -> RegisterUseCase -> AuthRepository.
 */
@AndroidEntryPoint
class SignUpFragment : BaseFragment<FragmentSignUpBinding>(FragmentSignUpBinding::inflate) {

    private val viewModel: RegisterViewModel by viewModels()
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private var pendingEmail = ""

    override fun onClickViews() {
        binding.ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            applyPasswordVisibility(binding.etPassword, binding.ivTogglePassword, isPasswordVisible)
        }

        binding.ivToggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            applyPasswordVisibility(
                binding.etConfirmPassword,
                binding.ivToggleConfirmPassword,
                isConfirmPasswordVisible
            )
        }

        binding.btnSignUp.setOnClickListener {
            val email = binding.etEmail.text?.toString().orEmpty().trim()
            val name = binding.etName.text?.toString().orEmpty().trim()
            val password = binding.etPassword.text?.toString().orEmpty()
            val confirmPassword = binding.etConfirmPassword.text?.toString().orEmpty()

            when {
                email.isEmpty() -> toast("Nhập email")
                name.isEmpty() -> toast("Nhập tên hiển thị")
                password.isEmpty() -> toast("Nhập mật khẩu")
                password != confirmPassword -> toast("Mật khẩu xác nhận không khớp")
                else -> {
                    pendingEmail = email
                    viewModel.register(email, name, password)
                }
            }
        }
    }

    private fun applyPasswordVisibility(
        editText: android.widget.EditText,
        toggle: android.widget.ImageView,
        visible: Boolean
    ) {
        editText.inputType = if (visible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        toggle.alpha = if (visible) 1.0f else 0.5f
        editText.setSelection(editText.text.length)
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun observerData() {
        collectWhenStarted(viewModel.events) { ui ->
            when (ui) {
                UiState.Idle -> Unit
                UiState.Loading -> setLoading(true)
                is UiState.Success -> {
                    setLoading(false)
                    val message = ui.data.message ?: "Đăng ký thành công, vui lòng kiểm tra email"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    val userId = ui.data.data?.userId ?: 0
                    val verifyEmail = ui.data.data?.email ?: pendingEmail
                    goToVerification(userId, verifyEmail)
                }
                is UiState.Error -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), ui.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        if (loading) showLoading() else hideLoading()
        binding.btnSignUp.isEnabled = !loading
        binding.btnSignUp.alpha = if (loading) 0.5f else 1.0f
    }

    private fun goToVerification(userId: Int, email: String) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, VerificationFragment.newInstance(userId, email))
            .addToBackStack(null)
            .commit()
    }
}
