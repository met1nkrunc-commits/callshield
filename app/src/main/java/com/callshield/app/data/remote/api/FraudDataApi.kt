package com.callshield.app.data.remote.api

import com.callshield.app.data.remote.dto.FraudNumbersResponse
import retrofit2.http.GET
import retrofit2.http.Url

interface FraudDataApi {
    // Pass the full raw.githubusercontent.com URL — @Url overrides Retrofit's base URL
    @GET
    suspend fun getFraudNumbers(@Url url: String): FraudNumbersResponse
}
