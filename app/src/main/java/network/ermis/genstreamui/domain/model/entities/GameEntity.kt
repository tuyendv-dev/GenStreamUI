package network.ermis.genstreamui.domain.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity Room lưu chi tiết một game — bảng "kho game" dùng chung cho discovery và browse.
 * Khoá chính là [id] nên mỗi game chỉ lưu một dòng (tự dedup), truy vấn lại bằng id.
 * Mirror domain [network.ermis.genstreamui.domain.model.Game]; map qua GameEntityMapper.
 * Các trường List<String> dựa vào TypeConverters (Converters.kt).
 */
@Entity(tableName = "game")
data class GameEntity(
    @PrimaryKey val id: Int,
    val slug: String = "",
    val title: String = "",
    val description: String = "",
    val tagline: String = "",
    val shortDescription: String = "",
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
