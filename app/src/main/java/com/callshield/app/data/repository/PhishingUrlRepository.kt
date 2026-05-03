package com.callshield.app.data.repository

import com.callshield.app.data.local.db.dao.PhishingUrlDao
import com.callshield.app.data.local.db.entity.PhishingUrlEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhishingUrlRepository @Inject constructor(
    private val dao: PhishingUrlDao,
) {
    fun getAll(): Flow<List<PhishingUrlEntity>> = dao.getAll()

    suspend fun insert(url: String, sender: String, snippet: String) {
        dao.insert(PhishingUrlEntity(url = url, sender = sender, snippet = snippet))
    }

    suspend fun pruneOld() {
        val cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        dao.deleteOlderThan(cutoff)
    }
}
