package network.ermis.genstreamui.application

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import network.ermis.genstreamui.common.SessionManager
import network.ermis.genstreamui.database.cache.SharedPrefCommon
import network.ermis.genstreamui.database.cache.clearSession
import network.ermis.genstreamui.presentation.SplashActivity
import network.ermis.genstreamui.presentation.auth.LoginActivity

/**
 * Application gốc — entry point của Hilt và nơi khởi tạo SharedPrefCommon.
 * Port từ GenPlayAndroid (application/GlobalApp.kt), bỏ Koin/Firebase do chưa cần.
 *
 * Đồng thời lắng nghe [SessionManager.sessionExpired]: khi refresh token thất bại ở bất kỳ đâu,
 * điều hướng người dùng về [LoginActivity] (xoá toàn bộ task cũ).
 */
@HiltAndroidApp
class GlobalApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // Activity đang hiển thị, để mở Login từ đúng context và tránh điều hướng trùng.
    private var currentActivity: Activity? = null

    override fun onCreate() {
        super.onCreate()
        SharedPrefCommon.init(this)
        registerActivityTracker()
        observeSessionExpired()
    }

    private fun observeSessionExpired() {
        appScope.launch {
            SessionManager.sessionExpired.collect {
                navigateToLogin()
            }
        }
    }

    private fun navigateToLogin() {
        // Đảm bảo phiên đã sạch (Authenticator chỉ xoá token, dọn nốt user cache).
        SharedPrefCommon.clearSession()

        val activity = currentActivity
        // Splash tự xử lý điều hướng; Login đang mở rồi thì không mở lại.
        if (activity is LoginActivity || activity is SplashActivity) return

        val context = activity ?: this
        context.startActivity(
            Intent(context, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
    }

    private fun registerActivityTracker() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                currentActivity = activity
            }

            override fun onActivityPaused(activity: Activity) {
                if (currentActivity === activity) currentActivity = null
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        })
    }
}
