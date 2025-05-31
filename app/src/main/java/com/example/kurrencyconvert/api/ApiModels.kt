package com.example.kurrencyconvert.api

/**
 * Modèle de données pour la réponse de l'API exchangerate.host
 * Version locale au package api pour éviter les problèmes de référence
 */
data class ConvertResponse(
    val success: Boolean = false,
    val query: Query = Query(),
    val info: Info = Info(),
    val historical: Boolean = false,
    val date: String = "",
    val result: Double = 0.0,
    val motd: Motd? = null,
    val error: Error? = null
)

data class Query(
    val from: String = "",
    val to: String = "",
    val amount: Double = 0.0
)

data class Info(
    val rate: Double = 0.0,
    val timestamp: Long? = null
)

data class Motd(
    val msg: String = "",
    val url: String = ""
)

data class Error(
    val code: Int = 0,
    val info: String = ""
)
