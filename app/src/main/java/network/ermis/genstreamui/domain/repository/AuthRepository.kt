package network.ermis.genstreamui.domain.repository

import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.dto.ResLoginDTO

/**
 * Repository interface tầng domain — tách presentation khỏi nguồn dữ liệu, dễ mock khi test.
 */
interface AuthRepository {
    suspend fun login(email: String, password: String): ResultWrapper<ResLoginDTO>
}
