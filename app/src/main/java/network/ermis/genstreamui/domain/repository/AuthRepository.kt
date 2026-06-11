package network.ermis.genstreamui.domain.repository

import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.dto.req.ReqForgetPasswordDTO
import network.ermis.genstreamui.domain.model.dto.req.ReqLoginGgDTO
import network.ermis.genstreamui.domain.model.dto.req.ReqRefreshToken
import network.ermis.genstreamui.domain.model.dto.req.ReqRegisterAccount
import network.ermis.genstreamui.domain.model.dto.req.ReqResendOtpDTO
import network.ermis.genstreamui.domain.model.dto.req.ReqResetPassword
import network.ermis.genstreamui.domain.model.dto.req.ReqVerificationCode
import network.ermis.genstreamui.domain.model.dto.res.ResForgetPassword
import network.ermis.genstreamui.domain.model.dto.res.ResGoogleLoginDTO
import network.ermis.genstreamui.domain.model.dto.res.ResLoginDTO
import network.ermis.genstreamui.domain.model.dto.res.ResRefreshToken
import network.ermis.genstreamui.domain.model.dto.res.ResRegisterAccount
import network.ermis.genstreamui.domain.model.dto.res.ResResendOtp
import network.ermis.genstreamui.domain.model.dto.res.ResResetPassword
import network.ermis.genstreamui.domain.model.dto.res.ResVerificationCode

/**
 * Repository interface tầng domain cho xác thực. Port theo GenPlayAndroid.
 */
interface AuthRepository {
    suspend fun loginWithGoogle(reqLoginGgDTO: ReqLoginGgDTO): ResultWrapper<ResGoogleLoginDTO>

    suspend fun loginByEmailAndPassword(
        email: String,
        password: String
    ): ResultWrapper<ResLoginDTO>

    suspend fun forgetPassword(req: ReqForgetPasswordDTO): ResultWrapper<ResForgetPassword>

    suspend fun resetPassword(req: ReqResetPassword): ResultWrapper<ResResetPassword>

    suspend fun registerAccount(req: ReqRegisterAccount): ResultWrapper<ResRegisterAccount>

    suspend fun verificationCode(req: ReqVerificationCode): ResultWrapper<ResVerificationCode>

    suspend fun resendOtp(req: ReqResendOtpDTO): ResultWrapper<ResResendOtp>

    suspend fun refreshToken(req: ReqRefreshToken): ResultWrapper<ResRefreshToken>
}
