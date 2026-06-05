package network.ermis.genstreamui.device

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import network.ermis.genstreamui.R
import network.ermis.genstreamui.databinding.ActivityDeviceBinding

class DeviceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceBinding
    private var initialFocusDone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        binding.btnBack.setOnClickListener {
            handleBack()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, DeviceListFragment())
                .commit()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Đặt focus ban đầu lên category đầu tiên của danh sách trái ngay khi cửa
        // sổ thực sự nhận focus, để lần nhấn D-pad đầu tiên có hiệu lực luôn.
        if (hasFocus && !initialFocusDone) {
            initialFocusDone = true
            val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            (fragment as? DeviceListFragment)?.focusFirstCategory()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BUTTON_B) {
            handleBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        handleBack()
    }

    private fun handleBack() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            binding.tvTitle.text = "Device"
        } else {
            super.onBackPressed()
        }
    }

    fun navigateToConnect(device: DeviceItem) {
        binding.tvTitle.text = device.name
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        val transaction = supportFragmentManager.beginTransaction()
        if (currentFragment != null) {
            transaction.hide(currentFragment)
        }
        transaction.add(R.id.fragmentContainer, DeviceConnectFragment.newInstance(device))
            .addToBackStack(null)
            .commit()
    }
}
