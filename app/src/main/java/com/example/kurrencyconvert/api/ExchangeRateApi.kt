package com.example.kurrencyconvert.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface Retrofit pour l'API de conversion de devises
 */
interface ExchangeRateApi {
    @GET("convert")
    suspend fun convertCurrency(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("amount") amount: Double,
        @Query("access_key") accessKey: String = RetrofitClient.API_KEY
    ): Response<ConvertResponse>
}
