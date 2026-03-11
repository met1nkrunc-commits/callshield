package com.bengel.shared.domain.model

data class AnalysisResult(
    val riskLevel: RiskLevel,
    val category: String? = null,
    val reason: String? = null,
    val score: Int = 0,
)
