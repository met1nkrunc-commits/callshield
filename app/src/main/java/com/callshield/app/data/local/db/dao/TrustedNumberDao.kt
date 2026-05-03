package com.callshield.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.callshield.app.data.local.db.entity.TrustedNumberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrustedNumberDao {

    @Query("SELECT * FROM trusted_numbers ORDER BY addedAt DESC")
    fun getAll(): Flow<List<TrustedNumberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TrustedNumberEntity)

    @Query("DELETE FROM trusted_numbers WHERE phoneNumber = :phoneNumber")
    suspend fun delete(phoneNumber: String)

    @Query("SELECT EXISTS(SELECT 1 FROM trusted_numbers WHERE phoneNumber = :phoneNumber)")
    suspend fun isTrusted(phoneNumber: String): Boolean
}
