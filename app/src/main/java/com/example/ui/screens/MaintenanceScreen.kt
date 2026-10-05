package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MaintenanceHistoryEntity
import com.example.data.MaintenanceItemEntity
import com.example.model.MaintenanceItemWithStatus
import com.example.model.MaintenanceStatusLevel
import com.example.ui.components.MaintenanceItemCard
import com.example.ui.components.ptBrCurrency
import com.example.ui.theme.GreenAlert
import com.example.ui.theme.RedAlert
import com.example.ui.theme.YellowAlert
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MaintenanceScreen(
    itemsWithStatus: List<MaintenanceItemWithStatus>,
    history: List<MaintenanceHistoryEntity>,
    onAddMaintenanceClick: () -> Unit,
    onRecordPerformedClick: (MaintenanceItemEntity) -> Unit,
    onEditItemClick: (MaintenanceItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filters = listOf("Todos", "Vencidos", "Atenção", "Em dia", "Histórico de Trocas")
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    val overdueCount = itemsWithStatus.count { it.status == MaintenanceStatusLevel.OVERDUE }
    val dueSoonCount = itemsWithStatus.count { it.status == MaintenanceStatusLevel.DUE_SOON }

    val filteredList = when (selectedFilter) {
        "Vencidos" -> itemsWithStatus.filter { it.status == MaintenanceStatusLevel.OVERDUE }
        "Atenção" -> itemsWithStatus.filter { it.status == MaintenanceStatusLevel.DUE_SOON }
        "Em dia" -> itemsWithStatus.filter { it.status == MaintenanceStatusLevel.OK }
        else -> itemsWithStatus
    }

    Scaffold(
        modifier = modifier.testTag("maintenance_screen"),
        floatingActionButton = {
            if (selectedFilter != "Histórico de Trocas") {
                FloatingActionButton(
                    onClick = onAddMaintenanceClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_maintenance")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar Manutenção")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Overview Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("maintenance_overview_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Manutenção Preventiva",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Acompanhamento por km e tempo",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Overdue badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = RedAlert.copy(alpha = 0.15f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$overdueCount",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = RedAlert
                                    )
                                    Text(
                                        text = "Vencidos",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = RedAlert
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Due soon badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = YellowAlert.copy(alpha = 0.15f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$dueSoonCount",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = YellowAlert
                                    )
                                    Text(
                                        text = "Próximos",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = YellowAlert
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // OK badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GreenAlert.copy(alpha = 0.15f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${itemsWithStatus.size - overdueCount - dueSoonCount}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GreenAlert
                                    )
                                    Text(
                                        text = "Em dia",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenAlert
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Horizontal Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 12.sp) },
                            modifier = Modifier.testTag("filter_maint_${filter.lowercase().take(5)}")
                        )
                    }
                }
            }

            // Body: Maintenance items or History list
            if (selectedFilter == "Histórico de Trocas") {
                if (history.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhuma manutenção concluída registrada ainda.\nAo concluir uma troca, ela ficará salva aqui.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(history) { entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = entry.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (entry.cost > 0) {
                                        Text(
                                            text = ptBrCurrency.format(entry.cost),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Realizada em ${dateFormat.format(Date(entry.serviceDateEpochMillis))} aos ${entry.serviceKm.toInt()} km",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (entry.workshop.isNotBlank()) {
                                    Text(
                                        text = "Oficina: ${entry.workshop}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (entry.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = entry.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                if (filteredList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhum item nesta categoria.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(filteredList) { itemWithStatus ->
                        MaintenanceItemCard(
                            itemWithStatus = itemWithStatus,
                            onRecordPerformedClick = { onRecordPerformedClick(itemWithStatus.item) },
                            onEditClick = { onEditItemClick(itemWithStatus.item) }
                        )
                    }
                }
            }
        }
    }
}
