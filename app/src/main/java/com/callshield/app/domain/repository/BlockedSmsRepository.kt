package com.callshield.app.domain.repository

import com.callshield.app.domain.model.BlockedSms
import kotlinx.coroutines.flow.Flow

interface BlockedSmsRepository {
    suspend fun save(sms: BlockedSms)
    fun getAll(): Flow<List<BlockedSms>>
    suspend fun deleteById(id: Long)
}
