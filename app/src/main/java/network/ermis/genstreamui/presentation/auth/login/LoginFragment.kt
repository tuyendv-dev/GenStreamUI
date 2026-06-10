package network.ermis.genstreamui.presentation.auth.login

import android.content.Intent
import android.text.InputType
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import network.ermis.genstreamui.R
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.common.base.BaseFragment
import network.ermis.genstreamui.common.base.ext.collectWhenStarted
import network.ermis.genstreamui.database.cache.SharedPrefCommon
import network.ermis.genstreamui.databinding.FragmentLoginBinding
import network.ermis.genstreamui.presentation.MainActivity
import network.ermis.genstreamui.presentation.auth.forget.ForgetPasswordFragment

/**
 * Màn Login — wire 3 hành động qua LoginViewModel:
 *  - Đăng nhập email/mật khẩu (LoginUseCase)
 *  - Đăng nhập Google (LoginByGoogleUseCase lấy token -> LoginWithGoogleUseCase đổi phiên) qua btnGoogle
 *  - tvForgotPassword điều hướng sang ForgetPasswordFragment
 */
@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate) {

    private val viewModel: LoginViewModel by viewModels()
    private var isPasswordVisible = false

    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(ApiException::class.java)
            // Backend dùng OAuth Authorization Code + PKCE: code = authorization code của Google.
            // TODO: code_verifier + redirect_uri cần lấy từ luồng OAuth PKCE đầy đủ (Custom Tabs/AppAuth).
            viewModel.loginWithGoogle(
                code = account.serverAuthCode.orEmpty(),
                codeVerifier = "",
                redirectUri = ""
            )
        } catch (e: ApiException) {
            Toast.makeText(requireContext(), "Đăng nhập Google bị huỷ", Toast.LENGTH_SHORT).show()
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

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString().orEmpty().trim()
            val password = binding.etPassword.text?.toString().orEmpty()
            viewModel.login(email, password)
        }

        binding.tvForgotPassword.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ForgetPasswordFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnGoogle.setOnClickListener {
            googleLauncher.launch(viewModel.googleSignInIntent(requireContext()))
        }
    }

    override fun observerData() {
        // Đăng nhập email/mật khẩu
        collectWhenStarted(viewModel.events) { ui ->
            when (ui) {
                UiState.Idle -> Unit
                UiState.Loading -> setLoading(true)
                is UiState.Success -> {
                    setLoading(false)
                    saveSession(ui.data.accessToken, ui.data.user?.displayName)
                    goToHome()
                }
                is UiState.Error -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), ui.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Đăng nhập Google
        collectWhenStarted(viewModel.googleEvents) { ui ->
            when (ui) {
                UiState.Idle -> Unit
                UiState.Loading -> showLoading()
                is UiState.Success -> {
                    hideLoading()
                    saveSession(ui.data.data?.accessToken, ui.data.data?.user?.displayName)
                    goToHome()
                }
                is UiState.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), ui.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveSession(accessToken: String?, displayName: String?) {
        accessToken?.let { SharedPrefCommon.accessToken = it }
        displayName?.let { SharedPrefCommon.userName = it }
    }

    private fun setLoading(loading: Boolean) {
        if (loading) showLoading() else hideLoading()
        binding.btnLogin.isEnabled = !loading
        binding.btnLogin.alpha = if (loading) 0.5f else 1.0f
    }

    private fun goToHome() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}
