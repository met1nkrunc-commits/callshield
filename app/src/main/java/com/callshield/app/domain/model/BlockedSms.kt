package com.callshield.app.domain.model

data class BlockedSms(
    val id: Long = 0,
    val sender: String,
    val body: String,
    val timestamp: Long,
    val category: String,
    val riskLevel: RiskLevel,
)
