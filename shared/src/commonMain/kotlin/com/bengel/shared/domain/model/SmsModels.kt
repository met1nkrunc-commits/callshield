package com.bengel.shared.domain.model

data class ConversationThread(
    val threadId: Long,
    val address: String,
    val snippet: String,
    val date: Long,
    val unreadCount: Int,
    val riskLevel: RiskLevel,
)

data class SmsMessage(
    val id: Long,
    val threadId: Long,
    val address: String,
    val body: String,
    val date: Long,
    val type: Int,  // 1 = gelen, 2 = giden
    val read: Boolean,
) {
    val isOutgoing get() = type == 2
}
