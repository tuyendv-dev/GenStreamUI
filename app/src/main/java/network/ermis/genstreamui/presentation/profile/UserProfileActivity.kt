package network.ermis.genstreamui.presentation.profile

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import network.ermis.genstreamui.R
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.common.base.ext.collectWhenStarted
import network.ermis.genstreamui.database.cache.SharedPrefCommon
import network.ermis.genstreamui.database.cache.cachedUser
import network.ermis.genstreamui.database.cache.saveUser
import network.ermis.genstreamui.databinding.ActivityUserProfileBinding
import network.ermis.genstreamui.databinding.DialogBindEmailBinding
import network.ermis.genstreamui.databinding.DialogBindPhoneBinding
import network.ermis.genstreamui.domain.model.User
import network.ermis.genstreamui.presentation.addScaleClickEffect
import network.ermis.genstreamui.presentation.auth.LoginActivity

@AndroidEntryPoint
class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding

    private val viewModel: UserProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnBindPhone.root.setOnClickListener {
            showBindPhoneDialog()
        }

        binding.btnBindEmail.root.setOnClickListener {
            showBindEmailDialog()
        }

        binding.btnBindPhone.tvSettingTitle.text = "Bind Phone Number"
        binding.btnBindEmail.tvSettingTitle.text = "Bind Email"

        binding.btnLogout.addScaleClickEffect()
        binding.btnLogout.setOnClickListener {
            // Handle logout
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.btnDeleteAccount.addScaleClickEffect()
        binding.btnDeleteAccount.setOnClickListener {
            // Handle delete account
        }

        // Hiển thị ngay thông tin đã cache (lúc đăng nhập hoặc lần getInfo trước),
        // rồi gọi API làm mới ở nền — tránh màn trống trong lúc chờ mạng.
        renderCachedUser()
        observeUserInfo()
        viewModel.getUserInfo()
    }

    private fun observeUserInfo() {
        collectWhenStarted(viewModel.userInfoEvents) { ui ->
            when (ui) {
                UiState.Idle, UiState.Loading -> Unit
                is UiState.Success -> {
                    SharedPrefCommon.saveUser(ui.data)
                    bindUser(ui.data)
                }
                is UiState.Error -> Unit // giữ nguyên thông tin cache đang hiển thị
            }
        }
    }

    private fun renderCachedUser() {
        bindUser(SharedPrefCommon.cachedUser())
    }

    private fun bindUser(user: User) {
        if (user.displayName.isNotEmpty()) binding.tvName.text = user.displayName
        if (user.id != 0) binding.tvUid.text = getString(R.string.user_uid, user.id)
        if (user.avatarUrl.isNotEmpty()) {
            Glide.with(this)
                .load(user.avatarUrl)
                .placeholder(R.drawable.ic_avatar_default)
                .error(R.drawable.ic_avatar_default)
                .circleCrop()
                .into(binding.ivAvatar)
        }
    }

    private fun showBindEmailDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogBindEmailBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSend.addScaleClickEffect()
        dialogBinding.btnSend.setOnClickListener {
            // Handle send action
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showBindPhoneDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogBindPhoneBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSend.addScaleClickEffect()
        dialogBinding.btnSend.setOnClickListener {
            // Handle send action
            dialog.dismiss()
        }

        dialog.show()
    }
}