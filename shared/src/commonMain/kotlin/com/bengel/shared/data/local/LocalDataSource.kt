package com.bengel.shared.data.local

import com.bengel.shared.domain.model.BlockedNumber
import com.bengel.shared.domain.model.BlockEvent
import com.bengel.shared.domain.model.RiskLevel
import kotlinx.coroutines.flow.Flow

interface BlockedNumberLocalDataSource {
    fun getAll(): Flow<List<BlockedNumber>>
    suspend fun isBlocked(phoneNumber: String): Boolean
    suspend fun insert(number: BlockedNumber)
    suspend fun delete(number: BlockedNumber)
    suspend fun upsert(number: BlockedNumber)
    suspend fun deleteAutoNumbers()
}

interface BlockEventLocalDataSource {
    fun getAll(): Flow<List<BlockEvent>>
    suspend fun insert(event: BlockEvent)
    suspend fun getCount(): Int
    suspend fun getCountSince(timestamp: Long): Int
    suspend fun getCategoryStatsMap(): Map<String, Int>
    suspend fun getRiskStats(): Map<RiskLevel, Int>
}
