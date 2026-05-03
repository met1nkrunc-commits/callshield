package com.bengel.shared.domain.model

data class BlockedNumber(
    val id: Long = 0,
    val phoneNumber: String,
    val label: String? = null,
    val riskLevel: RiskLevel = RiskLevel.BLOCKED,
    val isManual: Boolean = true,
    val source: String = "user",
    val createdAt: Long = 0L,
)
