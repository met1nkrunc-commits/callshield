package com.callshield.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat

class InCallOverlayService : Service() {

    companion object {
        const val EXTRA_NUMBER = "extra_number"
        const val EXTRA_RISK   = "extra_risk"
        const val EXTRA_REASON = "extra_reason"

        private const val CHANNEL_ID = "incall_overlay"
        private const val NOTIF_ID   = 9003

        fun start(context: Context, number: String, risk: String, reason: String?) {
            context.startForegroundService(
                Intent(context, InCallOverlayService::class.java).apply {
                    putExtra(EXTRA_NUMBER, number)
                    putExtra(EXTRA_RISK,   risk)
                    putExtra(EXTRA_REASON, reason ?: "Şüpheli numara")
                }
            )
        }
    }

    private var windowManager: WindowManager? = null
    private var overlayView: android.view.View? = null
    private val handler = Handler(Looper.getMainLooper())
    // Fix #10: Overlay güncellemesi için TextView referansları saklanıyor.
    private var titleTv: TextView? = null
    private var numberTv: TextView? = null
    private var reasonTv: TextView? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val number = intent?.getStringExtra(EXTRA_NUMBER) ?: return START_NOT_STICKY
        val risk   = intent.getStringExtra(EXTRA_RISK)   ?: "HIGH"
        val reason = intent.getStringExtra(EXTRA_REASON) ?: "Şüpheli numara"
        showOverlay(number, risk, reason)
        return START_STICKY
    }

    private fun showOverlay(number: String, risk: String, reason: String) {
        // Fix #10: Mevcut overlay varsa silently yoksaymak yerine içeriği güncelle.
        if (overlayView != null) {
            updateOverlayContent(number, risk, reason)
            return
        }
        if (!android.provider.Settings.canDrawOverlays(this)) { stopSelf(); return }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0; y = 0
        }

        val isHigh   = risk == "HIGH" || risk == "BLOCKED"
        val accentClr = if (isHigh) Color.parseColor("#B71C1C") else Color.parseColor("#E65100")
        val bgClr     = Color.parseColor("#1A1A2E")
        val dp        = resources.displayMetrics.density

        // ── Root card ─────────────────────────────────────────────────────────
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp16(dp), dp16(dp), dp16(dp), dp16(dp))
            background = GradientDrawable().apply {
                setColor(bgClr)
                cornerRadius = dp12(dp).toFloat()
                setStroke(dp2(dp), accentClr)
            }
            elevation = dp8(dp).toFloat()
            setOnClickListener { dismissOverlay() }
        }

        // ── Left strip ────────────────────────────────────────────────────────
        val strip = android.view.View(this).apply {
            background = GradientDrawable().apply {
                setColor(accentClr)
                cornerRadius = dp4(dp).toFloat()
            }
            layoutParams = LinearLayout.LayoutParams(dp4(dp), LinearLayout.LayoutParams.MATCH_PARENT)
                .also { it.marginEnd = dp12(dp) }
        }

        // ── Text column ───────────────────────────────────────────────────────
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val title = TextView(this).apply {
            text = if (isHigh) "⚠️  Şüpheli Arama" else "❗  Dikkat: Şüpheli Numara"
            setTextColor(Color.WHITE)
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
        }.also { titleTv = it }

        val numTv = TextView(this).apply {
            text = number.ifBlank { "Gizli Numara" }
            setTextColor(Color.parseColor("#AAAAAA"))
            textSize = 12f
            setPadding(0, dp4(dp), 0, 0)
        }.also { numberTv = it }

        val reasonView = TextView(this).apply {
            text = reason
            setTextColor(Color.parseColor("#888888"))
            textSize = 11f
            setPadding(0, dp2(dp), 0, 0)
        }.also { reasonTv = it }

        col.addView(title); col.addView(numTv); col.addView(reasonView)

        // ── Dismiss X ─────────────────────────────────────────────────────────
        val close = TextView(this).apply {
            text = "✕"
            setTextColor(Color.parseColor("#888888"))
            textSize = 16f
            setPadding(dp12(dp), 0, 0, 0)
            setOnClickListener { dismissOverlay() }
        }

        root.addView(strip); root.addView(col); root.addView(close)

        // Outer margin wrapper
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp16(dp), dp8(dp) + statusBarHeight(), dp16(dp), dp8(dp))
            addView(root)
        }

        overlayView = outer
        handler.post { windowManager?.addView(overlayView, params) }
        handler.postDelayed({ dismissOverlay() }, 10_000L)
    }

    /** Fix #10: Overlay açıkken yeni arama gelince içeriği güncelle. */
    private fun updateOverlayContent(number: String, risk: String, reason: String) {
        val isHigh = risk == "HIGH" || risk == "BLOCKED"
        handler.post {
            titleTv?.text  = if (isHigh) "⚠️  Şüpheli Arama" else "❗  Dikkat: Şüpheli Numara"
            numberTv?.text = number.ifBlank { "Gizli Numara" }
            reasonTv?.text = reason
        }
    }

    private fun dismissOverlay() {
        overlayView?.let {
            try { windowManager?.removeView(it) } catch (_: Exception) {}
            overlayView = null
        }
        titleTv = null; numberTv = null; reasonTv = null
        stopSelf()
    }

    override fun onDestroy() { dismissOverlay(); super.onDestroy() }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private fun dp2(dp: Float)  = (2  * dp).toInt()
    private fun dp4(dp: Float)  = (4  * dp).toInt()
    private fun dp8(dp: Float)  = (8  * dp).toInt()
    private fun dp12(dp: Float) = (12 * dp).toInt()
    private fun dp16(dp: Float) = (16 * dp).toInt()

    private fun statusBarHeight(): Int {
        val res = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (res > 0) resources.getDimensionPixelSize(res) else 0
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Arama Uyarısı", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Callshield — arama koruması aktif")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
}
