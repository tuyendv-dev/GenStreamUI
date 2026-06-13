package network.ermis.genstreamui.common

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Bus sự kiện phiên đăng nhập dùng chung toàn app.
 *
 * Khi refresh token thất bại ở BẤT KỲ đâu (xem [network.ermis.genstreamui.database.network.interceptor.TokenAuthenticator]),
 * gọi [notifySessionExpired] để phát sự kiện. [network.ermis.genstreamui.application.GlobalApp]
 * lắng nghe và điều hướng người dùng về màn Login.
 *
 * Là `object` (như SharedPrefCommon) để tầng network gọi được mà không cần inject.
 */
object SessionManager {

    // replay=0, có buffer để tryEmit từ background thread (OkHttp) không bị mất sự kiện.
    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    fun notifySessionExpired() {
        _sessionExpired.tryEmit(Unit)
    }
}
