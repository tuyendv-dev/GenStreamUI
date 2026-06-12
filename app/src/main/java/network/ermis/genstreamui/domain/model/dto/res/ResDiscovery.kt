package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep

/** Response bọc "data" cho GET /games/discovery. */
@Keep
data class ResDiscovery(
    val data: DiscoveryDataDTO? = null,
    val message: String? = null
)

/** Nội dung discovery: các nhóm game nổi bật + danh sách section theo category. */
@Keep
data class DiscoveryDataDTO(
    val featured: List<GameDTO>? = null,
    val hot: List<GameDTO>? = null,
    val recommended: List<GameDTO>? = null,
    val sections: List<DiscoverySectionDTO>? = null
)

/** Một section trong discovery: tên category + danh sách game thuộc category đó. */
@Keep
data class DiscoverySectionDTO(
    val category: String? = null,
    val games: List<GameDTO>? = null
)
