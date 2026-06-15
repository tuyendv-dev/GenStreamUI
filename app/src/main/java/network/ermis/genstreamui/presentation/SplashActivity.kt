package network.ermis.genstreamui.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import network.ermis.genstreamui.databinding.ActivitySplashBinding
import network.ermis.genstreamui.presentation.auth.LoginActivity

/**
 * Màn khởi động: kiểm tra phiên đăng nhập rồi điều hướng (xem [SplashViewModel]).
 * Có token hợp lệ (hoặc refresh được) -> [MainActivity]; ngược lại -> [LoginActivity].
 */
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

//        observeDestination()
//        viewModel.checkSession()
        navigateTo(MainActivity::class.java)
    }

    private fun observeDestination() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.destination.collect { dest ->
                    when (dest) {
                        SplashViewModel.Destination.Main -> navigateTo(MainActivity::class.java)
                        SplashViewModel.Destination.Login -> navigateTo(LoginActivity::class.java)
                    }
                }
            }
        }
    }

    private fun navigateTo(target: Class<*>) {
        startActivity(
            Intent(this, target).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }
}
