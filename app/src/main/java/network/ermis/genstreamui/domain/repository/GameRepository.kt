package network.ermis.genstreamui.domain.repository

import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.dto.res.ResDiscovery

/**
 * Repository interface tầng domain cho game. Song song với [AuthRepository] / [UserRepository].
 */
interface GameRepository {
    suspend fun getDiscovery(): ResultWrapper<ResDiscovery>

    /** Duyệt game theo store (vd: "steam") — GET /games/browse?store=. */
    suspend fun findGameStore(store: String?): ResultWrapper<ResDiscovery>
}
