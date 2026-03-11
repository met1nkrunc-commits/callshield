package com.callshield.app.service

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.bengel.shared.data.remote.FraudRemoteRepository
import com.bengel.shared.domain.model.RiskLevel
import com.callshield.app.core.util.Constants
import com.callshield.app.data.repository.IpqsRepository
import com.callshield.app.domain.model.BlockedNumber
import com.callshield.app.domain.repository.BlockedNumberRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class NumberUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val fraudRemoteRepo: FraudRemoteRepository,
    private val repository: BlockedNumberRepository,
    private val ipqsRepository: IpqsRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val response = fraudRemoteRepo.fetchFraudNumbersSafe(Constants.GITHUB_RAW_URL)
            ?: return@withContext Result.retry()

        response.numbers.forEach { dto ->
            repository.upsert(
                BlockedNumber(
                    id          = 0,
                    phoneNumber = dto.number,
                    label       = dto.note ?: dto.category,
                    riskLevel   = RiskLevel.fromStoredName(dto.riskLevel),
                    isManual    = false,
                    source      = "github_v${response.version}",
                    createdAt   = System.currentTimeMillis(),
                )
            )
        }

        ipqsRepository.pruneCache()
        Log.i(TAG, "Updated ${response.numbers.size} numbers (v${response.version}, ${response.updatedAt})")
        Result.success()
    }

    companion object {
        const val WORK_NAME = "NumberUpdateWorker"
        private const val TAG = "NumberUpdateWorker"

        fun schedule(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<NumberUpdateWorker>(24, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
