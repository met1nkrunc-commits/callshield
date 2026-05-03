package com.callshield.app.ui.screen.smsinbox

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bengel.shared.ml.TurkishPatternMatcher
import com.callshield.app.domain.model.RiskLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val matcher: TurkishPatternMatcher,
) : ViewModel() {

    private val _threads = MutableStateFlow<List<ConversationThread>>(emptyList())
    val threads: StateFlow<List<ConversationThread>> = _threads

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init { loadThreads() }

    fun loadThreads() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val cursor = context.contentResolver.query(
                    Uri.parse("content://sms"),
                    arrayOf("thread_id", "address", "body", "date", "read"),
                    null, null, "date DESC",
                ) ?: return@launch

                val seen    = mutableSetOf<Long>()
                val threads = mutableListOf<ConversationThread>()

                cursor.use {
                    while (it.moveToNext()) {
                        val threadId = it.getLong(it.getColumnIndexOrThrow("thread_id"))
                        if (!seen.add(threadId)) continue

                        val address = it.getString(it.getColumnIndexOrThrow("address")).orEmpty()
                        val body    = it.getString(it.getColumnIndexOrThrow("body")).orEmpty()
                        val date    = it.getLong(it.getColumnIndexOrThrow("date"))
                        val read    = it.getInt(it.getColumnIndexOrThrow("read")) == 1

                        val result = matcher.analyze(body, address)
                        threads.add(
                            ConversationThread(
                                threadId   = threadId,
                                address    = address,
                                snippet    = body,
                                date       = date,
                                unreadCount = if (read) 0 else 1,
                                riskLevel  = result.riskLevel,
                            )
                        )
                    }
                }
                _threads.value = threads
            } catch (_: SecurityException) {
                _threads.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
