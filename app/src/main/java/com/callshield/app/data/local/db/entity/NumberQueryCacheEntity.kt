package com.callshield.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "number_query_cache")
data class NumberQueryCacheEntity(
    @PrimaryKey
    val normalizedNumber: String,       // E.164 format: +905001234567
    val spamScore: Int,                 // 0-100, IPQS score
    val fraudScore: Int,                // 0-100, IPQS fraud score
    val isSpam: Boolean,
    val isFraud: Boolean,
    val carrier: String? = null,
    val lineType: String? = null,       // mobile, landline, voip
    val cachedAt: Long = System.currentTimeMillis(),
    val source: String = "ipqs",        // ipqs / local / github
)
