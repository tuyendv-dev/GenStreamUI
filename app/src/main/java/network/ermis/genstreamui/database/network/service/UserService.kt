package network.ermis.genstreamui.database.network.service

import network.ermis.genstreamui.database.cache.SharedPrefCommon
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.dto.req.ReqChangePassword
import network.ermis.genstreamui.domain.model.dto.req.ReqUpdateUserInfo
import network.ermis.genstreamui.domain.model.dto.res.ResChangePassword
import network.ermis.genstreamui.domain.model.dto.res.ResUserInfo
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Retrofit service cho người dùng. Bearer token được gắn tự động qua AuthInterceptor.
 * Endpoint update/change-password là placeholder — chỉnh theo spec backend ermis thật khi có.
 */
interface UserService {

    @GET("/users/me")
    suspend fun getUserInformation(
        @Header("Accept") accept: String = "application/json",
        @Header("Accept-Language") language: String = SharedPrefCommon.languageCode.ifEmpty { "en" }
    ): ResultWrapper<ResUserInfo>

    @PUT("/users/me")
    suspend fun updateUserInformation(
        @Body req: ReqUpdateUserInfo,
        @Header("Accept-Language") language: String = SharedPrefCommon.languageCode.ifEmpty { "en" }
    ): ResultWrapper<ResUserInfo>

    @POST("/users/me/change-password")
    suspend fun changePassword(
        @Body req: ReqChangePassword,
        @Header("Accept-Language") language: String = SharedPrefCommon.languageCode.ifEmpty { "en" }
    ): ResultWrapper<ResChangePassword>
}
