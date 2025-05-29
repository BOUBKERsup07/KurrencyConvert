package com.example.kurrencyconvert

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kurrencyconvert.ui.components.AmountInput
import com.example.kurrencyconvert.ui.components.CurrencyCard
import com.example.kurrencyconvert.ui.components.ResultCard
import com.example.kurrencyconvert.ui.theme.GoldAccent
import com.example.kurrencyconvert.ui.theme.KurrencyConvertTheme
import com.example.kurrencyconvert.ui.theme.PrimaryLight
import com.example.kurrencyconvert.viewmodel.ConversionViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val viewModel: ConversionViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KurrencyConvertTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        LargeTopAppBar(
                            title = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(PrimaryLight, GoldAccent)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "K",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "KurrencyConvert",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.largeTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    }
                ) { innerPadding ->
                    ConversionScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ConversionScreen(
    viewModel: ConversionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.observeAsState(false)
    val conversionResult by viewModel.conversionResult.observeAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()
    val savingStatus by viewModel.savingStatus.observeAsState(false)
    
    // Liste des devises disponibles
    val currencies = listOf("USD", "EUR", "MAD", "GBP", "JPY", "CAD", "AUD", "CHF")
    
    // États pour les devises sélectionnées et le montant
    var fromCurrency by remember { mutableStateOf(currencies[0]) }
    var toCurrency by remember { mutableStateOf(currencies[1]) }
    var amount by remember { mutableStateOf("") }
    
    // Effet pour afficher les erreurs
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Fond avec un dégradé subtil
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )
        
        // Contenu principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Titre et description
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Conversion de Devises",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Convertissez instantanément entre différentes devises",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
            
            // Section de conversion
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Champ de saisie du montant
                    AmountInput(
                        value = amount,
                        onValueChange = { amount = it },
                        label = "Montant à convertir",
                        currencyCode = fromCurrency
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Sélection des devises
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Devise source
                        CurrencyCard(
                            currencyCode = fromCurrency,
                            currencyName = "",
                            isSource = true,
                            selectedCurrency = fromCurrency,
                            onCurrencySelected = { fromCurrency = it },
                            availableCurrencies = currencies,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Devise cible
                        CurrencyCard(
                            currencyCode = toCurrency,
                            currencyName = "",
                            isSource = false,
                            selectedCurrency = toCurrency,
                            onCurrencySelected = { toCurrency = it },
                            availableCurrencies = currencies,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        
            // Bouton de conversion avec effet d'ondulation
            Button(
                onClick = {
                    viewModel.convertCurrency(fromCurrency, toCurrency, amount)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Conversion en cours...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Convertir",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Convertir",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            
            // Affichage du résultat avec notre composant personnalisé
            ResultCard(
                amount = amount,
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                result = conversionResult,
                isLoading = isLoading,
                onSaveClick = {
                    // Sauvegarder la conversion
                    conversionResult?.let { result ->
                        viewModel.saveConversionFromUI(fromCurrency, toCurrency, amount.toDoubleOrNull() ?: 0.0, result)
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConversionScreenPreview() {
    KurrencyConvertTheme {
        // Utilisation d'un ViewModel factice pour la prévisualisation
        val viewModel = ConversionViewModel()
        ConversionScreen(viewModel = viewModel)
    }
}