package network.ermis.genstreamui.database.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import network.ermis.genstreamui.database.storage.dao.RecentGameDao
import network.ermis.genstreamui.domain.model.entities.RecentGameEntity

/**
 * Room database khung tối thiểu cho parity với GenPlayAndroid.
 * Bổ sung entity/DAO mới vào đây khi cần lưu trữ cục bộ.
 */
@Database(
    entities = [RecentGameEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentGameDao(): RecentGameDao
}
