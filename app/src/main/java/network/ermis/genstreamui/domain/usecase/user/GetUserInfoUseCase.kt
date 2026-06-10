package network.ermis.genstreamui.domain.usecase.user

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.User
import network.ermis.genstreamui.domain.model.mapper.toDomain
import network.ermis.genstreamui.domain.repository.UserRepository
import javax.inject.Inject

/**
 * UseCase lấy thông tin người dùng (/users/me): map ResultWrapper + DTO -> UiState<User core>.
 */
class GetUserInfoUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke() = flow<UiState<User>> {
        emit(UiState.Loading)
        try {
            when (val response = userRepository.getUserInformation()) {
                is ResultWrapper.Success -> {
                    val user = response.value.data?.toDomain()
                    if (user != null) emit(UiState.Success(user))
                    else emit(UiState.Error("Dữ liệu người dùng trống"))
                }
                is ResultWrapper.GenericError -> emit(
                    UiState.Error(
                        message = response.message ?: "Không lấy được thông tin người dùng",
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
