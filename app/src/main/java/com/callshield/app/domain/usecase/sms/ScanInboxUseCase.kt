package com.callshield.app.domain.usecase.sms

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.bengel.shared.ml.TurkishPatternMatcher
import com.callshield.app.domain.model.RiskLevel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class SmsMessage(
    val id: Long,
    val address: String,    // sender
    val body: String,
    val date: Long,
    val threadId: Long,
)

data class ScannedSms(
    val message: SmsMessage,
    val riskLevel: RiskLevel,
    val category: String,
    val matchedKeywords: List<String>,
)

class ScanInboxUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val matcher: TurkishPatternMatcher,
) {
    /** Returns ALL inbox messages with their risk analysis, sorted newest first. */
    fun execute(limit: Int = 200): List<ScannedSms> {
        val messages = readSmsInbox(context.contentResolver, limit)
        return messages.map { msg ->
            val result = matcher.analyze(msg.body, msg.address)
            val category = matcher.detectCategory(result.matchedPatterns, result.hasPhishingUrl)
            ScannedSms(
                message = msg,
                riskLevel = result.riskLevel,
                category = category,
                matchedKeywords = result.matchedPatterns.filterNot { it == "suspicious_url" }.take(3),
            )
        }
    }

    private fun readSmsInbox(cr: ContentResolver, limit: Int): List<SmsMessage> {
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("_id", "address", "body", "date", "thread_id")
        val messages = mutableListOf<SmsMessage>()
        try {
            cr.query(uri, projection, null, null, "date DESC LIMIT $limit")?.use { cursor ->
                val idIdx      = cursor.getColumnIndexOrThrow("_id")
                val addrIdx    = cursor.getColumnIndexOrThrow("address")
                val bodyIdx    = cursor.getColumnIndexOrThrow("body")
                val dateIdx    = cursor.getColumnIndexOrThrow("date")
                val threadIdx  = cursor.getColumnIndexOrThrow("thread_id")
                while (cursor.moveToNext()) {
                    messages.add(
                        SmsMessage(
                            id       = cursor.getLong(idIdx),
                            address  = cursor.getString(addrIdx) ?: "",
                            body     = cursor.getString(bodyIdx) ?: "",
                            date     = cursor.getLong(dateIdx),
                            threadId = cursor.getLong(threadIdx),
                        )
                    )
                }
            }
        } catch (_: Exception) { /* READ_SMS not granted */ }
        return messages
    }
}
