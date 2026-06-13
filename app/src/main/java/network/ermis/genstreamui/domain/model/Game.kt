package network.ermis.genstreamui.domain.model

/**
 * Model core game ở tầng domain — UI/ViewModel làm việc trực tiếp với model này thay vì DTO.
 * Map từ [network.ermis.genstreamui.domain.model.dto.res.GameDTO] qua GameMapper.
 *
 * Các trường non-null (có default) để UI khỏi phải xử lý null rải rác.
 */
data class Game(
    val id: Int = 0,
    val slug: String = "",
    val title: String = "",
    val description: String = "",
    val shortDescription: String = "",
    val tagline: String = "",
    val coverImageUrl: String = "",
    val mainCapsule: String = "",
    val portraitImage: String = "",
    val heroImage: String = "",
    val headerImage: String = "",
    val capsuleImage: String = "",
    val backgroundImage: String = "",
    val trailerUrl: String = "",
    val screenshots: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val platforms: List<String> = emptyList(),
    val publisher: String = "",
    val releaseYear: Int = 0,
    val featured: Boolean = false,
    val hot: Boolean = false,
    val recommended: Boolean = false,
    val isActive: Boolean = false
)
