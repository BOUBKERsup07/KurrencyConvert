package com.example.kurrencyconvert.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kurrencyconvert.ui.theme.GoldAccent
import com.example.kurrencyconvert.ui.theme.PrimaryLight
import com.example.kurrencyconvert.ui.theme.SuccessLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat

/**
 * Composant pour afficher le résultat de la conversion avec des animations
 */
@Composable
fun ResultCard(
    amount: String,
    fromCurrency: String,
    toCurrency: String,
    result: Double?,
    isLoading: Boolean,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val decimalFormat = DecimalFormat("#,##0.00")
    val scope = rememberCoroutineScope()
    var showCheckmark by remember { mutableStateOf(false) }
    
    val animatedResult = remember(result) {
        Animatable(initialValue = 0f)
    }
    
    LaunchedEffect(result) {
        if (result != null && result > 0) {
            animatedResult.animateTo(
                targetValue = result.toFloat(),
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
            )
        } else {
            animatedResult.snapTo(0f)
        }
    }
    
    AnimatedVisibility(
        visible = result != null || isLoading,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Résultat de la conversion",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (result != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(PrimaryLight, GoldAccent)
                                )
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${if (amount.isEmpty()) "0" else amount} $fromCurrency =",
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "${decimalFormat.format(animatedResult.value)} $toCurrency",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Taux: 1 $fromCurrency = ${decimalFormat.format(result / (amount.toDoubleOrNull() ?: 1.0))} $toCurrency",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            scope.launch {
                                onSaveClick()
                                showCheckmark = true
                                delay(2000)
                                showCheckmark = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showCheckmark) SuccessLight else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            if (showCheckmark) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Sauvegardé",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sauvegardé!")
                            } else {
                                Text("Sauvegarder cette conversion")
                            }
                        }
                    }
                }
            }
        }
    }
}
