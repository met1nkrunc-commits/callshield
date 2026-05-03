package com.callshield.app.data.repository

import com.bengel.shared.data.local.BlockedNumberLocalDataSource
import com.callshield.app.data.local.db.dao.BlockedNumberDao
import com.callshield.app.data.local.db.entity.BlockedNumberEntity
import com.callshield.app.domain.model.BlockedNumber
import com.callshield.app.domain.repository.BlockedNumberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BlockedNumberRepositoryImpl @Inject constructor(
    private val dao: BlockedNumberDao,
) : BlockedNumberRepository, BlockedNumberLocalDataSource {

    override suspend fun addNumber(blockedNumber: BlockedNumber) {
        dao.insert(blockedNumber.toEntity())
    }

    override suspend fun upsert(blockedNumber: BlockedNumber) {
        dao.deleteAutoByPhoneNumber(blockedNumber.phoneNumber)
        dao.insert(blockedNumber.toEntity())
    }

    override suspend fun removeNumber(phoneNumber: String) {
        dao.deleteByPhoneNumber(phoneNumber)
    }

    override fun getAll(): Flow<List<BlockedNumber>> =
        dao.getAll().map { list -> list.map { it.toBlockedNumber() } }

    override suspend fun isBlocked(phoneNumber: String): Boolean =
        dao.isBlocked(phoneNumber)

    // BlockedNumberLocalDataSource
    override suspend fun insert(number: BlockedNumber) = addNumber(number)
    override suspend fun delete(number: BlockedNumber) = removeNumber(number.phoneNumber)
    override suspend fun deleteAutoNumbers() {
        dao.getAll().first()
            .filter { !it.isManual }
            .forEach { dao.deleteAutoByPhoneNumber(it.phoneNumber) }
    }
}

private fun BlockedNumber.toEntity(): BlockedNumberEntity = BlockedNumberEntity(
    id          = id,
    phoneNumber = phoneNumber,
    reason      = label ?: "",
    blockedAt   = createdAt,
    isManual    = isManual,
    // Fix #15: riskLevel ve source artık entity'ye yazılıyor.
    riskLevel   = riskLevel?.name ?: "UNKNOWN",
    source      = source ?: "manual",
)
