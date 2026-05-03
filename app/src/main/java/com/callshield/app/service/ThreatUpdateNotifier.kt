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

object ThreatUpdateNotifier {

    private const val CHANNEL_ID   = "threat_update"
    private const val CHANNEL_NAME = "Tehdit Listesi Güncellemeleri"
    private const val NOTIF_ID     = 9002

    fun notify(context: Context, newCount: Int) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Fraud listesi güncellendiğinde bildirim" }
            nm.createNotificationChannel(channel)
        }

        val intent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🛡️ Tehdit Listesi Güncellendi")
            .setContentText("$newCount yeni tehdit tespit edildi ve engellendi")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(intent)
            .build()

        nm.notify(NOTIF_ID, notification)
    }
}
