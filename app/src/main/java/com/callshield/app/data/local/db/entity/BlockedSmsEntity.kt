package com.callshield.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.callshield.app.domain.model.BlockedSms
import com.callshield.app.domain.model.RiskLevel

@Entity(tableName = "blocked_sms")
data class BlockedSmsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val body: String,
    val timestamp: Long,
    val category: String,
    val riskLevel: String,
) {
    fun toBlockedSms() = BlockedSms(
        id        = id,
        sender    = sender,
        body      = body,
        timestamp = timestamp,
        category  = category,
        riskLevel = when (riskLevel) {
            "BLOCKED" -> RiskLevel.BLOCKED
            "HIGH"    -> RiskLevel.HIGH
            else      -> RiskLevel.HIGH
        },
    )
}

fun BlockedSms.toEntity() = BlockedSmsEntity(
    id        = id,
    sender    = sender,
    body      = body,
    timestamp = timestamp,
    category  = category,
    riskLevel = when (riskLevel) {
        RiskLevel.BLOCKED -> "BLOCKED"
        else              -> "HIGH"
    },
)
