package com.example.kurrencyconvert.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kurrencyconvert.model.ConversionRecord
import com.example.kurrencyconvert.repository.ConversionRepository
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer les conversions de devises
 */
class ConversionViewModel : ViewModel() {
    private val repository = ConversionRepository()
    
    // État de chargement
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Résultat de la conversion
    private val _conversionResult = MutableLiveData<Double?>()
    val conversionResult: LiveData<Double?> = _conversionResult
    
    // Message d'erreur
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // État de sauvegarde Firebase
    private val _savingStatus = MutableLiveData<Boolean>()
    val savingStatus: LiveData<Boolean> = _savingStatus
    
    /**
     * Convertit une devise en une autre et sauvegarde le résultat dans Firebase
     */
    fun convertCurrency(from: String, to: String, amount: String) {
        // Vérification des entrées
        if (from.isEmpty() || to.isEmpty() || amount.isEmpty()) {
            _errorMessage.value = "Veuillez remplir tous les champs"
            return
        }
        
        val amountDouble = try {
            amount.toDouble()
        } catch (e: NumberFormatException) {
            _errorMessage.value = "Montant invalide"
            return
        }
        
        if (amountDouble <= 0) {
            _errorMessage.value = "Le montant doit être positif"
            return
        }
        
        // Réinitialisation des états
        _errorMessage.value = null
        _isLoading.value = true
        _conversionResult.value = null
        
        viewModelScope.launch {
            try {
                val result = repository.convertCurrency(from, to, amountDouble)
                
                result.onSuccess { response ->
                    // Vérifier que le résultat est valide avant de le sauvegarder
                    if (response.success && response.result > 0) {
                        _conversionResult.value = response.result
                        
                        // Sauvegarde dans Firebase
                        saveConversion(from, to, amountDouble, response.result)
                    } else {
                        _errorMessage.value = "Erreur: Résultat de conversion invalide"
                    }
                }
                
                result.onFailure { error ->
                    _errorMessage.value = "Erreur: ${error.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Sauvegarde une conversion dans Firebase
     */
    private fun saveConversion(from: String, to: String, amount: Double, result: Double) {
        // Ne pas sauvegarder si le résultat est 0 ou négatif
        if (result <= 0) {
            _errorMessage.value = "Erreur: Impossible de sauvegarder un résultat invalide"
            _savingStatus.value = false
            return
        }
        
        viewModelScope.launch {
            _savingStatus.value = true
            
            val conversionRecord = ConversionRecord(
                timestamp = System.currentTimeMillis(),
                source = from,
                target = to,
                amount = amount,
                result = result
            )
            
            try {
                repository.saveConversion(conversionRecord)
                    .onSuccess {
                        // Confirmation de sauvegarde réussie (optionnel)
                    }
                    .onFailure { error ->
                        _errorMessage.value = "Erreur lors de la sauvegarde: ${error.message}"
                    }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur lors de la sauvegarde: ${e.message}"
            } finally {
                _savingStatus.value = false
            }
        }
    }
    
    /**
     * Réinitialise les états
     */
    fun resetStates() {
        _errorMessage.value = null
        _conversionResult.value = null
    }
}
