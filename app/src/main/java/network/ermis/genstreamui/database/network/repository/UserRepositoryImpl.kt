package network.ermis.genstreamui.database.network.repository

import network.ermis.genstreamui.database.network.service.UserService
import network.ermis.genstreamui.domain.model.dto.req.ReqChangePassword
import network.ermis.genstreamui.domain.model.dto.req.ReqUpdateUserInfo
import network.ermis.genstreamui.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Triển khai UserRepository, delegate sang UserService. Bind qua Hilt @Binds (RepositoryModule).
 */
class UserRepositoryImpl @Inject constructor(
    private val userService: UserService
) : UserRepository {

    override suspend fun getUserInformation() =
        userService.getUserInformation()

    override suspend fun updateUserInformation(req: ReqUpdateUserInfo) =
        userService.updateUserInformation(req)

    override suspend fun changePassword(req: ReqChangePassword) =
        userService.changePassword(req)
}
