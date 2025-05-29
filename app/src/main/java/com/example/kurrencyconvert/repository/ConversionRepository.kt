package com.example.kurrencyconvert.repository

import com.example.kurrencyconvert.api.ExchangeRateResponse
import com.example.kurrencyconvert.api.RetrofitClient
import com.example.kurrencyconvert.model.ConversionRecord
import com.example.kurrencyconvert.model.ConversionResponse
import com.example.kurrencyconvert.model.formatTimestamp
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
    
    /**
     * Récupère toutes les conversions depuis Firebase
     */
    suspend fun getConversions(): Result<List<ConversionRecord>> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = conversionsRef.get().await()
                val conversions = mutableListOf<ConversionRecord>()
                
                // Utiliser une approche manuelle pour convertir les données Firebase
                // afin de gérer à la fois les anciens et les nouveaux formats de timestamp
                for (childSnapshot in snapshot.children) {
                    try {
                        // Extraire les valeurs manuellement
                        val source = childSnapshot.child("source").getValue(String::class.java) ?: ""
                        val target = childSnapshot.child("target").getValue(String::class.java) ?: ""
                        val amount = childSnapshot.child("amount").getValue(Double::class.java) ?: 0.0
                        val result = childSnapshot.child("result").getValue(Double::class.java) ?: 0.0
                        
                        // Gérer les deux formats de timestamp possibles
                        val timestamp = try {
                            // Essayer d'abord de récupérer comme String
                            val timestampStr = childSnapshot.child("timestamp").getValue(String::class.java)
                            if (timestampStr != null) {
                                timestampStr
                            } else {
                                // Si null, essayer de récupérer comme Long et le convertir en String
                                val timestampLong = childSnapshot.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()
                                formatTimestamp(timestampLong)
                            }
                        } catch (e: Exception) {
                            // En cas d'erreur, utiliser l'heure actuelle
                            formatTimestamp(System.currentTimeMillis())
                        }
                        
                        val conversion = ConversionRecord(
                            timestamp = timestamp,
                            source = source,
                            target = target,
                            amount = amount,
                            result = result
                        )
                        
                        conversions.add(conversion)
                    } catch (e: Exception) {
                        // Ignorer cet enregistrement et continuer avec le suivant
                        continue
                    }
                }
                
                // Trier par timestamp (du plus récent au plus ancien)
                val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())
                conversions.sortByDescending { 
                    try {
                        dateFormat.parse(it.timestamp)?.time ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                }
                
                Result.success(conversions)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
