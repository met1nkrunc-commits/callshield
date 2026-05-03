package com.callshield.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.callshield.app.data.local.db.entity.PhishingUrlEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhishingUrlDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: PhishingUrlEntity)

    @Query("SELECT * FROM phishing_urls ORDER BY detectedAt DESC LIMIT 100")
    fun getAll(): Flow<List<PhishingUrlEntity>>

    @Query("DELETE FROM phishing_urls WHERE detectedAt < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    @Query("DELETE FROM phishing_urls")
    suspend fun deleteAll()
}
