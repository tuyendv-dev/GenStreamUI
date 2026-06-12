package network.ermis.genstreamui.domain.model.entities

import androidx.room.Entity

/**
 * Index layout của một màn discovery/browse: trỏ tới [gameId] trong bảng game, giữ nguyên
 * nhóm ([bucket]/[category]) và thứ tự ([groupIndex]/[position]). Tách khỏi [GameEntity] để
 * thông tin game được dedup và truy vấn độc lập theo id.
 *
 * - [cacheKey]: định danh màn + ngôn ngữ, vd "discovery:en", "browse:steam:en".
 * - [bucket]: nhóm cấp 1 — [BUCKET_FEATURED]/[BUCKET_HOT]/[BUCKET_RECOMMENDED]/[BUCKET_SECTION].
 * - [groupIndex]: thứ tự section (0 cho 3 nhóm đầu, vì chúng chỉ có một nhóm).
 * - [position]: thứ tự game trong nhóm.
 * - [category]: tên section ("" nếu không phải section).
 */
@Entity(
    tableName = "discovery_ref",
    primaryKeys = ["cacheKey", "bucket", "groupIndex", "position"]
)
data class DiscoveryRefEntity(
    val cacheKey: String,
    val bucket: String,
    val groupIndex: Int,
    val position: Int,
    val category: String = "",
    val gameId: Int,
    val updatedAt: Long
) {
    companion object {
        const val BUCKET_FEATURED = "featured"
        const val BUCKET_HOT = "hot"
        const val BUCKET_RECOMMENDED = "recommended"
        const val BUCKET_SECTION = "section"
    }
}
