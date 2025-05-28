package com.example.kurrencyconvert

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kurrencyconvert.ui.theme.KurrencyConvertTheme
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
                        CenterAlignedTopAppBar(
                            title = { Text("KurrencyConvert") }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionScreen(
    viewModel: ConversionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.observeAsState(false)
    val conversionResult by viewModel.conversionResult.observeAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()
    
    // Liste des devises disponibles
    val currencies = listOf("USD", "EUR", "MAD", "GBP", "JPY", "CAD", "AUD", "CHF")
    
    // États pour les devises sélectionnées et le montant
    var fromCurrency by remember { mutableStateOf(currencies[0]) }
    var toCurrency by remember { mutableStateOf(currencies[1]) }
    var amount by remember { mutableStateOf("") }
    
    // Gestion des menus déroulants
    var fromCurrencyExpanded by remember { mutableStateOf(false) }
    var toCurrencyExpanded by remember { mutableStateOf(false) }
    
    // Format pour afficher le résultat
    val decimalFormat = DecimalFormat("#,##0.00")
    
    // Effet pour afficher les erreurs
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Titre
        Text(
            text = "Conversion de Devises",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Champ de saisie du montant
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Montant") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Devise source
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = fromCurrency,
                onValueChange = {},
                readOnly = true,
                label = { Text("Devise source") },
                trailingIcon = {
                    IconButton(onClick = { fromCurrencyExpanded = true }) {
                        Icon(Icons.Default.ArrowDropDown, "Dropdown")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            DropdownMenu(
                expanded = fromCurrencyExpanded,
                onDismissRequest = { fromCurrencyExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                currencies.forEach { currency ->
                    DropdownMenuItem(
                        text = { Text(currency) },
                        onClick = {
                            fromCurrency = currency
                            fromCurrencyExpanded = false
                        }
                    )
                }
            }
        }
        
        // Devise cible
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = toCurrency,
                onValueChange = {},
                readOnly = true,
                label = { Text("Devise cible") },
                trailingIcon = {
                    IconButton(onClick = { toCurrencyExpanded = true }) {
                        Icon(Icons.Default.ArrowDropDown, "Dropdown")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            DropdownMenu(
                expanded = toCurrencyExpanded,
                onDismissRequest = { toCurrencyExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                currencies.forEach { currency ->
                    DropdownMenuItem(
                        text = { Text(currency) },
                        onClick = {
                            toCurrency = currency
                            toCurrencyExpanded = false
                        }
                    )
                }
            }
        }
        
        // Bouton de conversion
        Button(
            onClick = {
                viewModel.convertCurrency(fromCurrency, toCurrency, amount)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Convertir")
            }
        }
        
        // Affichage du résultat
        conversionResult?.let { result ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Résultat de la conversion",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${amount.ifEmpty { "0" }} $fromCurrency = ${decimalFormat.format(result as Double)} $toCurrency",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
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