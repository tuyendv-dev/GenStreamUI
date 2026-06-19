package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Response cho GET /games (search/filter). Trả về danh sách game + thông tin phân trang.
 * Tái dùng [GameDTO] cho mỗi item — Gson tự bỏ qua field thừa.
 */
@Keep
data class ResSearchGames(
    val data: List<GameDTO>? = null,
    val pagination: PaginationDTO? = null,
    val message: String? = null
)

/** Thông tin phân trang đi kèm response danh sách game. */
@Keep
data class PaginationDTO(
    val page: Int? = null,
    @SerializedName("per_page")
    val perPage: Int? = null,
    val total: Int? = null,
    @SerializedName("total_pages")
    val totalPages: Int? = null
)
