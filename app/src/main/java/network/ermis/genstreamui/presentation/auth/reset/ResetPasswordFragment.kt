package network.ermis.genstreamui.presentation.auth.reset

import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import network.ermis.genstreamui.R
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.common.base.BaseFragment
import network.ermis.genstreamui.common.base.ext.collectWhenStarted
import network.ermis.genstreamui.databinding.FragmentResetPasswordBinding
import network.ermis.genstreamui.presentation.auth.login.LoginFragment

/**
 * Màn đặt lại mật khẩu — nhập mã + mật khẩu mới. Thành công thì quay lại [LoginFragment].
 * Wire qua ResetPasswordViewModel -> ResetPasswordUseCase. Nhận email qua [newInstance].
 */
@AndroidEntryPoint
class ResetPasswordFragment :
    BaseFragment<FragmentResetPasswordBinding>(FragmentResetPasswordBinding::inflate) {

    private val viewModel: ResetPasswordViewModel by viewModels()
    private var isPasswordVisible = false

    private val email: String by lazy { arguments?.getString(ARG_EMAIL).orEmpty() }

    override fun initViews() {
        if (email.isNotEmpty()) {
            binding.tvSubtitle.text = getString(R.string.reset_password_subtitle, email)
        }
    }

    override fun onClickViews() {
        binding.ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                binding.etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.ivTogglePassword.alpha = 1.0f
            } else {
                binding.etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.ivTogglePassword.alpha = 0.5f
            }
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }

        binding.btnReset.setOnClickListener {
            val code = binding.etCode.text?.toString().orEmpty().trim()
            val password = binding.etPassword.text?.toString().orEmpty()
            when {
                code.isEmpty() -> Toast.makeText(requireContext(), "Nhập mã xác minh", Toast.LENGTH_SHORT).show()
                password.isEmpty() -> Toast.makeText(requireContext(), "Nhập mật khẩu mới", Toast.LENGTH_SHORT).show()
                else -> viewModel.resetPassword(email, code, password)
            }
        }
    }

    override fun observerData() {
        collectWhenStarted(viewModel.events) { ui ->
            when (ui) {
                UiState.Idle -> Unit
                UiState.Loading -> setLoading(true)
                is UiState.Success -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Đặt lại mật khẩu thành công", Toast.LENGTH_SHORT).show()
                    goToLogin()
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
        binding.btnReset.isEnabled = !loading
        binding.btnReset.alpha = if (loading) 0.5f else 1.0f
    }

    private fun goToLogin() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LoginFragment())
            .commit()
    }

    companion object {
        private const val ARG_EMAIL = "arg_email"

        fun newInstance(email: String) = ResetPasswordFragment().apply {
            arguments = Bundle().apply { putString(ARG_EMAIL, email) }
        }
    }
}
