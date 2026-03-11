package com.bengel.shared.domain.model

data class BlockEvent(
    val id: Long = 0,
    val timestamp: Long,
    val type: String,           // "SMS" / "CALL"
    val riskLevel: RiskLevel,
    val category: String? = null,
    val senderHash: String,
)
