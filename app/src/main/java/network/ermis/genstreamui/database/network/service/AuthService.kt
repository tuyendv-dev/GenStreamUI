package network.ermis.genstreamui.database.network.service

import network.ermis.genstreamui.database.cache.SharedPrefCommon
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.dto.req.ReqForgetPasswordDTO
import network.ermis.genstreamui.domain.model.dto.req.ReqLoginGgDTO
import network.ermis.genstreamui.domain.model.dto.req.ReqRegisterAccount
import network.ermis.genstreamui.domain.model.dto.req.ReqResendOtpDTO
import network.ermis.genstreamui.domain.model.dto.req.ReqResetPassword
import network.ermis.genstreamui.domain.model.dto.req.ReqVerificationCode
import network.ermis.genstreamui.domain.model.dto.res.ResForgetPassword
import network.ermis.genstreamui.domain.model.dto.res.ResGoogleLoginDTO
import network.ermis.genstreamui.domain.model.dto.res.ResLoginDTO
import network.ermis.genstreamui.domain.model.dto.res.ResRegisterAccount
import network.ermis.genstreamui.domain.model.dto.res.ResResendOtp
import network.ermis.genstreamui.domain.model.dto.res.ResResetPassword
import network.ermis.genstreamui.domain.model.dto.res.ResVerificationCode
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit service xác thực. Port theo GenPlayAndroid (database/network/service/AuthService.kt).
 * Endpoint/field là placeholder — chỉnh theo spec backend ermis thật khi có.
 */
interface AuthService {

    @POST("/auth/google")
    suspend fun loginWithGoogle(
        @Body reqLoginGgDTO: ReqLoginGgDTO,
        @Header("Accept-Language") language: String = SharedPrefCommon.languageCode.ifEmpty { "en" }
    ): ResultWrapper<ResGoogleLoginDTO>

    @FormUrlEncoded
    @POST("/auth/login")
    suspend fun loginByEmailAndPassword(
        @Field("username") userName: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String = "password",
        @Header("Accept") accept: String = "application/json",
        @Header("Accept-Language") language: String = SharedPrefCommon.languageCode.ifEmpty { "en" }
    ): ResultWrapper<ResLoginDTO>

    @POST("/auth/forgot-password")
    suspend fun forgetPassword(
        @Body reqBody: ReqForgetPasswordDTO,
        @Header("Accept-Language") language: String = SharedPrefCommon.languageCode.ifEmpty { "en" }
    ): ResultWrapper<ResForgetPassword>

    @POST("/auth/reset-password")
    suspend fun resetPassword(
        @Body req: ReqResetPassword,
        @Header("Accept-Language") language: String = SharedPrefCommon.languageCode.ifEmpty { "en" }
    ): ResultWrapper<ResResetPassword>

    @POST("/auth/register")
    suspend fun registerAccount(
        @Body req: ReqRegisterAccount,
        @Header("Accept-Language") language: String = SharedPrefCommon.languageCode.ifEmpty { "en" }
    ): ResultWrapper<ResRegisterAccount>

    @POST("/auth/verify-email")
    suspend fun verificationCode(
        @Body req: ReqVerificationCode,
        @Header("Accept-Language") language: String = SharedPrefCommon.languageCode.ifEmpty { "en" }
    ): ResultWrapper<ResVerificationCode>

    @POST("/auth/resend-otp")
    suspend fun resendOtp(
        @Body req: ReqResendOtpDTO,
        @Header("Accept-Language") language: String = SharedPrefCommon.languageCode.ifEmpty { "en" }
    ): ResultWrapper<ResResendOtp>
}
