package network.ermis.genstreamui.database.network.service

import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.dto.ResLoginDTO
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit service xác thực (mẫu). Endpoint/field là placeholder — chỉnh theo API ermis thật.
 */
interface AuthService {

    @FormUrlEncoded
    @POST("/api/user/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Header("Accept-Language") language: String = "en"
    ): ResultWrapper<ResLoginDTO>
}
