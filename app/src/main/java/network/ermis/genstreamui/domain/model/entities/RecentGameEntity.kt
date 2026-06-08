package network.ermis.genstreamui.domain.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity Room mẫu (lịch sử game gần đây) để minh hoạ tầng lưu trữ.
 * Trường tags kiểu List<String> minh hoạ TypeConverters.
 */
@Entity(tableName = "recent_game")
data class RecentGameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String = "",
    val tags: List<String> = emptyList(),
    val lastPlayedAt: Long = 0L
)
