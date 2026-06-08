package network.ermis.genstreamui.database.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import network.ermis.genstreamui.domain.model.entities.RecentGameEntity

/**
 * DAO mẫu: suspend cho mutation, Flow cho query reactive. Pattern port từ GenPlayAndroid.
 */
@Dao
interface RecentGameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RecentGameEntity)

    @Update
    suspend fun update(entity: RecentGameEntity)

    @Delete
    suspend fun delete(entity: RecentGameEntity)

    @Query("SELECT * FROM recent_game ORDER BY lastPlayedAt DESC")
    fun getAll(): Flow<List<RecentGameEntity>>

    @Query("DELETE FROM recent_game")
    suspend fun clear()
}
