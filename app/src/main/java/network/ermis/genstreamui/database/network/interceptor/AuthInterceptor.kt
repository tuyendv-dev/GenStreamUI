package network.ermis.genstreamui.database.network.interceptor

import network.ermis.genstreamui.database.cache.SharedPrefCommon
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Gắn header Authorization: Bearer <accessToken> cho mọi request khi đã đăng nhập.
 * Token lấy từ [SharedPrefCommon.accessToken]; nếu rỗng (chưa login) thì bỏ qua.
 */
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = SharedPrefCommon.accessToken
        val request = if (token.isNotEmpty()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
