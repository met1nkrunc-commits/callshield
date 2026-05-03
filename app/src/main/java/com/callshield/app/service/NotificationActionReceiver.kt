package com.callshield.app.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.callshield.app.domain.repository.BlockedNumberRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ActionReceiverEntryPoint {
        fun blockedNumberRepository(): BlockedNumberRepository
    }

    companion object {
        const val ACTION_BLOCK  = "com.callshield.action.BLOCK_NUMBER"
        const val EXTRA_NUMBER  = "extra_number"
        const val EXTRA_NOTIF_ID = "extra_notif_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_BLOCK) return
        val number  = intent.getStringExtra(EXTRA_NUMBER)  ?: return
        val notifId = intent.getIntExtra(EXTRA_NOTIF_ID, CallWarningNotifier.NOTIF_ID)

        val repo = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ActionReceiverEntryPoint::class.java,
        ).blockedNumberRepository()

        CoroutineScope(Dispatchers.IO).launch {
            repo.addNumber(com.callshield.app.domain.model.BlockedNumber(phoneNumber = number))
        }

        // Bildirimi kapat
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(notifId)
    }
}
