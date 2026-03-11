package com.callshield.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface IpqsApi {
    @GET("phone/{apiKey}/{phoneNumber}")
    suspend fun checkNumber(
        @Path("apiKey")      apiKey: String,
        @Path("phoneNumber") phoneNumber: String,
        @Query("country[]")  country: String = "TR",
        @Query("strictness") strictness: Int = 1,
    ): IpqsPhoneResponse
}

data class IpqsPhoneResponse(
    @SerializedName("success")     val success: Boolean,
    @SerializedName("spam_score")  val spamScore: Int = 0,
    @SerializedName("fraud_score") val fraudScore: Int = 0,
    @SerializedName("spammer")     val isSpammer: Boolean = false,
    @SerializedName("fraud")       val isFraud: Boolean = false,
    @SerializedName("carrier")     val carrier: String? = null,
    @SerializedName("line_type")   val lineType: String? = null,
    @SerializedName("message")     val message: String? = null,
)
