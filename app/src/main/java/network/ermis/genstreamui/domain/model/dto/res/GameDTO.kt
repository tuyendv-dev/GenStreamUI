package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Một game trong response (discovery / danh sách). Chỉ map các field hay dùng để hiển thị —
 * Gson tự bỏ qua field thừa nên không cần khai báo đủ toàn bộ schema.
 */
@Keep
data class GameDTO(
    val id: Int? = null,
    val slug: String? = null,
    val title: String? = null,
    val description: String? = null,
    val tagline: String? = null,
    @SerializedName("short_description")
    val shortDescription: String? = null,
    @SerializedName("cover_image_url")
    val coverImageUrl: String? = null,
    @SerializedName("main_capsule")
    val mainCapsule: String? = null,
    @SerializedName("portrait_image")
    val portraitImage: String? = null,
    @SerializedName("hero_image")
    val heroImage: String? = null,
    @SerializedName("header_image")
    val headerImage: String? = null,
    @SerializedName("capsule_image")
    val capsuleImage: String? = null,
    @SerializedName("background_image")
    val backgroundImage: String? = null,
    @SerializedName("trailer_url")
    val trailerUrl: String? = null,
    val screenshots: List<String>? = null,
    val categories: List<String>? = null,
    val platforms: List<String>? = null,
    val publisher: String? = null,
    @SerializedName("release_year")
    val releaseYear: Int? = null,
    val featured: Boolean? = null,
    val hot: Boolean? = null,
    val recommended: Boolean? = null,
    @SerializedName("is_active")
    val isActive: Boolean? = null
)
