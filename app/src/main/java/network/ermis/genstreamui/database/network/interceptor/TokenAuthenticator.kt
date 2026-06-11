package network.ermis.genstreamui.database.network.interceptor

import android.util.Log
import kotlinx.coroutines.runBlocking
import network.ermis.genstreamui.database.cache.SharedPrefCommon
import network.ermis.genstreamui.database.cache.saveUser
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.dto.req.ReqRefreshToken
import network.ermis.genstreamui.domain.model.mapper.toDomain
import network.ermis.genstreamui.domain.repository.AuthRepository
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tự động làm mới phiên đăng nhập khi server trả 401 (access token hết hạn):
 * gọi /auth/refresh bằng refresh token đã lưu -> cập nhật token mới -> retry lại request cũ.
 *
 * - Dùng [dagger.Lazy] cho [AuthRepository] để phá vòng phụ thuộc (OkHttp -> Authenticator ->
 *   AuthRepository -> Retrofit -> OkHttp).
 * - Đồng bộ ([synchronized]) để nhiều request 401 cùng lúc chỉ refresh một lần; các request sau
 *   thấy token đã mới thì retry luôn.
 * - Bỏ qua chính endpoint /auth/refresh để tránh đệ quy vô hạn; giới hạn số lần retry.
 * - Refresh thất bại -> xoá phiên và trả null (request giữ nguyên 401 để UI điều hướng về Login).
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val authRepository: dagger.Lazy<AuthRepository>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d(TAG, "authenticate() 401 on ${response.request.url}")
        // Không refresh cho chính request refresh, tránh đệ quy.
        if (response.request.url.encodedPath.endsWith("/auth/refresh")) {
            Log.d(TAG, "skip: 401 from /auth/refresh itself")
            return null
        }
        // Đã retry rồi mà vẫn 401 -> dừng.
        if (responseCount(response) >= 2) {
            Log.d(TAG, "skip: already retried (responseCount >= 2)")
            return null
        }

        val currentRefreshToken = SharedPrefCommon.refreshToken
        if (currentRefreshToken.isEmpty()) {
            Log.w(TAG, "skip: refreshToken rỗng trong cache -> không thể refresh (cần đăng nhập lại)")
            return null
        }

        synchronized(this) {
            val latestAccessToken = SharedPrefCommon.accessToken
            val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            // Thread khác đã refresh xong trong lúc chờ lock -> dùng token mới nhất, khỏi gọi lại.
            if (latestAccessToken.isNotEmpty() && latestAccessToken != failedToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $latestAccessToken")
                    .build()
            }

            Log.d(TAG, "gọi /auth/refresh ...")
            val result = runBlocking {
                authRepository.get().refreshToken(ReqRefreshToken(refreshToken = currentRefreshToken))
            }
            val session = (result as? ResultWrapper.Success)?.value?.data
            val newAccessToken = session?.accessToken

            return if (newAccessToken.isNullOrEmpty()) {
                // Refresh hỏng (refresh token cũng hết hạn) -> xoá phiên.
                Log.w(TAG, "refresh thất bại -> xoá phiên, giữ 401")
                SharedPrefCommon.accessToken = ""
                SharedPrefCommon.refreshToken = ""
                null
            } else {
                Log.d(TAG, "refresh OK -> retry request với token mới")
                SharedPrefCommon.accessToken = newAccessToken
                session.refreshToken?.takeIf { it.isNotEmpty() }
                    ?.let { SharedPrefCommon.refreshToken = it }
                SharedPrefCommon.saveUser(session.user?.toDomain())
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            }
        }
    }

    private companion object {
        const val TAG = "TokenAuthenticator"
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
