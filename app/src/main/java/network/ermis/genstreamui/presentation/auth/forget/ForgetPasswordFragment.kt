package network.ermis.genstreamui.presentation.auth.forget

import android.widget.Toast
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import network.ermis.genstreamui.R
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.common.base.BaseFragment
import network.ermis.genstreamui.common.base.ext.collectWhenStarted
import network.ermis.genstreamui.databinding.FragmentForgetPasswordBinding
import network.ermis.genstreamui.presentation.auth.reset.ResetPasswordFragment

/**
 * Màn quên mật khẩu — nhập email để nhận mã. Thành công thì sang [ResetPasswordFragment].
 * Wire qua ForgetPasswordViewModel -> ForgetPasswordUseCase.
 */
@AndroidEntryPoint
class ForgetPasswordFragment :
    BaseFragment<FragmentForgetPasswordBinding>(FragmentForgetPasswordBinding::inflate) {

    private val viewModel: ForgetPasswordViewModel by viewModels()
    private var pendingEmail = ""

    override fun onClickViews() {
        binding.btnSend.setOnClickListener {
            val email = binding.etEmail.text?.toString().orEmpty().trim()
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Nhập email", Toast.LENGTH_SHORT).show()
            } else {
                pendingEmail = email
                viewModel.forgetPassword(email)
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
                    Toast.makeText(requireContext(), "Đã gửi mã đặt lại mật khẩu", Toast.LENGTH_SHORT).show()
                    goToReset()
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
        binding.btnSend.isEnabled = !loading
        binding.btnSend.alpha = if (loading) 0.5f else 1.0f
    }

    private fun goToReset() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ResetPasswordFragment.newInstance(pendingEmail))
            .addToBackStack(null)
            .commit()
    }
}
