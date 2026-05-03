package com.callshield.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trusted_numbers")
data class TrustedNumberEntity(
    @PrimaryKey val phoneNumber: String,
    val label: String = "",
    val addedAt: Long = System.currentTimeMillis(),
)
