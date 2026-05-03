package com.callshield.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.callshield.app.data.local.db.entity.NumberQueryCacheEntity

@Dao
interface NumberQueryCacheDao {

    @Query("SELECT * FROM number_query_cache WHERE normalizedNumber = :number LIMIT 1")
    suspend fun getByNumber(number: String): NumberQueryCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NumberQueryCacheEntity)

    @Query("DELETE FROM number_query_cache WHERE cachedAt < :cutoff")
    suspend fun deleteExpired(cutoff: Long)

    @Query("SELECT COUNT(*) FROM number_query_cache")
    suspend fun count(): Int

    @Query("DELETE FROM number_query_cache")
    suspend fun deleteAll()
}
