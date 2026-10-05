package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.VehicleEntity

@Composable
fun VehicleManageDialog(
    vehicles: List<VehicleEntity>,
    activeVehicle: VehicleEntity?,
    onDismiss: () -> Unit,
    onSelectVehicle: (Long) -> Unit,
    onAddNewVehicle: (
        name: String,
        brand: String,
        year: Int,
        plate: String,
        odometer: Double,
        capacity: Double,
        fuel: String
    ) -> Unit
) {
    var isAddingNew by remember { mutableStateOf(false) }

    // Form fields for new vehicle
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var yearText by remember { mutableStateOf("2023") }
    var plate by remember { mutableStateOf("") }
    var odometerText by remember { mutableStateOf("0") }
    var capacityText by remember { mutableStateOf("50") }
    var preferredFuel by remember { mutableStateOf("Gasolina Comum") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isAddingNew) "Cadastrar Novo Veículo" else "Meus Veículos",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            if (isAddingNew) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; errorMessage = null },
                        label = { Text("Nome / Apelido do Carro *") },
                        placeholder = { Text("Ex: Onix Hatch, Civic, Renegade") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_vehicle_name")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Marca") },
                            placeholder = { Text("Ex: Chevrolet") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = yearText,
                            onValueChange = { yearText = it },
                            label = { Text("Ano") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = plate,
                            onValueChange = { plate = it.uppercase() },
                            label = { Text("Placa") },
                            placeholder = { Text("ABC1D23") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = capacityText,
                            onValueChange = { capacityText = it },
                            label = { Text("Tanque (L)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = odometerText,
                        onValueChange = { odometerText = it; errorMessage = null },
                        label = { Text("Quilometragem Atual (km) *") },
                        placeholder = { Text("Ex: 45000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_vehicle_odometer")
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
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(vehicles) { v ->
                            val isSelected = v.id == activeVehicle?.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSelectVehicle(v.id) }
                                    .testTag("vehicle_item_${v.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected)
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsCar,
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = v.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${v.plate.ifBlank { "Sem placa" }} • ${v.currentOdometerKm.toInt()} km",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (isSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Ativo",
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { isAddingNew = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_vehicle_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cadastrar Outro Veículo")
                    }
                }
            }
        },
        confirmButton = {
            if (isAddingNew) {
                Button(
                    onClick = {
                        val year = yearText.toIntOrNull() ?: 2023
                        val odo = odometerText.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val cap = capacityText.replace(",", ".").toDoubleOrNull() ?: 50.0

                        if (name.isBlank()) {
                            errorMessage = "Informe o nome do veículo."
                            return@Button
                        }

                        onAddNewVehicle(
                            name.trim(),
                            brand.trim(),
                            year,
                            plate.trim(),
                            odo,
                            cap,
                            preferredFuel
                        )
                        isAddingNew = false
                    },
                    modifier = Modifier.testTag("confirm_add_vehicle_btn")
                ) {
                    Text("Salvar Veículo")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Fechar")
                }
            }
        },
        dismissButton = {
            if (isAddingNew) {
                TextButton(onClick = { isAddingNew = false }) {
                    Text("Voltar")
                }
            }
        }
    )
}

@Composable
fun UpdateOdometerDialog(
    currentKm: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var kmText by remember { mutableStateOf(currentKm.toInt().toString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atualizar Quilometragem") },
        text = {
            Column {
                Text(
                    text = "Ajuste o odômetro do veículo para manter os alertas de manutenção em dia.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = kmText,
                    onValueChange = { kmText = it; errorMessage = null },
                    label = { Text("Quilometragem Atual (km) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_update_odometer")
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val km = kmText.replace(",", ".").toDoubleOrNull()
                    if (km == null || km < 0) {
                        errorMessage = "Informe um valor de km válido."
                        return@Button
                    }
                    onConfirm(km)
                },
                modifier = Modifier.testTag("confirm_update_odometer")
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
