package network.ermis.genstreamui.domain.repository

import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.Discovery
import network.ermis.genstreamui.domain.model.Game
import network.ermis.genstreamui.domain.model.dto.res.ResDiscovery

/**
 * Repository interface tầng domain cho game. Song song với [AuthRepository] / [UserRepository].
 *
 * Cache cục bộ (Room) theo mô hình 2 bảng: bảng game (chi tiết, dedup theo id) + bảng
 * discovery_ref (index layout theo cacheKey). UseCase đọc cache trước rồi gọi API revalidate.
 */
interface GameRepository {
    suspend fun getDiscovery(): ResultWrapper<ResDiscovery>

    /** Duyệt game theo store (vd: "steam") — GET /games/browse?store=. */
    suspend fun findGameStore(store: String?): ResultWrapper<ResDiscovery>

    /** Đọc snapshot discovery đã cache (null nếu chưa có). */
    suspend fun cachedDiscovery(): Discovery?

    /** Ghi đè cache discovery sau khi API trả về thành công. */
    suspend fun saveDiscovery(discovery: Discovery)

    /** Đọc snapshot browse theo store đã cache (null nếu chưa có). */
    suspend fun cachedStore(store: String?): Discovery?

    /** Ghi đè cache browse theo store sau khi API trả về thành công. */
    suspend fun saveStore(store: String?, discovery: Discovery)

    /** Truy vấn thông tin một game từ kho game cục bộ chỉ bằng id. */
    suspend fun getGameById(id: Int): Game?
}
