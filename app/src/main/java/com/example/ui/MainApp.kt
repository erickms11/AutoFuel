package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.MaintenanceItemEntity
import com.example.ui.dialogs.AddFuelDialog
import com.example.ui.dialogs.AddMaintenanceItemDialog
import com.example.ui.dialogs.DataManagementDialog
import com.example.ui.dialogs.RecordMaintenanceDialog
import com.example.ui.dialogs.UpdateOdometerDialog
import com.example.ui.dialogs.VehicleManageDialog
import com.example.ui.screens.ComparatorContainerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FlexCalculatorScreen
import com.example.ui.screens.FuelHistoryScreen
import com.example.ui.screens.MaintenanceScreen

enum class AppTab(val title: String) {
    DASHBOARD("Painel"),
    FUEL_HISTORY("Abastecimentos"),
    MAINTENANCE("Manutenção"),
    COMPARATOR("Comparador")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: FuelViewModel = viewModel()
) {
    val activeVehicle by viewModel.activeVehicle.collectAsStateWithLifecycle()
    val allVehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val fuelLogs by viewModel.fuelLogsWithStats.collectAsStateWithLifecycle()
    val maintenanceItems by viewModel.maintenanceItemsWithStatus.collectAsStateWithLifecycle()
    val maintenanceHistory by viewModel.maintenanceHistory.collectAsStateWithLifecycle()
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.messageSnackbar.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    var comparatorSubTab by remember { mutableIntStateOf(0) }

    // Dialog controllers
    var showAddFuelDialog by remember { mutableStateOf(false) }
    var itemToRecordMaintenance by remember { mutableStateOf<MaintenanceItemEntity?>(null) }
    var itemToEditMaintenance by remember { mutableStateOf<MaintenanceItemEntity?>(null) }
    var showAddMaintenanceItemDialog by remember { mutableStateOf(false) }
    var showVehicleManageDialog by remember { mutableStateOf(false) }
    var showUpdateOdometerDialog by remember { mutableStateOf(false) }
    var showDataManagementDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    // Android back button handling: Return to dashboard if inside other tabs
    BackHandler(enabled = currentTab != AppTab.DASHBOARD) {
        currentTab = AppTab.DASHBOARD
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentTab.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // Active vehicle chip in app bar
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showVehicleManageDialog = true }
                            .testTag("appbar_vehicle_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = activeVehicle?.name?.take(14) ?: "Veículo",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Trocar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Data management & JSON export button in app bar
                    IconButton(
                        onClick = { showDataManagementDialog = true },
                        modifier = Modifier.testTag("appbar_btn_data_management")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "Gestão de Dados",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding().testTag("main_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                // Tab 1: Dashboard
                NavigationBarItem(
                    selected = currentTab == AppTab.DASHBOARD,
                    onClick = { currentTab = AppTab.DASHBOARD },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                    label = { Text("Início", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )

                // Tab 2: Fuel History
                NavigationBarItem(
                    selected = currentTab == AppTab.FUEL_HISTORY,
                    onClick = { currentTab = AppTab.FUEL_HISTORY },
                    icon = { Icon(Icons.Default.LocalGasStation, contentDescription = "Abastecimentos") },
                    label = { Text("Abastecer", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_fuel")
                )

                // Tab 3: Maintenance Alerts
                val alertBadgeCount = stats.overdueCount + stats.dueSoonCount
                NavigationBarItem(
                    selected = currentTab == AppTab.MAINTENANCE,
                    onClick = { currentTab = AppTab.MAINTENANCE },
                    icon = {
                        if (alertBadgeCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = if (stats.overdueCount > 0)
                                            MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.secondary
                                    ) {
                                        Text("$alertBadgeCount")
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Build, contentDescription = "Manutenções")
                            }
                        } else {
                            Icon(Icons.Default.Build, contentDescription = "Manutenções")
                        }
                    },
                    label = { Text("Manutenção", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_maintenance")
                )

                // Tab 4: Comparator (Market & Flex)
                NavigationBarItem(
                    selected = currentTab == AppTab.COMPARATOR,
                    onClick = { currentTab = AppTab.COMPARATOR },
                    icon = { Icon(Icons.Default.ElectricCar, contentDescription = "Comparador") },
                    label = { Text("Comparar", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_comparator")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.DASHBOARD -> {
                    DashboardScreen(
                        vehicle = activeVehicle,
                        stats = stats,
                        recentRefills = fuelLogs,
                        onAddFuelClick = { showAddFuelDialog = true },
                        onMaintenanceClick = { currentTab = AppTab.MAINTENANCE },
                        onFlexCalculatorClick = {
                            comparatorSubTab = 1
                            currentTab = AppTab.COMPARATOR
                        },
                        onMarketComparisonClick = {
                            comparatorSubTab = 0
                            currentTab = AppTab.COMPARATOR
                        },
                        onSwitchVehicleClick = { showVehicleManageDialog = true },
                        onUpdateOdometerClick = { showUpdateOdometerDialog = true },
                        onViewAllRefillsClick = { currentTab = AppTab.FUEL_HISTORY },
                        onDataManagementClick = { showDataManagementDialog = true }
                    )
                }

                AppTab.FUEL_HISTORY -> {
                    FuelHistoryScreen(
                        refills = fuelLogs,
                        onAddFuelClick = { showAddFuelDialog = true },
                        onDeleteLog = { viewModel.deleteFuelLog(it) }
                    )
                }

                AppTab.MAINTENANCE -> {
                    MaintenanceScreen(
                        itemsWithStatus = maintenanceItems,
                        history = maintenanceHistory,
                        onAddMaintenanceClick = { showAddMaintenanceItemDialog = true },
                        onRecordPerformedClick = { itemToRecordMaintenance = it },
                        onEditItemClick = { itemToEditMaintenance = it }
                    )
                }

                AppTab.COMPARATOR -> {
                    ComparatorContainerScreen(
                        activeVehicle = activeVehicle,
                        userAverageKmL = stats.averageKmPerLiter,
                        gasAvgKmL = stats.gasolineAvgKmPerL,
                        ethanolAvgKmL = stats.ethanolAvgKmPerL,
                        tankCapacity = activeVehicle?.fuelCapacityLiters ?: 50.0,
                        initialTab = comparatorSubTab
                    )
                }
            }
        }
    }

    // Modal Dialogs
    if (showAddFuelDialog) {
        AddFuelDialog(
            vehicle = activeVehicle,
            onDismiss = { showAddFuelDialog = false },
            onConfirm = { odo, liters, pricePerLiter, totalCost, fuelType, isFullTank, station, notes ->
                viewModel.addFuelLog(
                    odometerKm = odo,
                    liters = liters,
                    pricePerLiter = pricePerLiter,
                    totalCost = totalCost,
                    fuelType = fuelType,
                    isFullTank = isFullTank,
                    dateMillis = System.currentTimeMillis(),
                    gasStation = station,
                    notes = notes
                )
                showAddFuelDialog = false
            }
        )
    }

    if (itemToRecordMaintenance != null) {
        RecordMaintenanceDialog(
            item = itemToRecordMaintenance!!,
            currentVehicleKm = activeVehicle?.currentOdometerKm ?: 0.0,
            onDismiss = { itemToRecordMaintenance = null },
            onConfirm = { km, dateMillis, cost, workshop, notes ->
                viewModel.recordMaintenancePerformed(
                    item = itemToRecordMaintenance!!,
                    serviceKm = km,
                    serviceDateMillis = dateMillis,
                    cost = cost,
                    workshop = workshop,
                    notes = notes
                )
                itemToRecordMaintenance = null
            }
        )
    }

    if (showAddMaintenanceItemDialog) {
        AddMaintenanceItemDialog(
            currentVehicleKm = activeVehicle?.currentOdometerKm ?: 0.0,
            onDismiss = { showAddMaintenanceItemDialog = false },
            onConfirm = { title, category, intervalKm, intervalMonths, lastKm, lastDate, notes ->
                viewModel.addMaintenanceItem(
                    title = title,
                    category = category,
                    intervalKm = intervalKm,
                    intervalMonths = intervalMonths,
                    lastKm = lastKm,
                    lastDateMillis = lastDate,
                    notes = notes
                )
                showAddMaintenanceItemDialog = false
            }
        )
    }

    if (itemToEditMaintenance != null) {
        AddMaintenanceItemDialog(
            initialItem = itemToEditMaintenance,
            currentVehicleKm = activeVehicle?.currentOdometerKm ?: 0.0,
            onDismiss = { itemToEditMaintenance = null },
            onDelete = {
                viewModel.deleteMaintenanceItem(it)
                itemToEditMaintenance = null
            },
            onConfirm = { title, category, intervalKm, intervalMonths, lastKm, lastDate, notes ->
                val updated = itemToEditMaintenance!!.copy(
                    title = title,
                    category = category,
                    intervalKm = intervalKm,
                    intervalMonths = intervalMonths,
                    lastServiceKm = lastKm,
                    notes = notes
                )
                viewModel.updateMaintenanceItem(updated)
                itemToEditMaintenance = null
            }
        )
    }

    if (showVehicleManageDialog) {
        VehicleManageDialog(
            vehicles = allVehicles,
            activeVehicle = activeVehicle,
            onDismiss = { showVehicleManageDialog = false },
            onSelectVehicle = { id ->
                viewModel.switchActiveVehicle(id)
                showVehicleManageDialog = false
            },
            onAddNewVehicle = { name, brand, year, plate, odometer, cap, fuel ->
                viewModel.addVehicle(name, brand, year, plate, odometer, cap, fuel)
                showVehicleManageDialog = false
            }
        )
    }

    if (showUpdateOdometerDialog) {
        UpdateOdometerDialog(
            currentKm = activeVehicle?.currentOdometerKm ?: 0.0,
            onDismiss = { showUpdateOdometerDialog = false },
            onConfirm = { newKm ->
                activeVehicle?.let { v ->
                    viewModel.updateVehicle(v.copy(currentOdometerKm = newKm))
                }
                showUpdateOdometerDialog = false
            }
        )
    }

    if (showDataManagementDialog) {
        DataManagementDialog(
            onDismiss = { showDataManagementDialog = false },
            onExportJsonRequest = { viewModel.getExportJson() },
            onClearAllData = { createStarter ->
                viewModel.clearAllData(createStarter)
            },
            onResetToDemoData = {
                viewModel.resetToDemoData()
            }
        )
    }
}
