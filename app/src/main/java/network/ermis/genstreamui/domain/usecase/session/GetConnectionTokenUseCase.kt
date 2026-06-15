package network.ermis.genstreamui.domain.usecase.session

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.ConnectionToken
import network.ermis.genstreamui.domain.model.mapper.toDomain
import network.ermis.genstreamui.domain.repository.SessionRepository
import javax.inject.Inject

/**
 * Kết quả xin token kết nối. Tách [VmNotReady] khỏi [Error] vì VM chưa sẵn sàng là trạng thái
 * tạm thời — caller nên `delay` rồi gọi lại (poll), không coi là lỗi thật.
 */
sealed interface ConnectionTokenResult {
    data class Issued(val token: ConnectionToken) : ConnectionTokenResult
    data object VmNotReady : ConnectionTokenResult
    data class Error(val message: String, val code: String = "") : ConnectionTokenResult
}

/**
 * UseCase xin token kết nối tới host stream — POST /sessions/{id}/connection-token.
 *
 * Suspend one-shot (không phải Flow) để hợp với vòng poll:
 * ```
 * while (true) {
 *     when (val r = getConnectionToken(sessionId, deviceName)) {
 *         ConnectionTokenResult.VmNotReady -> delay(2_000)
 *         is ConnectionTokenResult.Issued  -> { connect(r.token); break }
 *         is ConnectionTokenResult.Error   -> { showError(r.message); break }
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
                        // Trả 2xx nhưng chưa có token (vd error = VM_NOT_READY).
                        isVmNotReady(body.error, body.message) -> ConnectionTokenResult.VmNotReady
                        else -> ConnectionTokenResult.Error(body.message ?: "Không lấy được token kết nối")
                    }
                }
                is ResultWrapper.GenericError ->
                    // VM chưa sẵn sàng có thể về dưới dạng lỗi HTTP — nhận diện qua message/code.
                    if (isVmNotReady(null, response.message)) {
                        ConnectionTokenResult.VmNotReady
                    } else {
                        ConnectionTokenResult.Error(
                            message = response.message ?: "Không lấy được token kết nối",
                            code = response.code?.toString().orEmpty()
                        )
                    }
                ResultWrapper.NetworkError ->
                    ConnectionTokenResult.Error("Không có kết nối mạng")
            }
        } catch (e: Exception) {
            ConnectionTokenResult.Error(e.message ?: "Đã có lỗi xảy ra")
        }
    }

    /** Khớp marker "VM_NOT_READY" dù backend trả ở field error hay trong message. */
    private fun isVmNotReady(error: String?, message: String?): Boolean {
        val haystack = "${error.orEmpty()} ${message.orEmpty()}".lowercase()
        return haystack.contains("vm_not_ready") || haystack.contains("not ready")
    }
}
