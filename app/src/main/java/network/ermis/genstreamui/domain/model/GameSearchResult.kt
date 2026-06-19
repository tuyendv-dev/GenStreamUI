package network.ermis.genstreamui.domain.model

/**
 * Kết quả tìm/lọc game (GET /games): danh sách game + thông tin phân trang.
 * UI dùng [total] để hiển thị số lượng kết quả, [games] để render lưới,
 * [page]/[totalPages] để biết trang hiện tại và còn trang để load thêm hay không.
 */
data class GameSearchResult(
    val games: List<Game> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val totalPages: Int = 1
) {
    /** Còn trang sau để load thêm không. */
    val hasMore: Boolean get() = page < totalPages
}
