package com.callshield.app.data.repository

import com.callshield.app.data.local.db.dao.TrustedNumberDao
import com.callshield.app.data.local.db.entity.TrustedNumberEntity
import com.callshield.app.domain.repository.TrustedNumber
import com.callshield.app.domain.repository.TrustedNumberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrustedNumberRepositoryImpl @Inject constructor(
    private val dao: TrustedNumberDao,
) : TrustedNumberRepository {

    override fun getAll(): Flow<List<TrustedNumber>> =
        dao.getAll().map { list ->
            list.map { TrustedNumber(it.phoneNumber, it.label, it.addedAt) }
        }

    override suspend fun add(phoneNumber: String, label: String) {
        dao.insert(TrustedNumberEntity(phoneNumber = phoneNumber, label = label))
    }

    override suspend fun remove(phoneNumber: String) {
        dao.delete(phoneNumber)
    }

    override suspend fun isTrusted(phoneNumber: String): Boolean =
        dao.isTrusted(phoneNumber)
}
