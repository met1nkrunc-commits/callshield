package com.callshield.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FraudNumbersResponse(
    @SerializedName("version")          val version: Int,
    @SerializedName("updated_at")       val updatedAt: String,
    @SerializedName("numbers")          val numbers: List<FraudNumberDto>,
    @SerializedName("keywords_version") val keywordsVersion: Int = 1,
    @SerializedName("extra_keywords")   val extraKeywords: ExtraKeywordsDto? = null,
)

data class FraudNumberDto(
    @SerializedName("number")       val number: String,
    @SerializedName("risk_level")   val riskLevel: String,
    @SerializedName("category")     val category: String,
    @SerializedName("report_count") val reportCount: Int = 1,
    @SerializedName("note")         val note: String? = null,
)

data class ExtraKeywordsDto(
    @SerializedName("betting")  val betting: List<String>  = emptyList(),
    @SerializedName("phishing") val phishing: List<String> = emptyList(),
    @SerializedName("legal")    val legal: List<String>    = emptyList(),
    @SerializedName("social")   val social: List<String>   = emptyList(),
)
