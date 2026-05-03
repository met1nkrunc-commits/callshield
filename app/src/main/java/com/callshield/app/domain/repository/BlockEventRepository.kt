package com.callshield.app.domain.repository

import com.callshield.app.domain.model.BlockEvent
import kotlinx.coroutines.flow.Flow

interface BlockEventRepository {
    suspend fun save(event: BlockEvent)
    fun getAll(): Flow<List<BlockEvent>>
    fun getWeeklyEvents(): Flow<List<BlockEvent>>
    fun getCategoryStats(): Flow<List<Pair<String, Int>>>
}
