package com.callshield.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.callshield.app.data.local.db.entity.BlockedSmsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedSmsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BlockedSmsEntity)

    @Query("SELECT * FROM blocked_sms ORDER BY timestamp DESC")
    fun getAll(): Flow<List<BlockedSmsEntity>>

    @Query("DELETE FROM blocked_sms WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM blocked_sms")
    suspend fun count(): Int

    @Query("DELETE FROM blocked_sms")
    suspend fun deleteAll()
}
