package com.example.ui.dialogs

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MaintenanceItemEntity

@Composable
fun AddMaintenanceItemDialog(
    initialItem: MaintenanceItemEntity? = null,
    currentVehicleKm: Double,
    onDismiss: () -> Unit,
    onDelete: ((MaintenanceItemEntity) -> Unit)? = null,
    onConfirm: (
        title: String,
        category: String,
        intervalKm: Int,
        intervalMonths: Int,
        lastKm: Double,
        lastDateMillis: Long,
        notes: String
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialItem?.title ?: "") }
    var category by remember { mutableStateOf(initialItem?.category ?: "Motor") }
    var intervalKmText by remember {
        mutableStateOf(initialItem?.intervalKm?.toString() ?: "10000")
    }
    var intervalMonthsText by remember {
        mutableStateOf(initialItem?.intervalMonths?.toString() ?: "12")
    }
    var lastKmText by remember {
        mutableStateOf(
            if (initialItem != null) initialItem.lastServiceKm.toInt().toString()
            else currentVehicleKm.toInt().toString()
        )
    }
    var notes by remember { mutableStateOf(initialItem?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Motor", "Pneus", "Freios", "Filtros", "Arrefecimento", "Elétrica", "Outro")
    val quickSuggestions = listOf(
        "Troca de Óleo e Filtro" to ("Motor" to (10000 to 6)),
        "Filtro de Ar do Motor" to ("Filtros" to (10000 to 12)),
        "Rodízio e Balanceamento" to ("Pneus" to (10000 to 6)),
        "Pastilhas de Freio" to ("Freios" to (25000 to 24)),
        "Correia Dentada" to ("Motor" to (50000 to 48)),
        "Velas de Ignição" to ("Motor" to (40000 to 24)),
        "Filtro de Cabine / Ar-Cond." to ("Filtros" to (10000 to 12)),
        "Fluido de Arrefecimento" to ("Arrefecimento" to (40000 to 24))
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AddAlert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (initialItem != null) "Editar Item de Manutenção" else "Novo Alerta Preventivo",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                if (initialItem != null && onDelete != null) {
                    IconButton(
                        onClick = { onDelete(initialItem) },
                        modifier = Modifier.testTag("delete_maintenance_item_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                if (initialItem == null) {
                    Text(
                        text = "Sugestões rápidas:",
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
                        quickSuggestions.forEach { (name, meta) ->
                            SuggestionChip(
                                onClick = {
                                    title = name
                                    category = meta.first
                                    intervalKmText = meta.second.first.toString()
                                    intervalMonthsText = meta.second.second.toString()
                                },
                                label = { Text(name, fontSize = 11.sp) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("Nome do Item / Serviço *") },
                    placeholder = { Text("Ex: Troca de Óleo e Filtro") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_maint_title")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Categoria:",
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
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = intervalKmText,
                        onValueChange = {
                            intervalKmText = it
                            errorMessage = null
                        },
                        label = { Text("Intervalo (km) *") },
                        placeholder = { Text("Ex: 10000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_interval_km")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = intervalMonthsText,
                        onValueChange = {
                            intervalMonthsText = it
                            errorMessage = null
                        },
                        label = { Text("Intervalo (meses) *") },
                        placeholder = { Text("Ex: 12") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_interval_months")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = lastKmText,
                    onValueChange = {
                        lastKmText = it
                        errorMessage = null
                    },
                    label = { Text("Última troca feita em (km)") },
                    placeholder = { Text("Ex: 30000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_last_km")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Recomendações da montadora (Opcional)") },
                    placeholder = { Text("Ex: Especificação 5W30 Dexos1") },
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
                    val intKm = intervalKmText.toIntOrNull()
                    val intMonths = intervalMonthsText.toIntOrNull()
                    val lastKm = lastKmText.replace(",", ".").toDoubleOrNull() ?: 0.0

                    if (title.isBlank()) {
                        errorMessage = "Informe o nome do item."
                        return@Button
                    }
                    if (intKm == null || intKm <= 0) {
                        errorMessage = "Informe um intervalo em km válido."
                        return@Button
                    }
                    if (intMonths == null || intMonths <= 0) {
                        errorMessage = "Informe um intervalo em meses válido."
                        return@Button
                    }

                    onConfirm(
                        title.trim(),
                        category,
                        intKm,
                        intMonths,
                        lastKm,
                        initialItem?.lastServiceDateEpochMillis ?: System.currentTimeMillis(),
                        notes.trim()
                    )
                },
                modifier = Modifier.testTag("confirm_save_maintenance_item")
            ) {
                Text(if (initialItem != null) "Atualizar" else "Adicionar Alerta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
