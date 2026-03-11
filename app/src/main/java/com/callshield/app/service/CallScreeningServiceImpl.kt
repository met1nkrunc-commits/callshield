package com.callshield.app.service

import android.telecom.Call
import android.telecom.CallScreeningService
import com.callshield.app.domain.usecase.call.CheckNumberUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CallScreeningServiceImpl : CallScreeningService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CallScreeningEntryPoint {
        fun checkNumberUseCase(): CheckNumberUseCase
    }

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle
            ?.schemeSpecificPart   // strips the "tel:" prefix from the Uri
            .orEmpty()

        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            CallScreeningEntryPoint::class.java,
        )
        val checkNumber = entryPoint.checkNumberUseCase()

        // respondToCall() must be invoked within 5 seconds of onScreenCall().
        // The Room query is a single indexed lookup — well within the deadline on any device.
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            val result = checkNumber(phoneNumber, isCall = true)
            val isBlocked = result.riskLevel == com.callshield.app.domain.model.RiskLevel.BLOCKED

            val response = if (isBlocked) {
                CallResponse.Builder()
                    .setRejectCall(true)
                    .setDisallowCall(true)
                    .setSkipCallLog(false)
                    .setSkipNotification(false)
                    .build()
            } else {
                CallResponse.Builder()
                    .setDisallowCall(false)
                    .setRejectCall(false)
                    .build()
            }

            respondToCall(callDetails, response)
        }
    }
}
