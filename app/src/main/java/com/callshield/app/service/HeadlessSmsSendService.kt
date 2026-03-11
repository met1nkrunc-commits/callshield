package com.callshield.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Required stub service for default SMS app eligibility.
 * Android requires this to exist and be exported; actual sending
 * is handled by the system when the user picks a different default.
 */
class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
