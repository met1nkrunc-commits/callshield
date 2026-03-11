package com.callshield.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.callshield.app.data.local.db.entity.BlockEventEntity
import kotlinx.coroutines.flow.Flow

data class CategoryCount(val category: String, val count: Int)

@Dao
interface BlockEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: BlockEventEntity)

    @Query("SELECT * FROM block_events ORDER BY timestamp DESC")
    fun getAll(): Flow<List<BlockEventEntity>>

    @Query("SELECT * FROM block_events WHERE type = :type ORDER BY timestamp DESC")
    fun getByType(type: String): Flow<List<BlockEventEntity>>

    @Query("SELECT * FROM block_events WHERE timestamp >= :timestamp ORDER BY timestamp DESC")
    fun getAfterTimestamp(timestamp: Long): Flow<List<BlockEventEntity>>

    @Query("SELECT category, COUNT(*) as count FROM block_events GROUP BY category")
    fun getCategoryStats(): Flow<List<CategoryCount>>
}
