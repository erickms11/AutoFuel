package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VehicleEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparatorContainerScreen(
    activeVehicle: VehicleEntity?,
    userAverageKmL: Double?,
    gasAvgKmL: Double?,
    ethanolAvgKmL: Double?,
    tankCapacity: Double = 50.0,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember(initialTab) { mutableIntStateOf(initialTab) }
    val tabTitles = listOf("Mercado (Elétricos & Híbridos)", "Flex (Etanol x Gasolina)")

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().testTag("comparator_tab_row")
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Text(
                        text = "Elétricos & Híbridos",
                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.ElectricCar,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.testTag("tab_market_comparison")
            )

            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Text(
                        text = "Calculadora Flex",
                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.testTag("tab_flex_calculator")
            )
        }

        when (selectedTabIndex) {
            0 -> {
                MarketComparisonScreen(
                    activeVehicle = activeVehicle,
                    userAverageKmL = userAverageKmL,
                    modifier = Modifier.weight(1f)
                )
            }
            1 -> {
                FlexCalculatorScreen(
                    vehicleGasAvgKmL = gasAvgKmL ?: (if (userAverageKmL != null && userAverageKmL > 0) userAverageKmL else 14.5),
                    vehicleEthanolAvgKmL = ethanolAvgKmL ?: (if (userAverageKmL != null && userAverageKmL > 0) userAverageKmL * 0.70 else 10.1),
                    tankCapacity = tankCapacity,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
