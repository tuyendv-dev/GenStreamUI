package network.ermis.genstreamui.domain.model

/**
 * Model core dữ liệu màn Discovery ở tầng domain. Map từ
 * [network.ermis.genstreamui.domain.model.dto.res.DiscoveryDataDTO] qua GameMapper.
 */
data class Discovery(
    val featured: List<Game> = emptyList(),
    val hot: List<Game> = emptyList(),
    val recommended: List<Game> = emptyList(),
    val sections: List<DiscoverySection> = emptyList()
)

/** Một section trong discovery: tên category + danh sách game thuộc category đó. */
data class DiscoverySection(
    val category: String = "",
    val games: List<Game> = emptyList()
)
