package com.bengel.shared.domain.repository

import com.bengel.shared.domain.model.BlockEvent
import kotlinx.coroutines.flow.Flow

interface BlockEventRepository {
    fun getAll(): Flow<List<BlockEvent>>
    suspend fun save(event: BlockEvent)
    suspend fun getCount(): Int
    suspend fun getCountSince(timestamp: Long): Int
}
