package com.callshield.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.callshield.app.MainActivity
import com.callshield.app.R
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.callshield.app.core.util.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class CallShieldWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val protectionKey = booleanPreferencesKey("protection_enabled")
        val isEnabled = runBlocking {
            context.dataStore.data.map { it[protectionKey] ?: true }.first()
        }

        val views = RemoteViews(context.packageName, R.layout.widget_callshield).apply {
            setTextViewText(
                R.id.widget_status,
                if (isEnabled) "Koruma Aktif" else "Koruma Devre Dışı",
            )
            setTextViewText(R.id.widget_icon, if (isEnabled) "🛡️" else "⚠️")

            // Tap → open app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            setOnClickPendingIntent(R.id.widget_icon, pendingIntent)
        }

        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
