package network.ermis.genstreamui.domain.usecase.session

import network.ermis.genstreamui.domain.model.ConnectionToken
import network.ermis.genstreamui.domain.model.TokenAuthResult
import network.ermis.genstreamui.domain.repository.GatewayRepository
import javax.inject.Inject

/**
 * UseCase token-auth (genstream-custom-auth.md §6 Stage 2): đổi connection token + client cert lấy
 * quyền stream từ host, pin server cert. Gọi ngay sau khi [GetConnectionTokenUseCase] trả `Issued`.
 *
 * ```
 * when (val r = tokenAuth(connectionToken, deviceName = Build.MODEL)) {
 *     is TokenAuthResult.Authorized -> registerHostAndStream(r.effectiveName)
 *     is TokenAuthResult.Rejected   -> fail(r.message)   // token/phiên hỏng
 *     is TokenAuthResult.Error      -> fail(r.message)   // VM chưa lên, hết warm-up
 * }
 * ```
 */
class TokenAuthUseCase @Inject constructor(
    private val gatewayRepository: GatewayRepository
) {
    suspend operator fun invoke(connection: ConnectionToken, deviceName: String): TokenAuthResult =
        gatewayRepository.tokenAuth(connection, deviceName)
}
