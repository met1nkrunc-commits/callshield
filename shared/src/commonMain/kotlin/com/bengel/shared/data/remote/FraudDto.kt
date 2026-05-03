package com.bengel.shared.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FraudNumbersResponse(
    @SerialName("version")          val version: Int,
    @SerialName("updated_at")       val updatedAt: String,
    @SerialName("numbers")          val numbers: List<FraudNumberDto>,
    @SerialName("keywords_version") val keywordsVersion: Int = 1,
    @SerialName("extra_keywords")   val extraKeywords: ExtraKeywordsDto? = null,
)

@Serializable
data class FraudNumberDto(
    @SerialName("number")       val number: String,
    @SerialName("risk_level")   val riskLevel: String,
    @SerialName("category")     val category: String,
    @SerialName("report_count") val reportCount: Int = 1,
    @SerialName("note")         val note: String? = null,
)

@Serializable
data class ExtraKeywordsDto(
    @SerialName("betting")  val betting: List<String>  = emptyList(),
    @SerialName("phishing") val phishing: List<String> = emptyList(),
    @SerialName("legal")    val legal: List<String>    = emptyList(),
    @SerialName("social")   val social: List<String>   = emptyList(),
)
