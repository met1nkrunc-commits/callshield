package com.callshield.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.callshield.app.domain.model.BlockEvent
import com.callshield.app.domain.model.RiskLevel

@Entity(tableName = "block_events")
data class BlockEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val type: String,       // "SMS" or "CALL"
    val riskLevel: String,  // "HIGH" or "BLOCKED"
    val category: String,   // "BETTING", "LEGAL", "PHISHING", "SOCIAL", "UNKNOWN"
    val senderHash: Int,    // sender.hashCode() — no real number stored
) {
    fun toBlockEvent() = BlockEvent(
        id = id,
        timestamp = timestamp,
        type = type,
        riskLevel = when (riskLevel) {
            "BLOCKED" -> RiskLevel.BLOCKED
            else      -> RiskLevel.HIGH
        },
        category = category,
        senderHash = "%06X".format(senderHash.and(0xFFFFFF)),
    )
}

fun BlockEvent.toEntity() = BlockEventEntity(
    id = id,
    timestamp = timestamp,
    type = type,
    riskLevel = when (riskLevel) {
        RiskLevel.BLOCKED -> "BLOCKED"
        else              -> "HIGH"
    },
    category = category ?: "UNKNOWN",
    senderHash = senderHash.toIntOrNull(16) ?: 0,
)
