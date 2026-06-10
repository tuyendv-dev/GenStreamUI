package network.ermis.genstreamui.domain.usecase.user

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.dto.req.ReqChangePassword
import network.ermis.genstreamui.domain.model.dto.res.ResChangePassword
import network.ermis.genstreamui.domain.repository.UserRepository
import javax.inject.Inject

/**
 * UseCase đổi mật khẩu: map ResultWrapper -> UiState.
 */
class ChangePasswordUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(req: ReqChangePassword) = flow<UiState<ResChangePassword>> {
        emit(UiState.Loading)
        try {
            when (val response = userRepository.changePassword(req)) {
                is ResultWrapper.Success -> emit(UiState.Success(response.value))
                is ResultWrapper.GenericError -> emit(
                    UiState.Error(
                        message = response.message ?: "Đổi mật khẩu thất bại",
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
