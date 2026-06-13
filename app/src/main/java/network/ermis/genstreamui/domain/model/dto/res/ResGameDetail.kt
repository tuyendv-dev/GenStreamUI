package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep

/**
 * Response bọc "data" cho GET /games/{id} (chi tiết một game).
 * Tái dùng [GameDTO] cho phần data — response chi tiết chứa đủ các field core của GameDTO,
 * các field thừa (detailed_description, similar, movies...) được Gson tự bỏ qua.
 */
@Keep
data class ResGameDetail(
    val data: GameDTO? = null,
    val message: String? = null
)
