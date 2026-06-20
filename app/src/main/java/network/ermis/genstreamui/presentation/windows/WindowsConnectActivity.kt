package network.ermis.genstreamui.presentation.windows

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
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
import com.limelight.AppView
import com.limelight.computers.ComputerManagerService
import com.limelight.nvstream.http.ComputerDetails
import com.limelight.nvstream.http.NvApp
import com.limelight.utils.ServerHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import network.ermis.genstreamui.database.security.PinnedServerCertStore
import network.ermis.genstreamui.databinding.ActivityWindowsConnectBinding
import javax.inject.Inject

/**
 * Màn kết nối tới máy tính (PC Emulator / Windows). Toàn bộ logic connect tới VM nằm ở đây qua
 * [WindowsConnectViewModel]. Sau khi token-auth xong (ConnectStage.Connected), đăng ký VM thành
 * "token host" trong [ComputerManagerService] (để chắc serverinfo đã lên + lấy đúng địa chỉ/cert),
 * rồi vào THẲNG màn điều khiển Desktop của VM (mở [com.limelight.Game] stream app "Desktop").
 */
@AndroidEntryPoint
class WindowsConnectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWindowsConnectBinding
    private val viewModel: WindowsConnectViewModel by viewModels()

    @Inject lateinit var pinnedStore: PinnedServerCertStore

    // Play-Now (tuỳ chọn): nếu appId > 0 thì sau khi kết nối sẽ mở sẵn game qua agent.
    private val platform: String by lazy { intent.getStringExtra(EXTRA_PLATFORM).orEmpty() }
    private val appId: Int by lazy { intent.getIntExtra(EXTRA_APP_ID, 0) }

    // true (mặc định, Import PC games) → mở lưới AppView; false (Play Now) → stream thẳng vào game.
    private val openAppList: Boolean by lazy { intent.getBooleanExtra(EXTRA_OPEN_APP_LIST, true) }

    // Binder của ComputerManagerService (Moonlight) — hoàn tất khi service connected.
    private val managerBinderDeferred = CompletableDeferred<ComputerManagerService.ComputerManagerBinder>()
    private var serviceBound = false
    private var streamLaunched = false

    // Sau khi đã mở stream/AppView, lần quay lại màn này sẽ tự đóng luôn (kết thúc phiên).
    private var finishOnReturn = false

    // Popup chọn gói (khi user có nhiều hơn 1 subscription) — non-cancelable, giữ ref để tránh mở trùng.
    private var subscriptionDialog: androidx.appcompat.app.AlertDialog? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val b = binder as ComputerManagerService.ComputerManagerBinder
            if (!managerBinderDeferred.isCompleted) managerBinderDeferred.complete(b)
        }

        override fun onServiceDisconnected(name: ComponentName?) = Unit
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

        // Bind engine Moonlight để đăng ký host khi kết nối xong.
        serviceBound = bindService(
            Intent(this, ComputerManagerService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )

        binding.btnBack.setOnClickListener { finish() }
        binding.btnRetry.setOnClickListener { viewModel.retry() }

        observeStage()
        // Subscription được xác định từ GET /users/me/subscriptions (auto nếu 1 gói, popup nếu nhiều).
        viewModel.start(platform, appId)
    }

    private fun observeStage() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stage.collect { render(it) }
            }
        }
    }

    private fun render(stage: ConnectStage) {
        // Đóng popup chọn gói nếu rời khỏi stage chọn (vd retry/đã chọn xong).
        if (stage !is ConnectStage.ChoosingSubscription) dismissSubscriptionPicker()

        when (stage) {
            ConnectStage.Idle,
            ConnectStage.ResolvingSubscription -> showProgress("Đang tải thông tin gói...", "")

            is ConnectStage.ChoosingSubscription -> showSubscriptionPicker(stage.options)

            ConnectStage.CreatingSession -> showProgress("Đang tạo phiên...", "")

            is ConnectStage.WaitingForVm ->
                showProgress("Đang khởi tạo máy ảo...", "Vui lòng đợi (lần ${stage.attempt})")

            ConnectStage.Authorizing ->
                showProgress("Đang xác thực thiết bị...", "")

            is ConnectStage.Connected -> streamDesktopOnce(stage)

            is ConnectStage.Failed ->
                showResult(title = "Kết nối thất bại", detail = stage.message, showRetry = stage.canRetry)
        }
    }

    /** Đăng ký VM (chờ host sẵn sàng) rồi vào thẳng màn điều khiển Desktop. Chỉ chạy một lần. */
    private fun streamDesktopOnce(connected: ConnectStage.Connected) {
        if (streamLaunched) return
        val serverCert = pinnedStore.get(connected.host)
        if (serverCert == null) {
            showResult("Chưa sẵn sàng", "Thiếu chứng chỉ host (token-auth chưa xong)", showRetry = true)
            return
        }
        streamLaunched = true

        lifecycleScope.launch {
            val binder = managerBinderDeferred.await()

            // Warm-up: Sunshine HTTP/HTTPS của VM (serverinfo base+0 / base−5) có thể chưa lên ngay
            // sau token-auth → thử đăng ký host nhiều lần đến khi ONLINE.
            var registered: ComputerDetails? = null
            val title = if (openAppList) "Đang tải danh sách ứng dụng..." else "Đang mở màn hình máy tính..."
            for (attempt in 1..MAX_REGISTER_ATTEMPTS) {
                showProgress(title, "Đang chờ host sẵn sàng ($attempt)")
                val details = ComputerDetails().apply {
                    manualAddress = ComputerDetails.AddressTuple(connected.host, connected.basePort)
                    this.serverCert = serverCert
                    derivePortsFromBase = true
                    name = PC_NAME
                }
                val ok = withContext(Dispatchers.IO) {
                    try {
                        binder.addComputerBlocking(details)
                    } catch (e: InterruptedException) {
                        false
                    }
                }
                if (ok && details.uuid != null) {
                    registered = details
                    break
                }
                delay(REGISTER_RETRY_DELAY_MS)
            }

            if (registered != null) {
                // HTTPS qua relay = base − 5 (chắc chắn dùng đúng cổng, bỏ dò HTTP base+0).
                registered.httpsPort = connected.basePort - 5
                // Mở stream/AppView → khi back về màn này sẽ tự đóng (xem onResume).
                finishOnReturn = true
                if (openAppList) {
                    // Import PC games → mở lưới app (AppView) để chọn (Desktop/Steam...).
                    startActivity(
                        Intent(this@WindowsConnectActivity, AppView::class.java).apply {
                            putExtra(AppView.NAME_EXTRA, registered.name)
                            putExtra(AppView.UUID_EXTRA, registered.uuid)
                        }
                    )
                } else {
                    // Play Now → stream thẳng vào game (NvApp name-only → host getAppByName resolve).
                    // createStartIntent tự set EXTRA_SERVER_PORT_BASE vì derivePortsFromBase = true.
                    startActivity(
                        ServerHelper.createStartIntent(
                            this@WindowsConnectActivity, NvApp(DESKTOP_APP), registered, binder
                        )
                    )
                }
            } else {
                streamLaunched = false
                showResult(
                    "Không mở được màn hình máy tính",
                    "Host chưa phản hồi serverinfo (base+0/base−5)",
                    showRetry = true
                )
            }
        }
    }

    /**
     * Popup không thể tắt yêu cầu user chọn 1 trong các gói. Mỗi option hiển thị
     * package_id + cpu_model + thời gian còn lại. Bấm "Xác nhận" → tiếp tục luồng connect.
     */
    private fun showSubscriptionPicker(options: List<network.ermis.genstreamui.domain.model.Subscription>) {
        if (subscriptionDialog?.isShowing == true || options.isEmpty()) return

        val labels = options.map { sub ->
            "Gói #${sub.packageId} • ${sub.cpuModel.ifEmpty { "?" }} • Còn ${formatHours(sub.hoursRemaining)} giờ"
        }

        val dialogBinding = network.ermis.genstreamui.databinding.DialogSubscriptionBinding.inflate(layoutInflater)
        val adapter = SubscriptionAdapter(labels, 0)
        dialogBinding.rvSubscriptions.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        dialogBinding.rvSubscriptions.adapter = adapter

        subscriptionDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
            .apply {
                window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
                setCanceledOnTouchOutside(false)
                setOnKeyListener { _, keyCode, _ -> keyCode == android.view.KeyEvent.KEYCODE_BACK }
            }

        dialogBinding.btnConfirm.setOnClickListener {
            viewModel.onSubscriptionChosen(options[adapter.getSelectedIndex()].id)
        }

        // Nút close: đóng dialog và thoát luôn màn hiện tại.
        dialogBinding.btnClose.setOnClickListener {
            dismissSubscriptionPicker()
            finish()
        }

        subscriptionDialog?.show()
    }

    private fun dismissSubscriptionPicker() {
        subscriptionDialog?.dismiss()
        subscriptionDialog = null
    }

    /** Bỏ phần thập phân thừa: 60.0 -> "60", 12.5 -> "12.5". */
    private fun formatHours(hours: Double): String =
        if (hours % 1.0 == 0.0) hours.toLong().toString() else hours.toString()

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

    override fun onResume() {
        super.onResume()
        // Đã mở stream/AppView rồi quay lại đây → đóng màn luôn (ViewModel.onCleared sẽ end phiên).
        if (finishOnReturn) finish()
    }

    override fun onDestroy() {
        dismissSubscriptionPicker()
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
        super.onDestroy()
    }

    companion object {
        /** (Play-Now) platform của game cần mở qua agent, vd "steam". */
        const val EXTRA_PLATFORM = "extra_platform"

        /** (Play-Now) appid của game trên host; <= 0 nghĩa là chỉ stream Desktop. */
        const val EXTRA_APP_ID = "extra_app_id"

        /** true (Import PC games) → mở lưới AppView; false (Play Now) → stream thẳng vào game. */
        const val EXTRA_OPEN_APP_LIST = "extra_open_app_list"

        private const val PC_NAME = "GenStream VM"
        private const val DESKTOP_APP = "Desktop"

        // Warm-up đăng ký host: ~20 lần × 3s = 60s (chờ Sunshine của VM lên).
        private const val MAX_REGISTER_ATTEMPTS = 20
        private const val REGISTER_RETRY_DELAY_MS = 3_000L
    }
}
