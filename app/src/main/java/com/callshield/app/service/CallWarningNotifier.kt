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
import com.callshield.app.domain.model.RiskLevel

object CallWarningNotifier {

    private const val CHANNEL_ID   = "call_warning"
    private const val CHANNEL_NAME = "Şüpheli Arama Uyarıları"
    const val NOTIF_ID             = 9001

    fun notify(context: Context, phoneNumber: String, riskLevel: RiskLevel, reason: String?) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Şüpheli aramalar için anlık uyarılar"
                enableVibration(true)
            }
            nm.createNotificationChannel(channel)
        }

        val openIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val (emoji, title) = when (riskLevel) {
            RiskLevel.BLOCKED -> "🔴" to "Arama Engellendi"
            RiskLevel.HIGH    -> "🟠" to "Şüpheli Arama Geliyor"
            RiskLevel.MEDIUM  -> "🟡" to "Dikkat: Şüpheli Numara"
            else              -> "🟢" to "Bilinmeyen Numara"
        }

        val display = if (phoneNumber.isBlank()) "Gizli Numara" else phoneNumber

        // "Engelle" hızlı eylem butonu
        val blockIntent = android.app.PendingIntent.getBroadcast(
            context,
            phoneNumber.hashCode(),
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_BLOCK
                putExtra(NotificationActionReceiver.EXTRA_NUMBER, phoneNumber)
                putExtra(NotificationActionReceiver.EXTRA_NOTIF_ID, NOTIF_ID)
            },
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("$emoji $title")
            .setContentText(display)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$display\n${reason ?: "Dolandırıcı numara listesinde"}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_delete, "Engelle", blockIntent)
            .build()

        nm.notify(NOTIF_ID, notification)
    }
}
