package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ptBrCurrency
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.GreenAlert
import java.util.Locale

@Composable
fun FlexCalculatorScreen(
    vehicleGasAvgKmL: Double?,
    vehicleEthanolAvgKmL: Double?,
    tankCapacity: Double = 50.0,
    modifier: Modifier = Modifier
) {
    var gasolinePriceText by remember { mutableStateOf("5.89") }
    var ethanolPriceText by remember { mutableStateOf("3.89") }
    var calculationMode by remember { mutableStateOf("standard") } // "standard" or "custom"

    val gasPrice = gasolinePriceText.replace(",", ".").toDoubleOrNull() ?: 5.89
    val ethanolPrice = ethanolPriceText.replace(",", ".").toDoubleOrNull() ?: 3.89

    // Ratio threshold
    val customRatio = if (vehicleGasAvgKmL != null && vehicleEthanolAvgKmL != null && vehicleGasAvgKmL > 0) {
        vehicleEthanolAvgKmL / vehicleGasAvgKmL
    } else null

    val threshold = if (calculationMode == "custom" && customRatio != null) customRatio else 0.70
    val currentRatio = if (gasPrice > 0) ethanolPrice / gasPrice else 0.0

    val isEthanolBetter = currentRatio <= threshold
    val percentage = currentRatio * 100.0

    // Breakeven gasoline price: what price should gasoline be to match current ethanol
    val breakevenGasPrice = if (threshold > 0) ethanolPrice / threshold else 0.0

    // Estimated cost to fill a full tank (e.g. 50L)
    val gasTankCost = gasPrice * tankCapacity
    val ethanolTankCost = ethanolPrice * tankCapacity
    val tankDiff = kotlin.math.abs(gasTankCost - (ethanolTankCost / threshold))

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("flex_calculator_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Calculadora Flex",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Etanol vs Gasolina: qual compensa mais hoje?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Mode Selector: 70% standard vs vehicle's measured ratio
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = calculationMode == "standard",
                onClick = { calculationMode = "standard" },
                label = { Text("Regra Padrão (70%)") },
                modifier = Modifier.weight(1f).testTag("chip_standard_calc")
            )

            FilterChip(
                selected = calculationMode == "custom",
                onClick = { calculationMode = "custom" },
                label = {
                    Text(
                        if (customRatio != null) "Consumo Real (${(customRatio * 100).toInt()}%)"
                        else "Consumo Real"
                    )
                },
                enabled = customRatio != null,
                modifier = Modifier.weight(1f).testTag("chip_custom_calc")
            )
        }

        // Price Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Preços na Bomba (R$)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = gasolinePriceText,
                        onValueChange = { gasolinePriceText = it },
                        label = { Text("Gasolina (R$/L)") },
                        placeholder = { Text("5.89") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.LocalGasStation, contentDescription = null, tint = AmberGlow)
                        },
                        modifier = Modifier.weight(1f).testTag("input_calc_gas")
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    OutlinedTextField(
                        value = ethanolPriceText,
                        onValueChange = { ethanolPriceText = it },
                        label = { Text("Etanol (R$/L)") },
                        placeholder = { Text("3.89") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.LocalGasStation, contentDescription = null, tint = GreenAlert)
                        },
                        modifier = Modifier.weight(1f).testTag("input_calc_ethanol")
                    )
                }
            }
        }

        // Result Decision Banner
        val resultBg = if (isEthanolBetter) GreenAlert.copy(alpha = 0.15f) else AmberGlow.copy(alpha = 0.15f)
        val resultColor = if (isEthanolBetter) GreenAlert else AmberGlow

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("flex_result_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = resultBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = resultColor,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isEthanolBetter) "COMPENSA ABASTECER COM ETANOL!" else "COMPENSA ABASTECER COM GASOLINA!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = resultColor,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "O preço do etanol está em ${String.format(Locale("pt", "BR"), "%.1f", percentage)}% do valor da gasolina (ponto de equilíbrio: ${(threshold * 100).toInt()}%).",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Detailed Economic Insights
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Análise de Paridade",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Relação Etanol / Gasolina:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "${String.format(Locale("pt", "BR"), "%.2f", percentage)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = resultColor
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Gasolina deveria custar mais de:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = ptBrCurrency.format(breakevenGasPrice),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Tanque cheio estimado (${tankCapacity.toInt()}L):", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "Etanol: ${ptBrCurrency.format(ethanolTankCost)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Dica: Em viagens longas na estrada, o motor costuma ter rendimento superior. Monitore a média real de km/L em cada tanque pelo AutoFuel!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
