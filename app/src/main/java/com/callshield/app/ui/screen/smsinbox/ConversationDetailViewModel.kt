package com.callshield.app.ui.screen.smsinbox

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.telephony.SmsManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _messages = MutableStateFlow<List<SmsMessage>>(emptyList())
    val messages: StateFlow<List<SmsMessage>> = _messages

    private val _draft = MutableStateFlow("")
    val draft: StateFlow<String> = _draft

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending

    private val _sendError = MutableStateFlow<String?>(null)
    val sendError: StateFlow<String?> = _sendError

    private var currentThreadId: Long = -1

    fun load(threadId: Long) {
        currentThreadId = threadId
        viewModelScope.launch(Dispatchers.IO) {
            fetchMessages(threadId)
            markRead(threadId)
        }
    }

    fun updateDraft(text: String) { _draft.value = text }

    fun clearSendError() { _sendError.value = null }

    fun send(address: String) {
        val body = _draft.value.trim()
        if (body.isBlank() || _isSending.value) return
        _isSending.value = true
        _draft.value = ""
        viewModelScope.launch(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                SmsManager.getDefault().sendTextMessage(address, null, body, null, null)

                // DB'ye gönderilen mesajı yaz
                val values = ContentValues().apply {
                    put(Telephony.Sms.ADDRESS, address)
                    put(Telephony.Sms.BODY, body)
                    put(Telephony.Sms.DATE, System.currentTimeMillis())
                    put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
                    put(Telephony.Sms.READ, 1)
                    if (currentThreadId != -1L) put(Telephony.Sms.THREAD_ID, currentThreadId)
                }
                context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)
                if (currentThreadId != -1L) fetchMessages(currentThreadId)
            } catch (e: Exception) {
                _sendError.value = "Mesaj gönderilemedi: ${e.message ?: "bilinmeyen hata"}"
            } finally {
                _isSending.value = false
            }
        }
    }

    private fun fetchMessages(threadId: Long) {
        val cursor = context.contentResolver.query(
            Uri.parse("content://sms"),
            arrayOf("_id", "thread_id", "address", "body", "date", "type", "read"),
            "thread_id=?", arrayOf(threadId.toString()), "date ASC",
        ) ?: return

        val list = mutableListOf<SmsMessage>()
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    SmsMessage(
                        id       = it.getLong(it.getColumnIndexOrThrow("_id")),
                        threadId = it.getLong(it.getColumnIndexOrThrow("thread_id")),
                        address  = it.getString(it.getColumnIndexOrThrow("address")).orEmpty(),
                        body     = it.getString(it.getColumnIndexOrThrow("body")).orEmpty(),
                        date     = it.getLong(it.getColumnIndexOrThrow("date")),
                        type     = it.getInt(it.getColumnIndexOrThrow("type")),
                        read     = it.getInt(it.getColumnIndexOrThrow("read")) == 1,
                    )
                )
            }
        }
        _messages.value = list
    }

    private fun markRead(threadId: Long) {
        try {
            context.contentResolver.update(
                Uri.parse("content://sms"),
                ContentValues().apply { put("read", 1) },
                "thread_id=? AND read=0", arrayOf(threadId.toString()),
            )
        } catch (_: Exception) {}
    }
}
