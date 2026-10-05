package com.example.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.MaintenanceItemEntity

@Composable
fun RecordMaintenanceDialog(
    item: MaintenanceItemEntity,
    currentVehicleKm: Double,
    onDismiss: () -> Unit,
    onConfirm: (
        serviceKm: Double,
        serviceDateMillis: Long,
        cost: Double,
        workshop: String,
        notes: String
    ) -> Unit
) {
    var kmText by remember {
        mutableStateOf(currentVehicleKm.toInt().toString())
    }
    var costText by remember { mutableStateOf("") }
    var workshopText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrar Manutenção", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Intervalo: a cada ${item.intervalKm} km ou ${item.intervalMonths} meses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = kmText,
                    onValueChange = {
                        kmText = it
                        errorMessage = null
                    },
                    label = { Text("Quilometragem da Troca (km) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_service_km")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = costText,
                    onValueChange = {
                        costText = it
                        errorMessage = null
                    },
                    label = { Text("Custo Total (R$)") },
                    placeholder = { Text("Ex: 280.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_service_cost")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = workshopText,
                    onValueChange = { workshopText = it },
                    label = { Text("Oficina / Estabelecimento (Opcional)") },
                    placeholder = { Text("Ex: AutoCenter Express") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Peças e Observações (Opcional)") },
                    placeholder = { Text("Ex: Óleo 5W30 Sintético + Filtro Mann") },
                    maxLines = 3,
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
                    val km = kmText.replace(",", ".").toDoubleOrNull()
                    val cost = costText.replace(",", ".").toDoubleOrNull() ?: 0.0

                    if (km == null || km <= 0) {
                        errorMessage = "Informe a quilometragem da troca."
                        return@Button
                    }

                    onConfirm(
                        km,
                        System.currentTimeMillis(),
                        cost,
                        workshopText.trim(),
                        notesText.trim()
                    )
                },
                modifier = Modifier.testTag("confirm_record_maint_button")
            ) {
                Text("Confirmar Troca")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
