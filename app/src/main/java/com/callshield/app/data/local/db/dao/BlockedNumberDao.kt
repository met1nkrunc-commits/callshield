package com.callshield.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.callshield.app.data.local.db.entity.BlockedNumberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedNumberDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BlockedNumberEntity)

    @Delete
    suspend fun delete(entity: BlockedNumberEntity)

    @Query("SELECT * FROM blocked_numbers ORDER BY blockedAt DESC")
    fun getAll(): Flow<List<BlockedNumberEntity>>

    @Query("SELECT COUNT(*) > 0 FROM blocked_numbers WHERE phoneNumber = :phoneNumber")
    suspend fun isBlocked(phoneNumber: String): Boolean

    @Query("DELETE FROM blocked_numbers WHERE phoneNumber = :phoneNumber")
    suspend fun deleteByPhoneNumber(phoneNumber: String)

    // Deletes only auto-synced entries (isManual = 0) — preserves user-added entries on upsert
    @Query("DELETE FROM blocked_numbers WHERE phoneNumber = :phoneNumber AND isManual = 0")
    suspend fun deleteAutoByPhoneNumber(phoneNumber: String)

    @Query("SELECT * FROM blocked_numbers WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun findByNumber(phoneNumber: String): BlockedNumberEntity?
}
