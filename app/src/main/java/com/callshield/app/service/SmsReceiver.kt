package com.callshield.app.service

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.bengel.shared.core.TurkishFraudConstants
import com.callshield.app.data.repository.PhishingUrlRepository
import com.callshield.app.domain.model.BlockEvent
import com.callshield.app.domain.model.BlockedSms
import com.callshield.app.domain.model.RiskLevel
import com.callshield.app.domain.repository.BlockedSmsRepository
import com.callshield.app.domain.repository.BlockEventRepository
import com.callshield.app.domain.usecase.sms.AnalyzeSmsContentUseCase
import com.callshield.app.domain.usecase.sms.DetectBtkCodeUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SmsReceiverEntryPoint {
        fun analyzeSmsContentUseCase(): AnalyzeSmsContentUseCase
        // Fix #8: DetectBtkCodeUseCase artık use case üzerinden — ham regex kaldırıldı.
        fun detectBtkCodeUseCase(): DetectBtkCodeUseCase
        fun phishingUrlRepository(): PhishingUrlRepository
        fun blockedSmsRepository(): BlockedSmsRepository
        fun blockEventRepository(): BlockEventRepository
    }

    companion object {
        private val urlRegex = Regex("https?://[\\w\\-./?=&%+#@!~:]+")
    }

    // Fix #1: goAsync() → BroadcastReceiver'ın ömrünü uzatarak coroutine'in kesilmesini önler.
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                when (intent.action) {
                    Telephony.Sms.Intents.SMS_RECEIVED_ACTION -> handleSmsReceived(context, intent)
                    Telephony.Sms.Intents.SMS_DELIVER_ACTION  -> handleSmsDeliver(context, intent)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    // ── SMS_RECEIVED: analiz et, spam ise durdur ──────────────────────────────
    private suspend fun handleSmsReceived(context: Context, intent: Intent) {
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        val grouped = messages
            .groupBy { it.originatingAddress.orEmpty() }
            .mapValues { (_, parts) -> parts.joinToString("") { it.messageBody.orEmpty() } }

        val ep = EntryPointAccessors.fromApplication(
            context.applicationContext, SmsReceiverEntryPoint::class.java,
        )
        val analyzeUseCase  = ep.analyzeSmsContentUseCase()
        val btkUseCase      = ep.detectBtkCodeUseCase()
        val phishingUrlRepo = ep.phishingUrlRepository()
        val blockedSmsRepo  = ep.blockedSmsRepository()
        val blockEventRepo  = ep.blockEventRepository()

        var shouldAbort = false
        val riskyMessages  = mutableListOf<Triple<String, String, com.callshield.app.domain.model.AnalysisResult>>()
        val mediumMessages = mutableListOf<Pair<String, String>>()

        grouped.forEach { (sender, body) ->
            val isTrusted = TurkishFraudConstants.TRUSTED_SENDER_IDS
                .any { it.equals(sender, ignoreCase = true) }
            if (isTrusted) return@forEach

            val result = analyzeUseCase(sender, body)
            // Fix #8: BTK kodu tespiti use case üzerinden yapılıyor.
            val hasBtk = btkUseCase(body) != null

            if (result.riskLevel == RiskLevel.HIGH || result.riskLevel == RiskLevel.BLOCKED || hasBtk) {
                shouldAbort = true
            }

            when (result.riskLevel) {
                RiskLevel.HIGH, RiskLevel.BLOCKED -> riskyMessages.add(Triple(sender, body, result))
                // Fix #9: MEDIUM riskli SMS'leri kaydedip bildiririz ama engellemeyiz.
                RiskLevel.MEDIUM -> mediumMessages.add(sender to body)
                else -> Unit
            }
        }

        // Spam broadcast'i durdur — varsayılan SMS uygulaması bu mesajı almayacak.
        if (shouldAbort) abortBroadcast()

        val now = System.currentTimeMillis()

        if (riskyMessages.isNotEmpty()) {
            riskyMessages.forEach { (sender, body, result) ->
                blockEventRepo.save(
                    BlockEvent(
                        timestamp  = now,
                        type       = "SMS",
                        riskLevel  = result.riskLevel,
                        category   = result.category ?: "UNKNOWN",
                        senderHash = "%06X".format(sender.hashCode().and(0xFFFFFF)),
                        sender     = sender,
                    )
                )
                blockedSmsRepo.save(
                    BlockedSms(
                        sender    = sender,
                        body      = body,
                        timestamp = now,
                        category  = result.category ?: "UNKNOWN",
                        riskLevel = result.riskLevel,
                    )
                )
                // Phishing URL'lerini kaydet.
                urlRegex.findAll(body).forEach { phishingUrlRepo.insert(it.value, sender, body.take(80)) }
            }

            // Fix #2: Sabit delay yerine retry loop — yavaş cihazlarda da güvenilir silme.
            riskyMessages.forEach { (sender, body, _) ->
                deleteSmsWithRetry(context, sender, body)
            }
        }

        // Fix #9: MEDIUM SMS → uyarı bildirimi göster, engellemez.
        mediumMessages.forEach { (sender, body) ->
            SmsWarningNotifier.notifyMedium(context, sender, body.take(80))
        }
    }

    // ── SMS_DELIVER: sadece Callshield varsayılan SMS uygulamasıyken gelir ───
    // HIGH/BLOCKED mesajlar DB'ye yazılmaz → hiçbir yerde görünmez.
    // MEDIUM mesajlar DB'ye yazılır, ancak uyarı etiketiyle bildirim gösterilir.
    // SAFE/LOW mesajlar normal şekilde DB'ye kaydedilir.
    private suspend fun handleSmsDeliver(context: Context, intent: Intent) {
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        val grouped = messages
            .groupBy { it.originatingAddress.orEmpty() }
            .mapValues { (_, parts) -> parts.joinToString("") { it.messageBody.orEmpty() } }

        val ep = EntryPointAccessors.fromApplication(
            context.applicationContext, SmsReceiverEntryPoint::class.java,
        )
        val analyzeUseCase  = ep.analyzeSmsContentUseCase()
        val btkUseCase      = ep.detectBtkCodeUseCase()
        val phishingUrlRepo = ep.phishingUrlRepository()
        val blockedSmsRepo  = ep.blockedSmsRepository()
        val blockEventRepo  = ep.blockEventRepository()

        val now = System.currentTimeMillis()
        grouped.forEach { (sender, body) ->
            val isTrusted = TurkishFraudConstants.TRUSTED_SENDER_IDS
                .any { it.equals(sender, ignoreCase = true) }
            val result = if (isTrusted) null else analyzeUseCase(sender, body)
            val hasBtk = btkUseCase(body) != null
            val isHighRisk = result != null &&
                (result.riskLevel == RiskLevel.HIGH || result.riskLevel == RiskLevel.BLOCKED)
            val isSpam = isHighRisk || hasBtk

            if (isSpam && result != null) {
                // Spam → DB'ye yazma; block event + karantina + phishing URL kaydet.
                blockEventRepo.save(
                    BlockEvent(
                        timestamp  = now,
                        type       = "SMS",
                        riskLevel  = result.riskLevel,
                        category   = result.category ?: "UNKNOWN",
                        senderHash = "%06X".format(sender.hashCode().and(0xFFFFFF)),
                        sender     = sender,
                    )
                )
                blockedSmsRepo.save(
                    BlockedSms(
                        sender    = sender,
                        body      = body,
                        timestamp = now,
                        category  = result.category ?: "UNKNOWN",
                        riskLevel = result.riskLevel,
                    )
                )
                urlRegex.findAll(body).forEach { phishingUrlRepo.insert(it.value, sender, body.take(80)) }
                return@forEach
            }

            // Temiz / MEDIUM → gelen kutusu DB'sine yaz.
            val values = ContentValues().apply {
                put(Telephony.Sms.ADDRESS,   sender)
                put(Telephony.Sms.BODY,      body)
                put(Telephony.Sms.DATE,      now)
                put(Telephony.Sms.DATE_SENT, now)
                put(Telephony.Sms.READ,      0)
                put(Telephony.Sms.SEEN,      0)
                put(Telephony.Sms.TYPE,      Telephony.Sms.MESSAGE_TYPE_INBOX)
            }
            try {
                context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)
            } catch (_: SecurityException) { }

            // Fix #9: Varsayılan SMS uygulaması olarak bildirim biz gösteriyoruz.
            if (result?.riskLevel == RiskLevel.MEDIUM) {
                SmsWarningNotifier.notifyMedium(context, sender, body.take(80))
            } else {
                SmsInboxNotifier.notify(context, sender, body)
            }
        }
    }

    /**
     * Fix #2: Sabit 400ms delay yerine retry mekanizması.
     * Default SMS uygulaması mesajı DB'ye yazana kadar kısa aralıklarla dener.
     * 3 deneme × 300ms = en fazla ~900ms; başarılı silmede erken çıkar.
     */
    private suspend fun deleteSmsWithRetry(context: Context, sender: String, body: String) {
        repeat(3) { attempt ->
            if (attempt > 0) delay(300L)
            try {
                val deleted = context.contentResolver.delete(
                    Telephony.Sms.CONTENT_URI,
                    "${Telephony.Sms.ADDRESS} = ? AND ${Telephony.Sms.BODY} = ?",
                    arrayOf(sender, body),
                )
                if (deleted > 0) return
            } catch (_: Exception) { }
        }
    }
}
