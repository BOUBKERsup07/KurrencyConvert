package com.example.kurrencyconvert.repository

import com.example.kurrencyconvert.api.ExchangeRateResponse
import com.example.kurrencyconvert.api.RetrofitClient
import com.example.kurrencyconvert.model.ConversionRecord
import com.example.kurrencyconvert.model.ConversionResponse
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository pour gérer les conversions de devises et l'interaction avec Firebase
 */
class ConversionRepository {
    private val exchangeRateApi = RetrofitClient.exchangeRateApi
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val conversionsRef = firebaseDatabase.getReference("conversions")
    
    /**
     * Convertit une devise en une autre via l'API
     */
    suspend fun convertCurrency(from: String, to: String, amount: Double): Result<ConversionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Obtenir les taux de change pour la devise source
                val response = exchangeRateApi.getExchangeRates(from)
                
                if (response.isSuccessful && response.body() != null) {
                    val exchangeRateResponse = response.body()!!
                    val rates = exchangeRateResponse.rates
                    
                    // Vérifier si la devise cible existe dans les taux
                    if (rates.containsKey(to)) {
                        val rate = rates[to]!!
                        val result = amount * rate
                        
                        // Créer une réponse de conversion
                        val conversionResponse = ConversionResponse(
                            success = true,
                            date = exchangeRateResponse.date,
                            result = result,
                            sourceRate = rate,
                            sourceCurrency = from,
                            targetCurrency = to,
                            amount = amount
                        )
                        
                        Result.success(conversionResponse)
                    } else {
                        Result.failure(Exception("Devise cible non trouvée dans les taux de change"))
                    }
                } else {
                    Result.failure(Exception("Erreur lors de la conversion: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Sauvegarde une conversion dans Firebase Realtime Database
     */
    suspend fun saveConversion(conversionRecord: ConversionRecord): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val key = conversionsRef.push().key ?: return@withContext Result.failure(Exception("Impossible de générer une clé"))
                conversionsRef.child(key).setValue(conversionRecord).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
