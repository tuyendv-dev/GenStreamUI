package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep

/**
 * Một trailer trong response /games (phần tử mảng "trailers"). Chỉ map field hay dùng —
 * Gson tự bỏ field thừa.
 */
@Keep
data class GameTrailerDTO(
    val name: String? = null,
    val video: String? = null,
    val thumbnail: String? = null,
    val category: Int? = null,
    val adaptive: List<TrailerStreamDTO>? = null,
    val microtrailer: List<TrailerStreamDTO>? = null
)

/** Nguồn video của trailer. adaptive -> [encoding]; microtrailer -> [type]. */
@Keep
data class TrailerStreamDTO(
    val url: String? = null,
    val encoding: String? = null,
    val type: String? = null
)
