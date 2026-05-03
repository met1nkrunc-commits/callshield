package com.callshield.app.data.repository

import com.callshield.app.data.local.db.dao.BlockedSmsDao
import com.callshield.app.data.local.db.entity.toEntity
import com.callshield.app.domain.model.BlockedSms
import com.callshield.app.domain.repository.BlockedSmsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BlockedSmsRepositoryImpl @Inject constructor(
    private val dao: BlockedSmsDao,
) : BlockedSmsRepository {

    override suspend fun save(sms: BlockedSms) {
        dao.insert(sms.toEntity())
    }

    override fun getAll(): Flow<List<BlockedSms>> =
        dao.getAll().map { list -> list.map { it.toBlockedSms() } }

    override suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }
}
