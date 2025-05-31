package com.example.kurrencyconvert.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Client Retrofit pour configurer les appels API
 */
object RetrofitClient {
    // URL de l'API exchangerate.host comme demandé par le professeur
    private const val BASE_URL = "https://api.exchangerate.host/"
    
    // Clé d'accès pour l'API
    const val API_KEY = "4d0a758322b47984a63fccf7a662ac7b"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()
    
    val exchangeRateApi: ExchangeRateApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExchangeRateApi::class.java)
    }
}
