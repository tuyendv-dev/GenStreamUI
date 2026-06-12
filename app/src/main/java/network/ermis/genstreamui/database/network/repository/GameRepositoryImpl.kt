package network.ermis.genstreamui.database.network.repository

import network.ermis.genstreamui.database.network.service.GameService
import network.ermis.genstreamui.domain.repository.GameRepository
import javax.inject.Inject

/**
 * Triển khai GameRepository, delegate sang GameService. Bind qua Hilt @Binds (RepositoryModule).
 */
class GameRepositoryImpl @Inject constructor(
    private val gameService: GameService
) : GameRepository {

    override suspend fun getDiscovery() =
        gameService.getDiscovery()

    override suspend fun findGameStore(store: String?) =
        gameService.browse(store)
}
