package network.ermis.genstreamui.database.network.repository

import androidx.room.withTransaction
import network.ermis.genstreamui.database.cache.SharedPrefCommon
import network.ermis.genstreamui.database.network.service.GameService
import network.ermis.genstreamui.database.storage.AppDatabase
import network.ermis.genstreamui.database.storage.dao.DiscoveryRefDao
import network.ermis.genstreamui.database.storage.dao.GameDao
import network.ermis.genstreamui.domain.model.Discovery
import network.ermis.genstreamui.domain.model.DiscoverySection
import network.ermis.genstreamui.domain.model.Game
import network.ermis.genstreamui.domain.model.entities.DiscoveryRefEntity
import network.ermis.genstreamui.domain.model.entities.DiscoveryRefEntity.Companion.BUCKET_FEATURED
import network.ermis.genstreamui.domain.model.entities.DiscoveryRefEntity.Companion.BUCKET_HOT
import network.ermis.genstreamui.domain.model.entities.DiscoveryRefEntity.Companion.BUCKET_RECOMMENDED
import network.ermis.genstreamui.domain.model.entities.DiscoveryRefEntity.Companion.BUCKET_SECTION
import network.ermis.genstreamui.domain.model.mapper.toDomain
import network.ermis.genstreamui.domain.model.mapper.toEntity
import network.ermis.genstreamui.domain.repository.GameRepository
import javax.inject.Inject

/**
 * Triển khai GameRepository: delegate API sang [GameService], cache cục bộ qua Room.
 * Mô hình 2 bảng — [gameDao] giữ chi tiết game (dedup theo id), [discoveryRefDao] giữ index
 * layout theo cacheKey (gắn cả ngôn ngữ để không hiện nhầm cache khác ngôn ngữ).
 */
class GameRepositoryImpl @Inject constructor(
    private val gameService: GameService,
    private val database: AppDatabase,
    private val gameDao: GameDao,
    private val discoveryRefDao: DiscoveryRefDao
) : GameRepository {

    override suspend fun getDiscovery() =
        gameService.getDiscovery()

    override suspend fun findGameStore(store: String?) =
        gameService.browse(store)

    override suspend fun cachedDiscovery(): Discovery? =
        readCache(discoveryKey())

    override suspend fun saveDiscovery(discovery: Discovery) =
        writeCache(discoveryKey(), discovery)

    override suspend fun cachedStore(store: String?): Discovery? =
        readCache(storeKey(store))

    override suspend fun saveStore(store: String?, discovery: Discovery) =
        writeCache(storeKey(store), discovery)

    override suspend fun getGameById(id: Int): Game? =
        gameDao.getById(id)?.toDomain()

    override suspend fun getGameDetail(id: Int) =
        gameService.getGameDetail(id)

    override suspend fun saveGame(game: Game) =
        gameDao.upsertAll(listOf(game.toEntity()))

    // region cache key

    private fun lang(): String = SharedPrefCommon.languageCode.ifEmpty { "en" }

    private fun discoveryKey(): String = "discovery:${lang()}"

    private fun storeKey(store: String?): String = "browse:${store ?: "all"}:${lang()}"

    // endregion

    // region cache read/write

    /** Dựng lại [Discovery] từ bảng game + index layout của [cacheKey]. Null nếu chưa cache. */
    private suspend fun readCache(cacheKey: String): Discovery? {
        val refs = discoveryRefDao.getByKey(cacheKey)
        if (refs.isEmpty()) return null

        val gamesById = gameDao.getByIds(refs.map { it.gameId }.distinct())
            .associate { it.id to it.toDomain() }

        fun bucket(name: String): List<Game> = refs
            .filter { it.bucket == name }
            .sortedBy { it.position }
            .mapNotNull { gamesById[it.gameId] }

        val sections = refs
            .filter { it.bucket == BUCKET_SECTION }
            .groupBy { it.groupIndex }
            .toSortedMap()
            .map { (_, rows) ->
                DiscoverySection(
                    category = rows.first().category,
                    games = rows.sortedBy { it.position }.mapNotNull { gamesById[it.gameId] }
                )
            }

        return Discovery(
            featured = bucket(BUCKET_FEATURED),
            hot = bucket(BUCKET_HOT),
            recommended = bucket(BUCKET_RECOMMENDED),
            sections = sections
        )
    }

    /** Upsert toàn bộ game của màn vào kho game, rồi thay trọn vẹn index layout của [cacheKey]. */
    private suspend fun writeCache(cacheKey: String, discovery: Discovery) {
        val now = System.currentTimeMillis()

        val allGames = (discovery.featured + discovery.hot + discovery.recommended +
            discovery.sections.flatMap { it.games })
            .distinctBy { it.id }

        val refs = buildList {
            addAll(refsFor(cacheKey, BUCKET_FEATURED, discovery.featured, now))
            addAll(refsFor(cacheKey, BUCKET_HOT, discovery.hot, now))
            addAll(refsFor(cacheKey, BUCKET_RECOMMENDED, discovery.recommended, now))
            discovery.sections.forEachIndexed { sectionIndex, section ->
                section.games.forEachIndexed { position, game ->
                    add(
                        DiscoveryRefEntity(
                            cacheKey = cacheKey,
                            bucket = BUCKET_SECTION,
                            groupIndex = sectionIndex,
                            position = position,
                            category = section.category,
                            gameId = game.id,
                            updatedAt = now
                        )
                    )
                }
            }
        }

        // Upsert kho game + thay trọn vẹn layout của cacheKey trong một transaction.
        database.withTransaction {
            gameDao.upsertAll(allGames.map { it.toEntity() })
            discoveryRefDao.clearByKey(cacheKey)
            discoveryRefDao.insertAll(refs)
        }
    }

    private fun refsFor(
        cacheKey: String,
        bucket: String,
        games: List<Game>,
        now: Long
    ): List<DiscoveryRefEntity> = games.mapIndexed { position, game ->
        DiscoveryRefEntity(
            cacheKey = cacheKey,
            bucket = bucket,
            groupIndex = 0,
            position = position,
            gameId = game.id,
            updatedAt = now
        )
    }

    // endregion
}
