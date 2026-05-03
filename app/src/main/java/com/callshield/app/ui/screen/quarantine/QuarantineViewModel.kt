package com.callshield.app.ui.screen.quarantine

import android.content.ContentValues
import android.content.Context
import android.provider.Telephony
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callshield.app.domain.model.BlockedSms
import com.callshield.app.domain.repository.BlockedSmsRepository
import com.callshield.app.domain.repository.TrustedNumberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuarantineViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val blockedSmsRepository: BlockedSmsRepository,
    private val trustedNumberRepository: TrustedNumberRepository,
) : ViewModel() {

    val messages: StateFlow<List<BlockedSms>> = blockedSmsRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Spam değildi: güvenilir listeye ekle + gelen kutusuna geri yaz + karantinadan sil. */
    fun markAsNotSpam(sms: BlockedSms) {
        viewModelScope.launch {
            trustedNumberRepository.add(phoneNumber = sms.sender, label = "")
            restoreToInbox(sms)
            blockedSmsRepository.deleteById(sms.id)
        }
    }

    /** Karantinadan kalıcı olarak sil (spam olarak onayla). */
    fun deleteFromQuarantine(sms: BlockedSms) {
        viewModelScope.launch {
            blockedSmsRepository.deleteById(sms.id)
        }
    }

    private fun restoreToInbox(sms: BlockedSms) {
        try {
            val values = ContentValues().apply {
                put(Telephony.Sms.ADDRESS,   sms.sender)
                put(Telephony.Sms.BODY,      sms.body)
                put(Telephony.Sms.DATE,      sms.timestamp)
                put(Telephony.Sms.DATE_SENT, sms.timestamp)
                put(Telephony.Sms.READ,      0)
                put(Telephony.Sms.SEEN,      0)
                put(Telephony.Sms.TYPE,      Telephony.Sms.MESSAGE_TYPE_INBOX)
            }
            context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)
        } catch (_: Exception) { }
    }
}
