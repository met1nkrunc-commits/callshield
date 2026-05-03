package com.bengel.shared.domain.repository

import com.bengel.shared.domain.model.BlockedNumber
import kotlinx.coroutines.flow.Flow

interface BlockedNumberRepository {
    fun getAll(): Flow<List<BlockedNumber>>
    suspend fun isBlocked(phoneNumber: String): Boolean
    suspend fun insert(blockedNumber: BlockedNumber)
    suspend fun delete(blockedNumber: BlockedNumber)
    suspend fun upsert(blockedNumber: BlockedNumber)
}
