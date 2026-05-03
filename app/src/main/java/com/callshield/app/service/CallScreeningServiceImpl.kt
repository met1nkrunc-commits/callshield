package com.callshield.app.service

import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.callshield.app.core.util.dataStore
import com.callshield.app.domain.model.RiskLevel
import com.callshield.app.domain.usecase.call.CheckNumberUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class CallScreeningServiceImpl : CallScreeningService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CallScreeningEntryPoint {
        fun checkNumberUseCase(): CheckNumberUseCase
    }

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle
            ?.schemeSpecificPart
            .orEmpty()

        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            CallScreeningEntryPoint::class.java,
        )
        val checkNumber = entryPoint.checkNumberUseCase()

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            // Fix #7: ARCHITECTURE.md → "respondToCall() max 5 saniye içinde çağrılmalı".
            // IPQS ağ isteği 15s+30s timeout'a sahip — withTimeout(4_500) ile güvence altına alınıyor.
            val response = try {
                withTimeout(4_500L) {
                    val result = checkNumber(phoneNumber, isCall = true)

                    val prefs     = applicationContext.dataStore.data.first()
                    val blockHigh = prefs[booleanPreferencesKey("block_high_risk")]       ?: false
                    val notifsOn  = prefs[booleanPreferencesKey("notifications_enabled")] ?: true

                    when (result.riskLevel) {
                        RiskLevel.BLOCKED -> {
                            if (notifsOn) CallWarningNotifier.notify(applicationContext, phoneNumber, result.riskLevel, result.reason)
                            CallResponse.Builder()
                                .setRejectCall(true)
                                .setDisallowCall(true)
                                .setSkipCallLog(false)
                                .setSkipNotification(false)
                                .build()
                        }
                        RiskLevel.HIGH, RiskLevel.MEDIUM -> {
                            if (notifsOn) CallWarningNotifier.notify(applicationContext, phoneNumber, result.riskLevel, result.reason)
                            if (result.riskLevel == RiskLevel.HIGH && blockHigh) {
                                CallResponse.Builder()
                                    .setRejectCall(true)
                                    .setDisallowCall(true)
                                    .setSkipCallLog(false)
                                    .setSkipNotification(false)
                                    .build()
                            } else {
                                InCallOverlayService.start(applicationContext, phoneNumber, result.riskLevel.name, result.reason)
                                CallResponse.Builder()
                                    .setDisallowCall(false)
                                    .setRejectCall(false)
                                    .build()
                            }
                        }
                        else -> CallResponse.Builder()
                            .setDisallowCall(false)
                            .setRejectCall(false)
                            .build()
                    }
                }
            } catch (_: TimeoutCancellationException) {
                // Timeout → fail-open: aramanın geçmesine izin ver, asılı kalma.
                CallResponse.Builder().setDisallowCall(false).setRejectCall(false).build()
            } catch (_: Exception) {
                // Analiz başarısız olsa bile aramayı geçir.
                CallResponse.Builder().setDisallowCall(false).setRejectCall(false).build()
            }

            respondToCall(callDetails, response)
        }
    }
}
