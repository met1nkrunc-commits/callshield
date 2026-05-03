package com.callshield.app.data.repository

import com.bengel.shared.data.local.BlockEventLocalDataSource
import com.bengel.shared.domain.model.RiskLevel
import com.callshield.app.data.local.db.dao.BlockEventDao
import com.callshield.app.data.local.db.entity.toEntity
import com.callshield.app.domain.model.BlockEvent
import com.callshield.app.domain.repository.BlockEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BlockEventRepositoryImpl @Inject constructor(
    private val dao: BlockEventDao,
) : BlockEventRepository, BlockEventLocalDataSource {

    override suspend fun save(event: BlockEvent) {
        dao.insert(event.toEntity())
    }

    override fun getAll(): Flow<List<BlockEvent>> =
        dao.getAll().map { list -> list.map { it.toBlockEvent() } }

    override fun getWeeklyEvents(): Flow<List<BlockEvent>> {
        val sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        return dao.getAfterTimestamp(sevenDaysAgo).map { list -> list.map { it.toBlockEvent() } }
    }

    override fun getCategoryStats(): Flow<List<Pair<String, Int>>> =
        dao.getCategoryStats().map { list -> list.map { it.category to it.count } }

    // BlockEventLocalDataSource
    override suspend fun insert(event: BlockEvent) = save(event)

    // Fix #6: Tüm tabloyu belleğe çekip .size yerine doğrudan SQL COUNT kullan.
    override suspend fun getCount(): Int = dao.countAll()

    override suspend fun getCountSince(timestamp: Long): Int = dao.countAfterTimestamp(timestamp)

    override suspend fun getCategoryStatsMap(): Map<String, Int> =
        dao.getCategoryStats().first().associate { it.category to it.count }

    override suspend fun getRiskStats(): Map<RiskLevel, Int> =
        dao.getAll().first()
            .groupBy { it.riskLevel }
            .mapKeys { (key, _) ->
                when (key) {
                    "BLOCKED" -> RiskLevel.BLOCKED
                    "HIGH"    -> RiskLevel.HIGH
                    "MEDIUM"  -> RiskLevel.MEDIUM
                    "LOW"     -> RiskLevel.LOW
                    else      -> RiskLevel.SAFE
                }
            }
            .mapValues { (_, v) -> v.size }
}
