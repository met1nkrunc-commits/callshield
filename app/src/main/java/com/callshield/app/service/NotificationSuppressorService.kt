package com.callshield.app.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.bengel.shared.ml.TurkishPatternMatcher
import com.callshield.app.domain.model.RiskLevel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class NotificationSuppressorService : NotificationListenerService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface NotificationSuppressorEntryPoint {
        fun turkishPatternMatcher(): TurkishPatternMatcher
    }

    companion object {
        // Fix #13: Türkiye'de yaygın markaların SMS uygulamaları eklendi.
        private val SMS_PACKAGES = setOf(
            "com.android.mms",
            "com.google.android.apps.messaging",
            "com.samsung.android.messaging",
            "com.miui.sms",                     // Xiaomi MIUI
            "com.xiaomi.smsmms",                // Xiaomi alternatif
            "com.vivo.mms",                     // Vivo
            "com.coloros.mms",                  // Oppo / OnePlus
            "com.oppo.mms",                     // Oppo eski
            "com.huawei.hidatalink",            // Huawei
            "com.hihonor.android.mms",          // Honor
            "com.lge.message",                  // LG
            "com.motorola.messaging",           // Motorola
        )
    }

    private val entryPoint by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            NotificationSuppressorEntryPoint::class.java,
        )
    }

    private val matcher by lazy { entryPoint.turkishPatternMatcher() }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in SMS_PACKAGES) return

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return
        val text  = extras.getString(Notification.EXTRA_TEXT)  ?: return

        val result = matcher.analyze(text, title)

        // Sadece bildirimi gizle — block event ve karantina kaydı SmsReceiver tarafından yapılır.
        if (result.riskLevel == RiskLevel.BLOCKED || result.riskLevel == RiskLevel.HIGH) {
            cancelNotification(sbn.key)
        }
    }
}
