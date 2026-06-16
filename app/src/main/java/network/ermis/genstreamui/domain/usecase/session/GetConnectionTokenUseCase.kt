package network.ermis.genstreamui.domain.usecase.session

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import network.ermis.genstreamui.database.network.factory.ApiErrorCode
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.ConnectionToken
import network.ermis.genstreamui.domain.model.mapper.toDomain
import network.ermis.genstreamui.domain.repository.SessionRepository
import javax.inject.Inject

/**
 * Kết quả xin token kết nối. Tách [VmNotReady] khỏi [Error] vì VM chưa sẵn sàng là trạng thái
 * tạm thời — caller nên `delay` rồi gọi lại (poll), không coi là lỗi thật.
 * [errorCode] giúp caller xử lý riêng vd [ApiErrorCode.RATE_LIMITED] (backoff lâu hơn).
 */
sealed interface ConnectionTokenResult {
    data class Issued(val token: ConnectionToken) : ConnectionTokenResult
    data object VmNotReady : ConnectionTokenResult
    data class Error(
        val message: String,
        val errorCode: ApiErrorCode = ApiErrorCode.UNKNOWN
    ) : ConnectionTokenResult
}

/**
 * UseCase xin token kết nối tới host stream — POST /sessions/{id}/connection-token (genstream-custom-auth.md §6 Stage 1).
 *
 * Suspend one-shot (không phải Flow) để hợp với vòng poll. Phân biệt [ApiErrorCode.VM_NOT_READY]
 * (409 — poll tiếp) với mọi lỗi khác (vd 409 SESSION_NOT_READY = phiên đã chết, phải dừng):
 * ```
 * while (true) {
 *     when (val r = getConnectionToken(sessionId, deviceName)) {
 *         ConnectionTokenResult.VmNotReady -> delay(7_000)            // cadence ~7s, budget 5'
 *         is ConnectionTokenResult.Issued  -> { tokenAuth(r.token); break }
 *         is ConnectionTokenResult.Error   ->
 *             if (r.errorCode == ApiErrorCode.RATE_LIMITED) delay(15_000) else { fail(r.message); break }
 *     }
 * }
 * ```
 */
class GetConnectionTokenUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(
        sessionId: Int,
        deviceName: String,
        deviceType: String = "android"
    ): ConnectionTokenResult = withContext(Dispatchers.IO) {
        try {
            when (val response = sessionRepository.getConnectionToken(sessionId, deviceName, deviceType)) {
                is ResultWrapper.Success -> {
                    val body = response.value
                    val data = body.data
                    when {
                        data?.token?.isNotEmpty() == true ->
                            ConnectionTokenResult.Issued(data.toDomain())
                        // Trả 2xx nhưng chưa có token (phòng trường hợp backend trả error trong body 200).
                        ApiErrorCode.from(body.error) == ApiErrorCode.VM_NOT_READY ->
                            ConnectionTokenResult.VmNotReady
                        else -> ConnectionTokenResult.Error(body.message ?: "Không lấy được token kết nối")
                    }
                }
                is ResultWrapper.GenericError ->
                    if (response.apiError == ApiErrorCode.VM_NOT_READY) {
                        ConnectionTokenResult.VmNotReady
                    } else {
                        ConnectionTokenResult.Error(
                            message = response.message ?: "Không lấy được token kết nối",
                            errorCode = response.apiError
                        )
                    }
                ResultWrapper.NetworkError ->
                    ConnectionTokenResult.Error("Không có kết nối mạng")
            }
        } catch (e: Exception) {
            ConnectionTokenResult.Error(e.message ?: "Đã có lỗi xảy ra")
        }
    }
}
