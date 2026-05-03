package com.callshield.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.callshield.app.MainActivity
import com.callshield.app.R

/**
 * MEDIUM riskli SMS'ler için sarı/dikkat düzeyinde bildirim.
 * Mesajı engellemez — kullanıcıyı şüpheli içerik konusunda uyarır.
 */
object SmsWarningNotifier {

    private const val CHANNEL_ID   = "sms_warning"
    private const val CHANNEL_NAME = "Şüpheli SMS Uyarıları"

    fun notifyMedium(context: Context, sender: String, snippet: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply { description = "Şüpheli içerik tespit edilen SMS'ler için uyarılar" }
            )
        }

        val openIntent = PendingIntent.getActivity(
            context,
            sender.hashCode(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val displaySender = sender.ifBlank { "Bilinmeyen" }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🟡 Şüpheli SMS — $displaySender")
            .setContentText(snippet)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$snippet\n\nBu mesaj şüpheli içerik barındırıyor olabilir.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .build()

        // Her göndericiye özgü ID → aynı kişiden gelen mesajlar birleştirilir.
        nm.notify("sms_warning_${sender.hashCode()}".hashCode(), notification)
    }
}
