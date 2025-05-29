package com.example.kurrencyconvert.model

/**
 * Modèle de données pour la réponse de l'API de taux de change
 */
data class ExchangeRateResponse(
    val base: String = "",
    val date: String = "",
    val rates: Map<String, Double> = emptyMap(),
    val time_last_updated: Long = 0
)

/**
 * Modèle de données pour la réponse de conversion
 */
data class ConversionResponse(
    val success: Boolean = false,
    val date: String = "",
    val result: Double = 0.0,
    // Champs additionnels pour faciliter l'utilisation
    val sourceRate: Double = 0.0,
    val sourceCurrency: String = "",
    val targetCurrency: String = "",
    val amount: Double = 0.0
)

/**
 * Modèle de données pour l'enregistrement des conversions dans Firebase
 */
data class ConversionRecord(
    val timestamp: String = formatTimestamp(System.currentTimeMillis()),
    val source: String = "",
    val target: String = "",
    val amount: Double = 0.0,
    val result: Double = 0.0
)

/**
 * Formate un timestamp en date lisible
 */
fun formatTimestamp(timestamp: Long): String {
    val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())
    return dateFormat.format(java.util.Date(timestamp))
}
