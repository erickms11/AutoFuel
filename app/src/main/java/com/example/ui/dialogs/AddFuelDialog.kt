package com.example.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VehicleEntity

@Composable
fun AddFuelDialog(
    vehicle: VehicleEntity?,
    onDismiss: () -> Unit,
    onConfirm: (
        odometer: Double,
        liters: Double,
        pricePerLiter: Double,
        totalCost: Double,
        fuelType: String,
        isFullTank: Boolean,
        gasStation: String,
        notes: String
    ) -> Unit
) {
    var odometerText by remember {
        mutableStateOf(
            if (vehicle != null && vehicle.currentOdometerKm > 0)
                vehicle.currentOdometerKm.toInt().toString()
            else ""
        )
    }
    var litersText by remember { mutableStateOf("") }
    var pricePerLiterText by remember { mutableStateOf("") }
    var totalCostText by remember { mutableStateOf("") }
    var selectedFuelType by remember {
        mutableStateOf(vehicle?.preferredFuel ?: "Gasolina Comum")
    }
    var isFullTank by remember { mutableStateOf(true) }
    var gasStationText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val fuelTypes = listOf("Gasolina Comum", "Gasolina Aditivada", "Etanol", "Diesel S10", "GNV")

    // Automatic calculation between liters, price per liter, and total cost
    fun updateCalculations(
        litersStr: String = litersText,
        priceStr: String = pricePerLiterText,
        totalStr: String = totalCostText,
        trigger: String
    ) {
        val l = litersStr.replace(",", ".").toDoubleOrNull()
        val p = priceStr.replace(",", ".").toDoubleOrNull()
        val t = totalStr.replace(",", ".").toDoubleOrNull()

        if (trigger == "liters" || trigger == "price") {
            if (l != null && p != null && l > 0 && p > 0) {
                val total = l * p
                totalCostText = String.format(java.util.Locale.US, "%.2f", total)
            }
        } else if (trigger == "total") {
            if (l != null && t != null && l > 0 && t > 0) {
                val price = t / l
                pricePerLiterText = String.format(java.util.Locale.US, "%.2f", price)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalGasStation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrar Abastecimento", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                // Vehicle Name
                Text(
                    text = "Veículo: ${vehicle?.name ?: "Padrão"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Odometer
                OutlinedTextField(
                    value = odometerText,
                    onValueChange = {
                        odometerText = it
                        errorMessage = null
                    },
                    label = { Text("Quilometragem no Painel (km) *") },
                    placeholder = { Text("Ex: 34500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_odometer")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Fuel Types horizontal chips
                Text(
                    text = "Tipo de Combustível:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    fuelTypes.forEach { type ->
                        FilterChip(
                            selected = selectedFuelType == type,
                            onClick = { selectedFuelType = type },
                            label = { Text(type, fontSize = 12.sp) },
                            modifier = Modifier.testTag("chip_fuel_${type.lowercase().take(5)}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Liters
                OutlinedTextField(
                    value = litersText,
                    onValueChange = {
                        litersText = it
                        updateCalculations(litersStr = it, trigger = "liters")
                        errorMessage = null
                    },
                    label = { Text("Litros abastecidos (L) *") },
                    placeholder = { Text("Ex: 40.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_liters")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Price Per Liter & Total Cost side by side
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = pricePerLiterText,
                        onValueChange = {
                            pricePerLiterText = it
                            updateCalculations(priceStr = it, trigger = "price")
                            errorMessage = null
                        },
                        label = { Text("Preço/Litro (R$)") },
                        placeholder = { Text("Ex: 5.89") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_price_per_liter")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = totalCostText,
                        onValueChange = {
                            totalCostText = it
                            updateCalculations(totalStr = it, trigger = "total")
                            errorMessage = null
                        },
                        label = { Text("Total Pago (R$) *") },
                        placeholder = { Text("Ex: 238.54") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_total_cost")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Full Tank Switch with explanation
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Encheu o tanque?",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Switch(
                                checked = isFullTank,
                                onCheckedChange = { isFullTank = it },
                                modifier = Modifier.testTag("switch_full_tank")
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                text = "Fundamental para o cálculo preciso de km/L entre abastecimentos.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Gas station name
                OutlinedTextField(
                    value = gasStationText,
                    onValueChange = { gasStationText = it },
                    label = { Text("Posto de Combustível (Opcional)") },
                    placeholder = { Text("Ex: Shell Posto Central") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Observações (Opcional)") },
                    placeholder = { Text("Ex: Calibragem 32 psi, aditivo, etc.") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val odo = odometerText.replace(",", ".").toDoubleOrNull()
                    val lit = litersText.replace(",", ".").toDoubleOrNull()
                    var total = totalCostText.replace(",", ".").toDoubleOrNull()
                    var price = pricePerLiterText.replace(",", ".").toDoubleOrNull()

                    if (odo == null || odo <= 0) {
                        errorMessage = "Informe uma quilometragem válida."
                        return@Button
                    }
                    if (lit == null || lit <= 0) {
                        errorMessage = "Informe a quantidade de litros abastecidos."
                        return@Button
                    }
                    if (total == null || total <= 0) {
                        if (price != null && price > 0) {
                            total = lit * price
                        } else {
                            errorMessage = "Informe o valor total pago ou o preço por litro."
                            return@Button
                        }
                    }
                    if (price == null || price <= 0) {
                        price = total / lit
                    }

                    onConfirm(
                        odo,
                        lit,
                        price,
                        total,
                        selectedFuelType,
                        isFullTank,
                        gasStationText.trim(),
                        notesText.trim()
                    )
                },
                modifier = Modifier.testTag("confirm_add_fuel_button")
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
