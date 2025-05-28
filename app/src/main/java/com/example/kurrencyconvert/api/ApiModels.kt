package com.example.kurrencyconvert.api

/**
 * Modèle de données pour la réponse de l'API de taux de change
 * Version locale au package api pour éviter les problèmes de référence
 */
data class ExchangeRateResponse(
    val base: String = "",
    val date: String = "",
    val rates: Map<String, Double> = emptyMap(),
    val time_last_updated: Long = 0
)
