package network.ermis.genstreamui.setting

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import network.ermis.genstreamui.R
import network.ermis.genstreamui.databinding.ActivityUserProfileBinding
import network.ermis.genstreamui.databinding.DialogBindEmailBinding
import network.ermis.genstreamui.databinding.DialogBindPhoneBinding
import network.ermis.genstreamui.addScaleClickEffect

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding

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
            finish()
        }

        binding.btnDeleteAccount.addScaleClickEffect()
        binding.btnDeleteAccount.setOnClickListener {
            // Handle delete account
        }
    }

    private fun showBindEmailDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogBindEmailBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
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
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
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
