package network.ermis.genstreamui.database.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import network.ermis.genstreamui.domain.model.entities.GameEntity

/**
 * DAO cho bảng game (kho thông tin game dùng chung). Upsert theo id để luôn giữ bản mới nhất,
 * và truy vấn lại thông tin game chỉ bằng id.
 */
@Dao
interface GameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(games: List<GameEntity>)

    @Query("SELECT * FROM game WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): GameEntity?

    @Query("SELECT * FROM game WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Int>): List<GameEntity>

    @Query("DELETE FROM game")
    suspend fun clear()
}
