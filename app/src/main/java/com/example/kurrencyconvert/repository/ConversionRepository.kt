package com.example.kurrencyconvert.repository

import com.example.kurrencyconvert.api.ConvertResponse
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
                // Log des paramètres d'entrée
                android.util.Log.d("ConversionRepository", "Conversion demandée: $from -> $to, montant: $amount")
                android.util.Log.d("ConversionRepository", "Utilisation de la clé API: ${RetrofitClient.API_KEY}")
                
                // Appel direct à l'API de conversion avec la clé d'accès
                val response = exchangeRateApi.convertCurrency(from, to, amount)
                
                // Log de la réponse
                android.util.Log.d("ConversionRepository", "Réponse API: ${response.isSuccessful}, code: ${response.code()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val convertResponse = response.body()!!
                    
                    // Log du contenu de la réponse
                    android.util.Log.d("ConversionRepository", "Contenu réponse: success=${convertResponse.success}, result=${convertResponse.result}")
                    
                    // Vérifier que le résultat est valide
                    if (convertResponse.success && convertResponse.result > 0) {
                        // Créer une réponse de conversion à partir de la réponse de l'API
                        val conversionResponse = ConversionResponse(
                            success = true,
                            date = convertResponse.date,
                            result = convertResponse.result,
                            sourceRate = convertResponse.info.rate,
                            sourceCurrency = convertResponse.query.from,
                            targetCurrency = convertResponse.query.to,
                            amount = convertResponse.query.amount
                        )
                        
                        android.util.Log.d("ConversionRepository", "Conversion réussie: ${conversionResponse.result}")
                        Result.success(conversionResponse)
                    } else {
                        // Si la conversion a échoué, vérifier s'il y a un message d'erreur
                        val errorMessage = if (convertResponse.error != null) {
                            "Erreur API: ${convertResponse.error.info}"
                        } else {
                            "Résultat de conversion invalide"
                        }
                        android.util.Log.e("ConversionRepository", "Réponse API invalide: $errorMessage")
                        Result.failure(Exception(errorMessage))
                    }
                } else {
                    android.util.Log.e("ConversionRepository", "Erreur API: ${response.code()} ${response.message()}")
                    Result.failure(Exception("Erreur lors de la conversion: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                android.util.Log.e("ConversionRepository", "Exception lors de la conversion", e)
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
