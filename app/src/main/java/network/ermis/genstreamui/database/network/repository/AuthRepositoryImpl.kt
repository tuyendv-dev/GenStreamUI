package network.ermis.genstreamui.database.network.repository

import network.ermis.genstreamui.database.network.service.AuthService
import network.ermis.genstreamui.domain.model.dto.req.ReqForgetPasswordDTO
import network.ermis.genstreamui.domain.model.dto.req.ReqLoginGgDTO
import network.ermis.genstreamui.domain.model.dto.req.ReqRefreshToken
import network.ermis.genstreamui.domain.model.dto.req.ReqRegisterAccount
import network.ermis.genstreamui.domain.model.dto.req.ReqResendOtpDTO
import network.ermis.genstreamui.domain.model.dto.req.ReqResetPassword
import network.ermis.genstreamui.domain.model.dto.req.ReqVerificationCode
import network.ermis.genstreamui.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Triển khai AuthRepository, delegate sang AuthService. Bind qua Hilt @Binds (RepositoryModule).
 */
class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService
) : AuthRepository {

    override suspend fun loginWithGoogle(reqLoginGgDTO: ReqLoginGgDTO) =
        authService.loginWithGoogle(reqLoginGgDTO)

    override suspend fun loginByEmailAndPassword(email: String, password: String) =
        authService.loginByEmailAndPassword(userName = email, password = password)

    override suspend fun forgetPassword(req: ReqForgetPasswordDTO) =
        authService.forgetPassword(req)

    override suspend fun resetPassword(req: ReqResetPassword) =
        authService.resetPassword(req)

    override suspend fun registerAccount(req: ReqRegisterAccount) =
        authService.registerAccount(req)

    override suspend fun verificationCode(req: ReqVerificationCode) =
        authService.verificationCode(req)

    override suspend fun resendOtp(req: ReqResendOtpDTO) =
        authService.resendOtp(req)

    override suspend fun refreshToken(req: ReqRefreshToken) =
        authService.refreshToken(req)
}
