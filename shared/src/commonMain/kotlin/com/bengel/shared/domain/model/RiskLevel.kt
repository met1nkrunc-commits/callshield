package com.bengel.shared.domain.model

enum class RiskLevel(val storedName: String) {
    SAFE("SAFE"),
    LOW("LOW"),
    MEDIUM("MEDIUM"),
    HIGH("HIGH"),
    BLOCKED("BLOCKED");

    companion object {
        fun fromStoredName(name: String): RiskLevel =
            entries.firstOrNull { it.storedName == name } ?: SAFE
    }
}
