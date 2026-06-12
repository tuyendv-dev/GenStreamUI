package network.ermis.genstreamui.database.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import network.ermis.genstreamui.domain.model.entities.DiscoveryRefEntity

/**
 * DAO cho bảng index layout discovery_ref. Việc ghi đè trọn vẹn một cacheKey (xoá cũ + ghi mới)
 * được gói trong một transaction ở repository qua RoomDatabase.withTransaction.
 */
@Dao
interface DiscoveryRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(refs: List<DiscoveryRefEntity>)

    @Query("SELECT * FROM discovery_ref WHERE cacheKey = :cacheKey")
    suspend fun getByKey(cacheKey: String): List<DiscoveryRefEntity>

    @Query("DELETE FROM discovery_ref WHERE cacheKey = :cacheKey")
    suspend fun clearByKey(cacheKey: String)
}
