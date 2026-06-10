package network.ermis.genstreamui.domain.usecase.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.dto.req.ReqForgetPasswordDTO
import network.ermis.genstreamui.domain.model.dto.res.ResForgetPassword
import network.ermis.genstreamui.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * UseCase quên mật khẩu: gửi email yêu cầu, map ResultWrapper -> UiState. Port theo GenPlayAndroid.
 */
class ForgetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(req: ReqForgetPasswordDTO) = flow<UiState<ResForgetPassword>> {
        emit(UiState.Loading)
        try {
            when (val response = authRepository.forgetPassword(req)) {
                is ResultWrapper.Success -> emit(UiState.Success(response.value))
                is ResultWrapper.GenericError -> emit(
                    UiState.Error(
                        message = response.message ?: "Yêu cầu thất bại",
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
