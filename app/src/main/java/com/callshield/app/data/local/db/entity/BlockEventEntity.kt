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
    val senderHash: Int,    // sender.hashCode()
    val sender: String = "", // gerçek numara/ID — false positive için gerekli
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
        sender = sender,
    )
}

fun BlockEvent.toEntity() = BlockEventEntity(
    id         = id,
    timestamp  = timestamp,
    type       = type,
    riskLevel  = when (riskLevel) {
        RiskLevel.BLOCKED -> "BLOCKED"
        else              -> "HIGH"
    },
    category   = category ?: "UNKNOWN",
    // Fix #3: senderHash String(hex) → Int dönüşümü: parse başarısız olursa
    // sender string'inden yeniden hesapla — 0 yazılmak yerine doğru değer korunur.
    senderHash = senderHash.toIntOrNull(16)
        ?: sender.hashCode().and(0xFFFFFF),
    sender     = sender,
)
