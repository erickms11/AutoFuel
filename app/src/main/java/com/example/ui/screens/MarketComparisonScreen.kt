package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VehicleEntity
import com.example.model.MarketCarBenchmark
import com.example.model.VehiclePowertrain
import com.example.ui.components.ptBrCurrency
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.GreenAlert
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

@Composable
fun MarketComparisonScreen(
    activeVehicle: VehicleEntity?,
    userAverageKmL: Double?,
    modifier: Modifier = Modifier
) {
    // Current user vehicle real consumption (fallback to 14.0 if no fill-up logs exist yet)
    val actualUserKmL = if (userAverageKmL != null && userAverageKmL > 0) userAverageKmL else 14.0

    // Simulation parameters
    var monthlyKm by remember { mutableFloatStateOf(1200f) }
    var gasolinePriceText by remember { mutableStateOf("5.89") }
    var electricityKwhPriceText by remember { mutableStateOf("0.90") }
    var selectedCategoryFilter by remember { mutableStateOf("Todos") }
    var selectedBenchmarkCar by remember { mutableStateOf(MarketCarBenchmark.PRESET_CARS.first()) }
    var showCustomInput by remember { mutableStateOf(false) }

    // Custom vehicle input fields (if user wants to test an arbitrary car)
    var customName by remember { mutableStateOf("Meu Modelo Simulado") }
    var customIsElectric by remember { mutableStateOf(true) }
    var customEfficiencyText by remember { mutableStateOf("8.5") }

    val gasolinePrice = gasolinePriceText.replace(",", ".").toDoubleOrNull() ?: 5.89
    val electricityPrice = electricityKwhPriceText.replace(",", ".").toDoubleOrNull() ?: 0.90

    val categories = listOf("Todos", "100% Elétrico", "Híbrido", "Combustão Econômico")
    val filteredCars = when (selectedCategoryFilter) {
        "100% Elétrico" -> MarketCarBenchmark.PRESET_CARS.filter { it.powertrain == VehiclePowertrain.ELECTRIC }
        "Híbrido" -> MarketCarBenchmark.PRESET_CARS.filter { it.powertrain == VehiclePowertrain.HYBRID }
        "Combustão Econômico" -> MarketCarBenchmark.PRESET_CARS.filter { it.powertrain == VehiclePowertrain.COMBUSTION }
        else -> MarketCarBenchmark.PRESET_CARS
    }

    // Active benchmark car to compare against
    val activeBenchmark = if (showCustomInput) {
        val eff = customEfficiencyText.replace(",", ".").toDoubleOrNull() ?: 8.0
        MarketCarBenchmark(
            id = "custom",
            name = customName.ifBlank { "Carro Simulado" },
            category = if (customIsElectric) "Elétrico Personalizado" else "Combustão Personalizado",
            powertrain = if (customIsElectric) VehiclePowertrain.ELECTRIC else VehiclePowertrain.COMBUSTION,
            efficiencyKmPerKwh = if (customIsElectric) eff else 0.0,
            efficiencyKmL = if (!customIsElectric) eff else 0.0,
            description = "Simulação personalizada definida pelo usuário.",
            badge = "Custom"
        )
    } else {
        selectedBenchmarkCar
    }

    // Cost calculations
    val userCostPerKm = if (actualUserKmL > 0) gasolinePrice / actualUserKmL else 0.40
    val benchmarkCostPerKm = activeBenchmark.calculateCostPerKm(gasolinePrice, electricityPrice)

    val userMonthlyCost = userCostPerKm * monthlyKm
    val benchmarkMonthlyCost = benchmarkCostPerKm * monthlyKm

    val monthlyDifference = userMonthlyCost - benchmarkMonthlyCost
    val annualDifference = monthlyDifference * 12.0
    val percentSavings = if (userCostPerKm > 0) ((userCostPerKm - benchmarkCostPerKm) / userCostPerKm) * 100.0 else 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("market_comparison_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Comparador de Mercado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Compare o consumo real do seu carro com veículos 100% elétricos e híbridos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Active Car Snapshot Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Seu Veículo Atual",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = if (userAverageKmL != null && userAverageKmL > 0) "Média Real do App" else "Estimativa Padrão",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = activeVehicle?.name ?: "Meu Carro",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Consumo: ${String.format(Locale("pt", "BR"), "%.1f", actualUserKmL)} km/L",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = ptBrCurrency.format(userCostPerKm),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "por km rodado",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = !showCustomInput && selectedCategoryFilter == cat,
                    onClick = {
                        showCustomInput = false
                        selectedCategoryFilter = cat
                        val firstMatch = if (cat == "Todos") MarketCarBenchmark.PRESET_CARS.first()
                        else MarketCarBenchmark.PRESET_CARS.firstOrNull { it.category == cat } ?: MarketCarBenchmark.PRESET_CARS.first()
                        selectedBenchmarkCar = firstMatch
                    },
                    label = { Text(cat, fontSize = 12.sp) },
                    modifier = Modifier.testTag("filter_car_${cat.lowercase().replace(" ", "_")}")
                )
            }

            FilterChip(
                selected = showCustomInput,
                onClick = { showCustomInput = true },
                label = { Text("Personalizado", fontSize = 12.sp) },
                modifier = Modifier.testTag("filter_car_custom")
            )
        }

        // Vehicle Selector Carousel / Horizontal list (if not custom)
        if (!showCustomInput) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Escolha o carro para comparar:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    filteredCars.forEach { car ->
                        val isSelected = car.id == selectedBenchmarkCar.id
                        Card(
                            modifier = Modifier
                                .width(220.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedBenchmarkCar = car }
                                .testTag("card_select_car_${car.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (car.powertrain) {
                                            VehiclePowertrain.ELECTRIC -> GreenAlert.copy(alpha = 0.15f)
                                            VehiclePowertrain.HYBRID -> AmberGlow.copy(alpha = 0.15f)
                                            VehiclePowertrain.COMBUSTION -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ) {
                                        Text(
                                            text = car.badge,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when (car.powertrain) {
                                                VehiclePowertrain.ELECTRIC -> GreenAlert
                                                VehiclePowertrain.HYBRID -> AmberGlow
                                                VehiclePowertrain.COMBUSTION -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = car.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = car.getEfficiencyDisplay(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = car.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Custom Input Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Configurar Carro Personalizado",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Nome do Veículo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = customIsElectric,
                            onClick = {
                                customIsElectric = true
                                customEfficiencyText = "8.0"
                            },
                            label = { Text("100% Elétrico (km/kWh)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !customIsElectric,
                            onClick = {
                                customIsElectric = false
                                customEfficiencyText = "16.0"
                            },
                            label = { Text("Combustão / Híbrido (km/L)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = customEfficiencyText,
                        onValueChange = { customEfficiencyText = it },
                        label = { Text(if (customIsElectric) "Eficiência (km/kWh)" else "Consumo Médio (km/L)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Big Highlight Savings Card
        val isSaving = monthlyDifference > 0
        val highlightColor = if (isSaving) GreenAlert else AmberGlow
        val highlightBg = if (isSaving) GreenAlert.copy(alpha = 0.12f) else AmberGlow.copy(alpha = 0.12f)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_savings_result"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = highlightBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, highlightColor.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isSaving) Icons.Default.Bolt else Icons.Default.CompareArrows,
                        contentDescription = null,
                        tint = highlightColor,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSaving) "PROJEÇÃO DE ECONOMIA" else "COMPARAÇÃO DE CUSTO",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = highlightColor,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isSaving)
                        "${ptBrCurrency.format(annualDifference)} / ano"
                    else
                        "${ptBrCurrency.format(abs(annualDifference))} a mais / ano",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = highlightColor
                )

                Text(
                    text = if (isSaving)
                        "Economia de ${ptBrCurrency.format(monthlyDifference)} por mês rodando ${monthlyKm.toInt()} km"
                    else
                        "Custo adicional de ${ptBrCurrency.format(abs(monthlyDifference))} por mês",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isSaving && percentSavings > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = highlightColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "⚡ Custo ${String.format(Locale("pt", "BR"), "%.0f", percentSavings)}% menor por quilômetro rodado!",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = highlightColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Side by Side Detailed Comparison Table Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Comparativo Detalhado",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Métrica",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1.2f)
                    )
                    Text(
                        text = "Seu Carro",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = activeBenchmark.name.take(14),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.3f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 1: Efficiency
                ComparisonRow(
                    label = "Eficiência",
                    userVal = "${String.format(Locale("pt", "BR"), "%.1f", actualUserKmL)} km/L",
                    benchmarkVal = activeBenchmark.getEfficiencyDisplay().take(15)
                )

                // Row 2: Cost / km
                ComparisonRow(
                    label = "Custo / km",
                    userVal = ptBrCurrency.format(userCostPerKm),
                    benchmarkVal = ptBrCurrency.format(benchmarkCostPerKm),
                    highlight = true
                )

                // Row 3: Cost for 100 km
                ComparisonRow(
                    label = "Custo p/ 100 km",
                    userVal = ptBrCurrency.format(userCostPerKm * 100),
                    benchmarkVal = ptBrCurrency.format(benchmarkCostPerKm * 100)
                )

                // Row 4: Monthly Cost
                ComparisonRow(
                    label = "Gasto Mensal",
                    userVal = ptBrCurrency.format(userMonthlyCost),
                    benchmarkVal = ptBrCurrency.format(benchmarkMonthlyCost),
                    highlight = true
                )

                // Row 5: Yearly Cost
                ComparisonRow(
                    label = "Gasto Anual",
                    userVal = ptBrCurrency.format(userMonthlyCost * 12),
                    benchmarkVal = ptBrCurrency.format(benchmarkMonthlyCost * 12)
                )
            }
        }

        // Comparative Bar Chart: Cost to drive 100 km across classes
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Custo para Rodar 100 km (R$)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Chart Items
                val chartItems = listOf(
                    Triple("100% Elétrico (Dolphin Mini)", (0.90 / 9.6) * 100, GreenAlert),
                    Triple("Híbrido (Corolla Hybrid)", (gasolinePrice / 18.5) * 100, MaterialTheme.colorScheme.primary),
                    Triple("Compacto Econômico (Kwid)", (gasolinePrice / 15.5) * 100, Color(0xFF0284C7)),
                    Triple("Seu Carro Atual", userCostPerKm * 100, AmberGlow),
                    Triple("SUV Médio Turbo (Gasolina)", (gasolinePrice / 11.0) * 100, Color(0xFFE11D48))
                )

                val maxCost = (chartItems.maxOfOrNull { it.second } ?: 60.0).coerceAtLeast(10.0)

                chartItems.forEach { (label, cost, color) ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (label.startsWith("Seu")) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = ptBrCurrency.format(cost),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        val progressFraction = (cost / maxCost).toFloat().coerceIn(0.05f, 1f)
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                        ) {
                            // background track
                            drawRoundRect(
                                color = color.copy(alpha = 0.15f),
                                size = Size(size.width, size.height),
                                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                            )
                            // progress bar
                            drawRoundRect(
                                color = color,
                                size = Size(size.width * progressFraction, size.height),
                                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                            )
                        }
                    }
                }
            }
        }

        // Adjustable Simulation Parameters
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ajustar Parâmetros da Simulação",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Monthly km slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Quilometragem Mensal Estimada:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${monthlyKm.toInt()} km/mês",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = monthlyKm,
                        onValueChange = { monthlyKm = it },
                        valueRange = 400f..3500f,
                        steps = 30,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Prices: Gasoline & Electricity
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = gasolinePriceText,
                        onValueChange = { gasolinePriceText = it },
                        label = { Text("Gasolina (R$/L)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = AmberGlow, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = electricityKwhPriceText,
                        onValueChange = { electricityKwhPriceText = it },
                        label = { Text("Energia (R$/kWh)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = GreenAlert, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tarifa residencial média no Brasil varia entre R$ 0,75 e R$ 1,15 por kWh (com impostos).",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonRow(
    label: String,
    userVal: String,
    benchmarkVal: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1.2f)
        )
        Text(
            text = userVal,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = benchmarkVal,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (highlight) GreenAlert else MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.3f)
        )
    }
}
