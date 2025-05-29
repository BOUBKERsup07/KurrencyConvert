package com.example.kurrencyconvert.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kurrencyconvert.ui.theme.GoldAccent
import com.example.kurrencyconvert.ui.theme.PrimaryLight

/**
 * Composant pour afficher une carte de devise avec un dégradé de couleurs et des animations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyCard(
    currencyCode: String,
    currencyName: String,
    isSource: Boolean,
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
    availableCurrencies: List<String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300)
    )
    
    val gradientColors = if (isSource) {
        listOf(PrimaryLight, GoldAccent.copy(alpha = 0.7f))
    } else {
        listOf(GoldAccent.copy(alpha = 0.7f), PrimaryLight)
    }
    
    val cardColor by animateColorAsState(
        targetValue = if (expanded) 
            MaterialTheme.colorScheme.surfaceVariant 
        else 
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 300)
    )
    
    Column(modifier = modifier) {
        Text(
            text = if (isSource) "De" else "Vers",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(gradientColors))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = selectedCurrency,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        // Nom de la devise (optionnel)
                        Text(
                            text = getCurrencyName(selectedCurrency),
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Sélectionner une devise",
                        tint = Color.White,
                        modifier = Modifier.rotate(rotationState)
                    )
                }
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                availableCurrencies.forEach { currency ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = "$currency - ${getCurrencyName(currency)}",
                                fontWeight = if (currency == selectedCurrency) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        onClick = {
                            onCurrencySelected(currency)
                            expanded = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = if (currency == selectedCurrency) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}

/**
 * Fonction pour obtenir le nom complet d'une devise à partir de son code
 */
fun getCurrencyName(code: String): String {
    return when (code) {
        "USD" -> "Dollar américain"
        "EUR" -> "Euro"
        "MAD" -> "Dirham marocain"
        "GBP" -> "Livre sterling"
        "JPY" -> "Yen japonais"
        "CAD" -> "Dollar canadien"
        "AUD" -> "Dollar australien"
        "CHF" -> "Franc suisse"
        else -> code
    }
}
