package com.example.kurrencyconvert.model

/**
 * Modèle de données pour la requête de conversion
 */
data class ConversionRequest(
    val from: String,
    val to: String,
    val amount: Double
)

/**
 * Modèle de données pour la réponse de l'API de conversion
 */
data class ConversionResponse(
    val success: Boolean,
    val query: Query,
    val info: Info,
    val date: String,
    val result: Double
)

data class Query(
    val from: String,
    val to: String,
    val amount: Double
)

data class Info(
    val rate: Double
)

/**
 * Modèle de données pour l'enregistrement des conversions dans Firebase
 */
data class ConversionRecord(
    val timestamp: Long = System.currentTimeMillis(),
    val source: String = "",
    val target: String = "",
    val amount: Double = 0.0,
    val result: Double = 0.0
)
