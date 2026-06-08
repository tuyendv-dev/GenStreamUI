package network.ermis.genstreamui.database.network.repository

import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.database.network.service.AuthService
import network.ermis.genstreamui.domain.model.dto.ResLoginDTO
import network.ermis.genstreamui.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Triển khai AuthRepository, delegate sang AuthService. Bind qua Hilt @Binds (RepositoryModule).
 */
class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService
) : AuthRepository {

    override suspend fun login(email: String, password: String): ResultWrapper<ResLoginDTO> =
        authService.login(username = email, password = password)
}
