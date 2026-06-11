package network.ermis.genstreamui.database.network.interceptor

import network.ermis.genstreamui.database.cache.SharedPrefCommon
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Gắn header Authorization: Bearer <accessToken> cho request đã đăng nhập.
 * Token lấy từ [SharedPrefCommon.accessToken]; rỗng (chưa login) thì bỏ qua.
 *
 * Bỏ qua các endpoint dưới /auth/ (login, register, refresh...) — chúng là public và không cần bearer.
 * Đặc biệt tránh đính token đã hết hạn vào /auth/refresh khiến chính request refresh bị 401.
 */
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = SharedPrefCommon.accessToken
        val request = if (token.isNotEmpty() && !original.url.encodedPath.startsWith("/auth/")) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        return chain.proceed(request)
    }
}
