package network.ermis.genstreamui.domain.repository

import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.dto.req.ReqChangePassword
import network.ermis.genstreamui.domain.model.dto.req.ReqUpdateUserInfo
import network.ermis.genstreamui.domain.model.dto.res.ResChangePassword
import network.ermis.genstreamui.domain.model.dto.res.ResUserInfo

/**
 * Repository interface tầng domain cho người dùng. Song song với [AuthRepository].
 */
interface UserRepository {
    suspend fun getUserInformation(): ResultWrapper<ResUserInfo>

    suspend fun updateUserInformation(req: ReqUpdateUserInfo): ResultWrapper<ResUserInfo>

    suspend fun changePassword(req: ReqChangePassword): ResultWrapper<ResChangePassword>
}
