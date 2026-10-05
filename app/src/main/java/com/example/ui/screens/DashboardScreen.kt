package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VehicleEntity
import com.example.model.FuelRefillWithStats
import com.example.model.VehicleDashboardStats
import com.example.ui.components.ConsumptionTrendCard
import com.example.ui.components.MaintenanceAlertBanner
import com.example.ui.components.StatMetricCard
import com.example.ui.components.VehicleHeroCard
import com.example.ui.components.ptBrCurrency
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.GreenAlert
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    vehicle: VehicleEntity?,
    stats: VehicleDashboardStats,
    recentRefills: List<FuelRefillWithStats>,
    onAddFuelClick: () -> Unit,
    onMaintenanceClick: () -> Unit,
    onFlexCalculatorClick: () -> Unit,
    onMarketComparisonClick: () -> Unit = {},
    onSwitchVehicleClick: () -> Unit,
    onUpdateOdometerClick: () -> Unit,
    onViewAllRefillsClick: () -> Unit,
    onDataManagementClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Vehicle Banner
        item {
            VehicleHeroCard(
                vehicle = vehicle,
                onSwitchVehicleClick = onSwitchVehicleClick,
                onEditOdometerClick = onUpdateOdometerClick
            )
        }

        // Maintenance Alerts (if any)
        if (stats.overdueCount > 0 || stats.dueSoonCount > 0) {
            item {
                MaintenanceAlertBanner(
                    overdueCount = stats.overdueCount,
                    dueSoonCount = stats.dueSoonCount,
                    onClick = onMaintenanceClick
                )
            }
        }

        // Primary Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddFuelClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_quick_add_fuel")
                ) {
                    Icon(imageVector = Icons.Default.LocalGasStation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abastecer", fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = onMaintenanceClick,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_quick_maintenance")
                ) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Manutenção", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Key Metrics Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Desempenho & Consumo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = "Consumo Médio",
                        value = if (stats.averageKmPerLiter > 0)
                            String.format(Locale("pt", "BR"), "%.2f", stats.averageKmPerLiter)
                        else "--",
                        unit = "km/L",
                        icon = Icons.Default.Speed,
                        iconTint = MaterialTheme.colorScheme.primary,
                        subtitle = if (stats.lastKmPerLiter != null)
                            "Último: ${String.format(Locale("pt", "BR"), "%.1f", stats.lastKmPerLiter)} km/L"
                        else "Calibrado por tanque",
                        modifier = Modifier.weight(1f)
                    )

                    StatMetricCard(
                        title = "Custo / km",
                        value = if (stats.averageCostPerKm > 0)
                            String.format(Locale("pt", "BR"), "R$ %.2f", stats.averageCostPerKm)
                        else "--",
                        unit = "/km",
                        icon = Icons.Default.AttachMoney,
                        iconTint = GreenAlert,
                        subtitle = "Gasto médio rodado",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = "Gasto no Mês",
                        value = ptBrCurrency.format(stats.monthlySpent),
                        unit = "",
                        icon = Icons.Default.CalendarMonth,
                        iconTint = AmberGlow,
                        subtitle = "Mês atual",
                        modifier = Modifier.weight(1f)
                    )

                    StatMetricCard(
                        title = "Total Abastecido",
                        value = String.format(Locale("pt", "BR"), "%.1f", stats.totalLitersFilled),
                        unit = "Litros",
                        icon = Icons.Default.LocalGasStation,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        subtitle = ptBrCurrency.format(stats.totalFuelSpent),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Consumption Evolution Trend Chart
        item {
            ConsumptionTrendCard(
                refills = recentRefills,
                overallAverageKmL = stats.averageKmPerLiter
            )
        }

        // Quick Market Comparison Banner (Electric & Hybrid)
        item {
            Card(
                onClick = onMarketComparisonClick,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("banner_market_comparison")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Comparar com Elétricos & Híbridos",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Veja quanto você economizaria por ano com seu consumo atual.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quick Flex Calculator Banner
        item {
            Card(
                onClick = onFlexCalculatorClick,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("banner_flex_calc")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Calculadora Flex: Etanol vs Gasolina",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Descubra qual combustível é mais econômico para o seu carro hoje.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Recent Refuelings header & items
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Últimos Abastecimentos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = onViewAllRefillsClick,
                    modifier = Modifier.testTag("btn_view_all_refills")
                ) {
                    Text("Ver Histórico")
                }
            }
        }

        if (recentRefills.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nenhum abastecimento cadastrado ainda.", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onAddFuelClick) {
                            Text("Cadastrar Primeiro Abastecimento")
                        }
                    }
                }
            }
        } else {
            items(recentRefills.take(3)) { refill ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("refill_item_${refill.log.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${refill.log.odometerKm.toInt()} km",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = refill.log.fuelType,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${dateFormat.format(Date(refill.log.dateEpochMillis))} • ${String.format(Locale("pt", "BR"), "%.1f", refill.log.liters)} L • R$ ${String.format(Locale("pt", "BR"), "%.2f", refill.log.pricePerLiter)}/L",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (refill.log.gasStationName.isNotBlank()) {
                                Text(
                                    text = refill.log.gasStationName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = ptBrCurrency.format(refill.log.totalCost),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (refill.kmPerLiter != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = GreenAlert.copy(alpha = 0.15f),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = "${String.format(Locale("pt", "BR"), "%.2f", refill.kmPerLiter)} km/L",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenAlert,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Data Management Shortcut Card
        item {
            Card(
                onClick = onDataManagementClick,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("banner_data_management")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gestão de Dados & Backup",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Exportar registros para JSON ou limpar dados locais.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
