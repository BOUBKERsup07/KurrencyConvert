package com.example.kurrencyconvert.api

// Utilisation de la classe locale au package api
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Interface Retrofit pour l'API de conversion de devises
 */
interface ExchangeRateApi {
    @GET("{base}")
    suspend fun getExchangeRates(
        @Path("base") baseCurrency: String
    ): Response<ExchangeRateResponse>
}
