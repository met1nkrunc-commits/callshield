package com.callshield.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phishing_urls")
data class PhishingUrlEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val sender: String,
    val snippet: String,   // first 80 chars of SMS
    val detectedAt: Long = System.currentTimeMillis(),
)
