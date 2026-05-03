package com.bengel.shared.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class FraudRemoteRepository(private val client: HttpClient) {

    suspend fun fetchFraudNumbers(url: String): FraudNumbersResponse =
        client.get(url).body()

    suspend fun fetchFraudNumbersSafe(url: String): FraudNumbersResponse? =
        try {
            fetchFraudNumbers(url)
        } catch (e: Exception) {
            println("FraudRemoteRepository: fetch failed — ${e.message}")
            null
        }

    companion object {
        fun create(): FraudRemoteRepository = FraudRemoteRepository(createHttpClient())
    }
}
