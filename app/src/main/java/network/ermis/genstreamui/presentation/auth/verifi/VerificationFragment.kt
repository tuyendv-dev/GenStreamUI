package network.ermis.genstreamui.presentation.auth.verifi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import network.ermis.genstreamui.R
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.common.base.BaseFragment
import network.ermis.genstreamui.common.base.ext.collectWhenStarted
import network.ermis.genstreamui.database.cache.SharedPrefCommon
import network.ermis.genstreamui.database.cache.saveUser
import network.ermis.genstreamui.domain.model.mapper.toDomain
import network.ermis.genstreamui.databinding.FragmentVerificationBinding
import network.ermis.genstreamui.presentation.MainActivity

/**
 * Màn nhập mã xác minh email — wire qua VerificationViewModel -> VerificationCodeUseCase.
 * Nhận user_id (để gọi /auth/verify-email) và email (để hiển thị) qua [newInstance].
 * Verify thành công trả về phiên đăng nhập (token) -> lưu token và vào Home.
 */
@AndroidEntryPoint
class VerificationFragment :
    BaseFragment<FragmentVerificationBinding>(FragmentVerificationBinding::inflate) {

    private val viewModel: VerificationViewModel by viewModels()

    private val userId: Int by lazy { arguments?.getInt(ARG_USER_ID) ?: 0 }
    private val email: String by lazy { arguments?.getString(ARG_EMAIL).orEmpty() }

    override fun initViews() {
        if (email.isNotEmpty()) {
            binding.tvSubtitle.text = getString(R.string.verification_subtitle, email)
        }
    }

    override fun onClickViews() {
        binding.btnVerify.setOnClickListener {
            val otp = binding.etCode.text?.toString().orEmpty().trim()
            if (otp.isEmpty()) {
                Toast.makeText(requireContext(), "Nhập mã xác minh", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.verify(userId, otp)
            }
        }

        binding.tvResend.setOnClickListener {
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Thiếu email để gửi lại mã", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.resendOtp(email)
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
                    ui.data.data?.accessToken?.let { SharedPrefCommon.accessToken = it }
                    SharedPrefCommon.saveUser(ui.data.data?.user?.toDomain())
                    val message = ui.data.message ?: "Xác minh thành công"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    goToHome()
                }
                is UiState.Error -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), ui.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Gửi lại mã
        collectWhenStarted(viewModel.resendEvents) { ui ->
            when (ui) {
                UiState.Idle -> Unit
                UiState.Loading -> {
                    showLoading()
                    binding.tvResend.isEnabled = false
                }
                is UiState.Success -> {
                    hideLoading()
                    binding.tvResend.isEnabled = true
                    val message = ui.data.data?.message ?: ui.data.message ?: "Đã gửi lại mã"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
                is UiState.Error -> {
                    hideLoading()
                    binding.tvResend.isEnabled = true
                    Toast.makeText(requireContext(), ui.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        if (loading) showLoading() else hideLoading()
        binding.btnVerify.isEnabled = !loading
        binding.btnVerify.alpha = if (loading) 0.5f else 1.0f
    }

    private fun goToHome() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    companion object {
        private const val ARG_USER_ID = "arg_user_id"
        private const val ARG_EMAIL = "arg_email"

        fun newInstance(userId: Int, email: String) = VerificationFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_USER_ID, userId)
                putString(ARG_EMAIL, email)
            }
        }
    }
}
