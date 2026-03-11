package com.callshield.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.bengel.shared.core.TurkishFraudConstants
import com.callshield.app.domain.usecase.sms.AnalyzeSmsContentUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class SmsReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SmsReceiverEntryPoint {
        fun analyzeSmsContentUseCase(): AnalyzeSmsContentUseCase
    }

    // Compiled once per instance; BroadcastReceiver is re-instantiated per broadcast.
    private val btkCodeRegex = Regex("""B\d{3,4}""")

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return

        // Merge multipart SMS: group message segments by sender, then concatenate bodies.
        val grouped: Map<String, String> = messages
            .groupBy { it.originatingAddress.orEmpty() }
            .mapValues { (_, parts) -> parts.joinToString("") { it.messageBody.orEmpty() } }

        // abortBroadcast() must be called synchronously before any coroutine is launched.
        // Lightweight BTK-code pre-check: confirmed spam is suppressed immediately so the
        // default SMS app never sees it. NotificationSuppressorService handles all notifications.
        val shouldAbort = grouped.any { (sender, body) ->
            val isTrusted = TurkishFraudConstants.TRUSTED_SENDER_IDS
                .any { it.equals(sender, ignoreCase = true) }
            !isTrusted && btkCodeRegex.containsMatchIn(body)
        }
        if (shouldAbort) abortBroadcast()

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            SmsReceiverEntryPoint::class.java,
        )
        val analyzeUseCase = entryPoint.analyzeSmsContentUseCase()

        // Full analysis — result is available for future use (e.g. logging, ML feedback).
        // Notifications are intentionally not posted here; NotificationSuppressorService
        // intercepts the SMS app's notification and replaces it with a CallShield warning.
        grouped.forEach { (sender, body) ->
            analyzeUseCase(sender, body)
        }
    }
}
