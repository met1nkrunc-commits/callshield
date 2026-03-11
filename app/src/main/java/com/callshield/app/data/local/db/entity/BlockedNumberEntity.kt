package com.callshield.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.callshield.app.domain.model.BlockedNumber

@Entity(tableName = "blocked_numbers")
data class BlockedNumberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val reason: String,
    val blockedAt: Long,
    val isManual: Boolean,
) {
    fun toBlockedNumber(): BlockedNumber = BlockedNumber(
        id = id,
        phoneNumber = phoneNumber,
        label = reason.ifBlank { null },
        isManual = isManual,
        createdAt = blockedAt,
    )
}
