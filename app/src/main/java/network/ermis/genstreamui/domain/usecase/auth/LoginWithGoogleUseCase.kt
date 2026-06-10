package network.ermis.genstreamui.domain.usecase.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.dto.req.ReqLoginGgDTO
import network.ermis.genstreamui.domain.model.dto.res.ResGoogleLoginDTO
import network.ermis.genstreamui.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * UseCase đổi authorization code (OAuth + PKCE) lấy phiên đăng nhập từ backend.
 * Phần lấy code từ Google nằm ở [LoginByGoogleUseCase].
 */
class LoginWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(req: ReqLoginGgDTO) = flow<UiState<ResGoogleLoginDTO>> {
        emit(UiState.Loading)
        try {
            when (val response = authRepository.loginWithGoogle(req)) {
                is ResultWrapper.Success -> emit(UiState.Success(response.value))
                is ResultWrapper.GenericError -> emit(
                    UiState.Error(
                        message = response.message ?: "Đăng nhập Google thất bại",
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
