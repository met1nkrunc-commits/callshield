package com.callshield.app.domain.repository

import kotlinx.coroutines.flow.Flow

data class TrustedNumber(
    val phoneNumber: String,
    val label: String,
    val addedAt: Long,
)

interface TrustedNumberRepository {
    fun getAll(): Flow<List<TrustedNumber>>
    suspend fun add(phoneNumber: String, label: String)
    suspend fun remove(phoneNumber: String)
    suspend fun isTrusted(phoneNumber: String): Boolean
}
