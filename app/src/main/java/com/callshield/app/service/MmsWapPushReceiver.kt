package com.callshield.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * ROLE_SMS için zorunlu WAP Push (MMS) stub receiver.
 * Callshield MMS işlemiyor; bu sınıf yalnızca varsayılan SMS uygulaması
 * rolü için Android'in zorunlu kıldığı bileşeni sağlamak amacıyla var.
 */
class MmsWapPushReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // MMS işleme yok — sadece ROLE_SMS uygunluğu için gerekli.
    }
}
