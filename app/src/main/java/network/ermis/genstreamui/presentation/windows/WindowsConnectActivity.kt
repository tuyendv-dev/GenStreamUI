package network.ermis.genstreamui.presentation.windows

import android.os.Bundle
import android.view.View
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
import network.ermis.genstreamui.databinding.ActivityWindowsConnectBinding

/**
 * Màn kết nối tới máy tính (PC Emulator / Windows). Toàn bộ logic connect tới VM nằm ở đây qua
 * [WindowsConnectViewModel] (KHÔNG còn ở PlayGameActivity). Mở từ MineFragment khi tile Windows
 * được chọn và bấm "Import PC games".
 */
@AndroidEntryPoint
class WindowsConnectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWindowsConnectBinding
    private val viewModel: WindowsConnectViewModel by viewModels()

    private val subscriptionId: Int by lazy {
        intent.getIntExtra(EXTRA_SUBSCRIPTION_ID, DEFAULT_SUBSCRIPTION_ID)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWindowsConnectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Immersive landscape (giống PlayGameActivity).
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())

        binding.btnBack.setOnClickListener { finish() }
        binding.btnRetry.setOnClickListener { viewModel.connect(subscriptionId) }

        observeStage()
        viewModel.connect(subscriptionId)
    }

    private fun observeStage() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stage.collect { render(it) }
            }
        }
    }

    private fun render(stage: ConnectStage) {
        when (stage) {
            ConnectStage.Idle,
            ConnectStage.CreatingSession -> showProgress("Đang tạo phiên...", "")

            is ConnectStage.WaitingForVm ->
                showProgress("Đang khởi tạo máy ảo...", "Vui lòng đợi (lần ${stage.attempt})")

            ConnectStage.Authorizing ->
                showProgress("Đang xác thực thiết bị...", "")

            is ConnectStage.Connected ->
                showResult(
                    title = "Đã kết nối máy tính",
                    detail = "${stage.deviceName} • ${stage.host}:${stage.basePort}",
                    showRetry = false
                )
            // TODO: vào stream Moonlight (Stage 3b serverinfo/stream) khi engine native sẵn sàng.

            is ConnectStage.Failed ->
                showResult(title = "Kết nối thất bại", detail = stage.message, showRetry = true)
        }
    }

    private fun showProgress(title: String, detail: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRetry.visibility = View.GONE
        binding.tvStatus.text = title
        binding.tvDetail.text = detail
    }

    private fun showResult(title: String, detail: String, showRetry: Boolean) {
        binding.progressBar.visibility = View.GONE
        binding.btnRetry.visibility = if (showRetry) View.VISIBLE else View.GONE
        binding.tvStatus.text = title
        binding.tvDetail.text = detail
    }

    companion object {
        /** Subscription dùng để mở phiên (POST /sessions). */
        const val EXTRA_SUBSCRIPTION_ID = "extra_subscription_id"

        // TODO: thay bằng subscription active thật từ GET /users/me/subscriptions (§5).
        private const val DEFAULT_SUBSCRIPTION_ID = 1
    }
}
