package network.ermis.genstreamui.domain.usecase.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.dto.req.ReqVerificationCode
import network.ermis.genstreamui.domain.model.dto.res.ResVerificationCode
import network.ermis.genstreamui.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * UseCase xác minh mã (OTP/email): map ResultWrapper -> UiState. Port theo GenPlayAndroid.
 */
class VerificationCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(req: ReqVerificationCode) = flow<UiState<ResVerificationCode>> {
        emit(UiState.Loading)
        try {
            when (val response = authRepository.verificationCode(req)) {
                is ResultWrapper.Success -> emit(UiState.Success(response.value))
                is ResultWrapper.GenericError -> emit(
                    UiState.Error(
                        message = response.message ?: "Mã xác minh không hợp lệ",
                        code = response.code?.toString().orEmpty()
                    )
                )
                ResultWrapper.NetworkError -> emit(UiState.Error("Không có kết nối mạng"))
            }
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Đã có lỗi xảy ra"))
        }
    }.flowOn(Dispatchers.IO)
}
