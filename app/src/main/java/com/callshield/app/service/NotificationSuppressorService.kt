package com.callshield.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.bengel.shared.ml.PatternMatchResult
import com.bengel.shared.ml.TurkishPatternMatcher
import com.callshield.app.MainActivity
import com.callshield.app.domain.model.BlockEvent
import com.callshield.app.domain.model.RiskLevel
import com.callshield.app.domain.repository.BlockEventRepository
import com.callshield.app.ui.theme.toLabel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationSuppressorService : NotificationListenerService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface NotificationSuppressorEntryPoint {
        fun turkishPatternMatcher(): TurkishPatternMatcher
        fun blockEventRepository(): BlockEventRepository
    }

    companion object {
        private val SMS_PACKAGES = setOf(
            "com.android.mms",
            "com.google.android.apps.messaging",
            "com.samsung.android.messaging",
            "com.miui.sms",
        )
        private const val CHANNEL_ID = "callshield_sms_alerts"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val notificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    private val entryPoint by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            NotificationSuppressorEntryPoint::class.java,
        )
    }

    private val matcher by lazy { entryPoint.turkishPatternMatcher() }
    private val blockEventRepository by lazy { entryPoint.blockEventRepository() }

    override fun onListenerConnected() {
        super.onListenerConnected()
        ensureChannel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in SMS_PACKAGES) return

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return
        val text = extras.getString(Notification.EXTRA_TEXT) ?: return

        val result = matcher.analyze(text, title)

        if (result.riskLevel == RiskLevel.BLOCKED ||
            result.riskLevel == RiskLevel.HIGH ||
            result.riskLevel == RiskLevel.MEDIUM) {
            cancelNotification(sbn.key)

            val category = matcher.detectCategory(result.matchedPatterns, result.hasPhishingUrl)
            showWarningNotification(title, result, category)

            scope.launch {
                blockEventRepository.save(
                    BlockEvent(
                        id = 0,
                        timestamp = System.currentTimeMillis(),
                        type = "SMS",
                        riskLevel = result.riskLevel,
                        category = category,
                        senderHash = "%06X".format(title.hashCode().and(0xFFFFFF)),
                    )
                )
            }
        }
    }

    private fun showWarningNotification(
        sender: String,
        result: PatternMatchResult,
        category: String,
    ) {
        ensureChannel()

        val riskEmoji = when (result.riskLevel) {
            RiskLevel.BLOCKED -> "🔴"
            RiskLevel.HIGH    -> "🟠"
            else              -> "🟡"
        }
        val categoryEmoji = when (category) {
            "BETTING"    -> "🎰"
            "PHISHING"   -> "🎣"
            "LEGAL"      -> "⚖️"
            "INVESTMENT" -> "💰"
            "SOCIAL"     -> "🎭"
            else         -> "⚠️"
        }
        val categoryLabel = when (category) {
            "BETTING"    -> "Bahis/Kumar"
            "PHISHING"   -> "Kimlik Avı"
            "LEGAL"      -> "Hukuki Tehdit"
            "INVESTMENT" -> "Yatırım Dolandırıcılığı"
            "SOCIAL"     -> "Sosyal Mühendislik"
            else         -> "Şüpheli İçerik"
        }

        val keywords = result.matchedPatterns
            .filterNot { it == "suspicious_url" }
            .take(3)
            .joinToString(", ")

        val tapIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val tapPending = PendingIntent.getActivity(
            applicationContext, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val bodyText = buildString {
            append("Gönderen: $sender · ${result.riskLevel.toLabel()}")
            if (keywords.isNotBlank()) append("\nTetikleyenler: $keywords")
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("$riskEmoji Engellendi — $categoryEmoji $categoryLabel")
            .setContentText("Gönderen: $sender · ${result.riskLevel.toLabel()}")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(tapPending)
            .build()

        notificationManager.notify(sender.hashCode(), notification)
    }

    private fun ensureChannel() {
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            notificationManager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "SMS Uyarıları", NotificationManager.IMPORTANCE_HIGH)
            )
        }
    }
}
