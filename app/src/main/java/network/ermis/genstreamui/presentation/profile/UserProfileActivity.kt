package network.ermis.genstreamui.presentation.profile

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
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
import network.ermis.genstreamui.database.cache.clearSession
import network.ermis.genstreamui.database.cache.saveUser
import network.ermis.genstreamui.databinding.ActivityUserProfileBinding
import network.ermis.genstreamui.databinding.DialogBindEmailBinding
import network.ermis.genstreamui.databinding.DialogBindPhoneBinding
import network.ermis.genstreamui.databinding.DialogUpdateNameBinding
import network.ermis.genstreamui.domain.model.User
import network.ermis.genstreamui.domain.model.dto.req.ReqUpdateUserInfo
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

        binding.btnBindPhone.tvSettingTitle.text = "Bind Phone Number"
        // Cụm Email do bindEmailRow() đảm nhiệm (theo email đã/chưa có)

        binding.tvName.setOnClickListener {
            showUpdateNameDialog()
        }

        binding.btnLogout.addScaleClickEffect()
        binding.btnLogout.setOnClickListener {
            // Xoá phiên (token + user cache) trước khi về màn đăng nhập
            SharedPrefCommon.clearSession()
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
        observeUpdateUser()
        viewModel.getUserInfo()
    }

    private fun observeUpdateUser() {
        collectWhenStarted(viewModel.updateEvents) { ui ->
            when (ui) {
                UiState.Idle, UiState.Loading -> Unit
                is UiState.Success -> {
                    SharedPrefCommon.saveUser(ui.data)
                    bindUser(ui.data)
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                }
                is UiState.Error ->
                    Toast.makeText(this, ui.message, Toast.LENGTH_SHORT).show()
            }
        }
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
        bindEmailRow(user.email)
    }

    /**
     * Chưa có email -> hiển thị "Bind Email", cho bấm để mở dialog.
     * Đã có email -> hiển thị email thay thế, ẩn mũi tên và vô hiệu hoá cụm (không bấm được).
     */
    private fun bindEmailRow(email: String) {
        val row = binding.btnBindEmail
        if (email.isEmpty()) {
            row.tvSettingTitle.text = "Bind Email"
            row.ivArrow.visibility = View.VISIBLE
            row.root.isEnabled = true
            row.root.isClickable = true
            row.root.alpha = 1f
            row.root.setOnClickListener { showBindEmailDialog() }
        } else {
            row.tvSettingTitle.text = "Email: $email"
            row.ivArrow.visibility = View.GONE
            row.root.isEnabled = false
            row.root.isClickable = false
            row.root.alpha = 0.5f
            row.root.setOnClickListener(null)
        }
    }

    private fun showUpdateNameDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogUpdateNameBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.etName.setText(SharedPrefCommon.userName)
        dialogBinding.etName.setSelection(dialogBinding.etName.text?.length ?: 0)

        dialogBinding.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.addScaleClickEffect()
        dialogBinding.btnSave.setOnClickListener {
            val newName = dialogBinding.etName.text?.toString().orEmpty().trim()
            if (newName.isEmpty()) {
                Toast.makeText(this, "Nhập tên hiển thị", Toast.LENGTH_SHORT).show()
            } else {
                // Giữ nguyên avatar hiện tại (null -> Gson bỏ qua field, backend không xoá avatar)
                val avatar = SharedPrefCommon.userAvatarUrl.takeIf { it.isNotEmpty() }
                viewModel.updateUserInfo(ReqUpdateUserInfo(displayName = newName, avatarUrl = avatar))
                dialog.dismiss()
            }
        }

        dialog.show()
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