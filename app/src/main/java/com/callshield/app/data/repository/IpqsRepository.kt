package com.callshield.app.data.repository

import android.util.Log
import com.callshield.app.BuildConfig
import com.callshield.app.core.billing.PlanManager
import com.callshield.app.core.util.BillingConstants
import com.callshield.app.data.local.db.dao.NumberQueryCacheDao
import com.callshield.app.data.local.db.entity.NumberQueryCacheEntity
import com.callshield.app.data.remote.IpqsApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IpqsRepository @Inject constructor(
    private val api: IpqsApi,
    private val cacheDao: NumberQueryCacheDao,
    private val planManager: PlanManager,
) {

    companion object {
        private const val TAG          = "IpqsRepository"
        private const val CACHE_TTL_MS = 30L * 24 * 60 * 60 * 1000 // 30 days
    }

    /** Normalize to E.164-ish for cache key: strip spaces/dashes, keep leading '+'. */
    fun normalizeNumber(raw: String): String {
        val cleaned = raw.filter { it.isDigit() || it == '+' }
        return if (cleaned.startsWith("+")) cleaned
        else {
            val digits = cleaned.trimStart('0')
            // Assume TR if 10-digit number without country code
            if (digits.length == 10) "+90$digits" else cleaned
        }
    }

    /**
     * Cache-first IPQS lookup.
     * Free plan users are skipped — keyword engine is sufficient for them.
     * Returns null on API failure (fail-open — caller treats as SAFE).
     */
    suspend fun queryNumber(phoneNumber: String): NumberQueryCacheEntity? {
        // Free plan: skip IPQS entirely
        val plan = planManager.currentPlan.first()
        if (plan == BillingConstants.PLAN_FREE) {
            Log.d(TAG, "Free plan — skipping IPQS lookup")
            return null
        }

        val normalized = normalizeNumber(phoneNumber)

        // Cache hit — valid within TTL
        val cached = cacheDao.getByNumber(normalized)
        if (cached != null && System.currentTimeMillis() - cached.cachedAt < CACHE_TTL_MS) {
            return cached
        }

        // Cache miss — call IPQS
        return try {
            val apiKey = BuildConfig.IPQS_API_KEY
            android.util.Log.d("IPQS", "Key loaded, length: ${apiKey.length}")
            if (apiKey.isBlank()) {
                Log.w(TAG, "IPQS_API_KEY not set — skipping lookup")
                return null
            }

            val response = api.checkNumber(apiKey, normalized)
            if (!response.success) {
                Log.w(TAG, "IPQS returned success=false: ${response.message}")
                return null
            }

            val entity = NumberQueryCacheEntity(
                normalizedNumber = normalized,
                spamScore        = response.spamScore,
                fraudScore       = response.fraudScore,
                isSpam           = response.isSpammer,
                isFraud          = response.isFraud,
                carrier          = response.carrier,
                lineType         = response.lineType,
            )
            cacheDao.insert(entity)
            entity
        } catch (e: Exception) {
            Log.w(TAG, "IPQS query failed for $normalized: ${e.message}")
            null
        }
    }

    /** Delete cache entries older than TTL — call from background worker. */
    suspend fun pruneCache() {
        val cutoff = System.currentTimeMillis() - CACHE_TTL_MS
        cacheDao.deleteExpired(cutoff)
        Log.d(TAG, "Cache pruned (cutoff=${cutoff})")
    }
}
