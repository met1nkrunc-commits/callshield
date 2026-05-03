package com.callshield.app.domain.repository

import com.callshield.app.domain.model.BlockedNumber
import kotlinx.coroutines.flow.Flow

interface BlockedNumberRepository {
    suspend fun addNumber(blockedNumber: BlockedNumber)
    suspend fun upsert(blockedNumber: BlockedNumber)
    suspend fun removeNumber(phoneNumber: String)
    fun getAll(): Flow<List<BlockedNumber>>
    suspend fun isBlocked(phoneNumber: String): Boolean
}
