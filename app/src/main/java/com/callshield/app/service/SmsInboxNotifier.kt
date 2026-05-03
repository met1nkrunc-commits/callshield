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

object SmsInboxNotifier {

    private const val CHANNEL_ID   = "sms_inbox"
    private const val CHANNEL_NAME = "Gelen SMS Bildirimleri"

    fun notify(context: Context, sender: String, body: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Gelen SMS mesajları için bildirimler"
                enableVibration(true)
            }
            nm.createNotificationChannel(channel)
        }

        val openIntent = PendingIntent.getActivity(
            context,
            sender.hashCode(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val displaySender = sender.ifBlank { "Bilinmeyen" }
        val snippet       = body.take(80).replace('\n', ' ')

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(displaySender)
            .setContentText(snippet)
            .setStyle(NotificationCompat.BigTextStyle().bigText(snippet))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .build()

        // Her göndericiye özgü ID → aynı kişiden gelen mesajlar üst üste binmez, güncellenir.
        nm.notify(sender.hashCode(), notification)
    }
}
