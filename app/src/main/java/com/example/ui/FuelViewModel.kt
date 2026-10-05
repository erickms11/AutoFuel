package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FuelLogEntity
import com.example.data.FuelRepository
import com.example.data.MaintenanceHistoryEntity
import com.example.data.MaintenanceItemEntity
import com.example.data.VehicleEntity
import com.example.model.FuelRefillWithStats
import com.example.model.MaintenanceItemWithStatus
import com.example.model.MaintenanceStatusLevel
import com.example.model.VehicleDashboardStats
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class FuelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FuelRepository

    val vehicles: StateFlow<List<VehicleEntity>>
    val activeVehicle: StateFlow<VehicleEntity?>
    val fuelLogsWithStats: StateFlow<List<FuelRefillWithStats>>
    val maintenanceItemsWithStatus: StateFlow<List<MaintenanceItemWithStatus>>
    val maintenanceHistory: StateFlow<List<MaintenanceHistoryEntity>>
    val dashboardStats: StateFlow<VehicleDashboardStats>

    private val _messageSnackbar = MutableStateFlow<String?>(null)
    val messageSnackbar: StateFlow<String?> = _messageSnackbar.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FuelRepository(database.fuelDao())

        // Seed default vehicle and maintenance if DB is empty
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        vehicles = repository.allVehicles.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        activeVehicle = repository.activeVehicle.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        fuelLogsWithStats = activeVehicle.flatMapLatest { vehicle: VehicleEntity? ->
            if (vehicle != null) {
                repository.getFuelRefillsWithStats(vehicle.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        maintenanceItemsWithStatus = activeVehicle.flatMapLatest { vehicle: VehicleEntity? ->
            if (vehicle != null) {
                repository.getMaintenanceItems(vehicle.id).map { items: List<MaintenanceItemEntity> ->
                    val now = System.currentTimeMillis()
                    val mapped = items.map { item ->
                        MaintenanceItemWithStatus.compute(item, vehicle.currentOdometerKm, now)
                    }
                    mapped.sortedWith(Comparator { a, b ->
                        val priorityA = when (a.status) {
                            MaintenanceStatusLevel.OVERDUE -> 0
                            MaintenanceStatusLevel.DUE_SOON -> 1
                            MaintenanceStatusLevel.OK -> 2
                        }
                        val priorityB = when (b.status) {
                            MaintenanceStatusLevel.OVERDUE -> 0
                            MaintenanceStatusLevel.DUE_SOON -> 1
                            MaintenanceStatusLevel.OK -> 2
                        }
                        val comp = priorityA.compareTo(priorityB)
                        if (comp != 0) comp else a.remainingKm.compareTo(b.remainingKm)
                    })
                }
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        maintenanceHistory = activeVehicle.flatMapLatest { vehicle: VehicleEntity? ->
            if (vehicle != null) {
                repository.getMaintenanceHistory(vehicle.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        val rawStats: Flow<VehicleDashboardStats> = activeVehicle.flatMapLatest { vehicle: VehicleEntity? ->
            if (vehicle != null) {
                repository.getDashboardStats(vehicle.id, vehicle.currentOdometerKm)
            } else {
                flowOf(VehicleDashboardStats())
            }
        }

        dashboardStats = combine(
            rawStats,
            maintenanceItemsWithStatus
        ) { stats: VehicleDashboardStats, maintItems: List<MaintenanceItemWithStatus> ->
            val overdue = maintItems.count { it.status == MaintenanceStatusLevel.OVERDUE }
            val dueSoon = maintItems.count { it.status == MaintenanceStatusLevel.DUE_SOON }
            stats.copy(overdueCount = overdue, dueSoonCount = dueSoon)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = VehicleDashboardStats()
        )
    }

    fun clearSnackbar() {
        _messageSnackbar.value = null
    }

    fun showMessage(msg: String) {
        _messageSnackbar.value = msg
    }

    fun switchActiveVehicle(vehicleId: Long) {
        viewModelScope.launch {
            repository.setActiveVehicle(vehicleId)
        }
    }

    fun addVehicle(
        name: String,
        brand: String,
        year: Int,
        plate: String,
        odometer: Double,
        capacity: Double,
        preferredFuel: String
    ) {
        viewModelScope.launch {
            val newVehicle = VehicleEntity(
                name = name,
                brand = brand,
                modelYear = year,
                plate = plate.uppercase(),
                currentOdometerKm = odometer,
                fuelCapacityLiters = capacity,
                preferredFuel = preferredFuel,
                isActive = true
            )
            val id = repository.insertVehicle(newVehicle)
            repository.setActiveVehicle(id)

            // Seed basic standard maintenance for this new vehicle
            val standardItems = listOf(
                MaintenanceItemEntity(
                    vehicleId = id,
                    title = "Troca de Óleo do Motor e Filtro",
                    category = "Motor",
                    intervalKm = 10000,
                    intervalMonths = 6,
                    lastServiceKm = odometer,
                    lastServiceDateEpochMillis = System.currentTimeMillis()
                ),
                MaintenanceItemEntity(
                    vehicleId = id,
                    title = "Filtro de Ar e Combustível",
                    category = "Filtros",
                    intervalKm = 15000,
                    intervalMonths = 12,
                    lastServiceKm = odometer,
                    lastServiceDateEpochMillis = System.currentTimeMillis()
                ),
                MaintenanceItemEntity(
                    vehicleId = id,
                    title = "Rodízio e Alinhamento de Pneus",
                    category = "Pneus",
                    intervalKm = 10000,
                    intervalMonths = 6,
                    lastServiceKm = odometer,
                    lastServiceDateEpochMillis = System.currentTimeMillis()
                )
            )
            for (item in standardItems) {
                repository.addMaintenanceItem(item)
            }
            showMessage("Veículo '$name' cadastrado com sucesso!")
        }
    }

    fun updateVehicle(vehicle: VehicleEntity) {
        viewModelScope.launch {
            repository.updateVehicle(vehicle)
            showMessage("Veículo atualizado!")
        }
    }

    fun addFuelLog(
        odometerKm: Double,
        liters: Double,
        pricePerLiter: Double,
        totalCost: Double,
        fuelType: String,
        isFullTank: Boolean,
        dateMillis: Long,
        gasStation: String,
        notes: String
    ) {
        val vehicle = activeVehicle.value ?: return
        viewModelScope.launch {
            val log = FuelLogEntity(
                vehicleId = vehicle.id,
                odometerKm = odometerKm,
                liters = liters,
                pricePerLiter = pricePerLiter,
                totalCost = totalCost,
                fuelType = fuelType,
                isFullTank = isFullTank,
                dateEpochMillis = dateMillis,
                gasStationName = gasStation,
                notes = notes
            )
            repository.addFuelLog(log)
            showMessage("Abastecimento registrado com sucesso!")
        }
    }

    fun deleteFuelLog(log: FuelLogEntity) {
        viewModelScope.launch {
            repository.deleteFuelLog(log)
            showMessage("Abastecimento removido.")
        }
    }

    fun addMaintenanceItem(
        title: String,
        category: String,
        intervalKm: Int,
        intervalMonths: Int,
        lastKm: Double,
        lastDateMillis: Long,
        notes: String
    ) {
        val vehicle = activeVehicle.value ?: return
        viewModelScope.launch {
            val item = MaintenanceItemEntity(
                vehicleId = vehicle.id,
                title = title,
                category = category,
                intervalKm = intervalKm,
                intervalMonths = intervalMonths,
                lastServiceKm = lastKm,
                lastServiceDateEpochMillis = lastDateMillis,
                notes = notes
            )
            repository.addMaintenanceItem(item)
            showMessage("Alerta de manutenção '$title' adicionado!")
        }
    }

    fun updateMaintenanceItem(item: MaintenanceItemEntity) {
        viewModelScope.launch {
            repository.updateMaintenanceItem(item)
            showMessage("Item de manutenção atualizado!")
        }
    }

    fun deleteMaintenanceItem(item: MaintenanceItemEntity) {
        viewModelScope.launch {
            repository.deleteMaintenanceItem(item)
            showMessage("Item de manutenção excluído.")
        }
    }

    fun recordMaintenancePerformed(
        item: MaintenanceItemEntity,
        serviceKm: Double,
        serviceDateMillis: Long,
        cost: Double,
        workshop: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.recordMaintenancePerformed(
                item = item,
                serviceKm = serviceKm,
                serviceDateMillis = serviceDateMillis,
                cost = cost,
                workshop = workshop,
                notes = notes
            )
            showMessage("Manutenção '${item.title}' concluída e renovada!")
        }
    }

    suspend fun getExportJson(): String {
        return repository.exportDataAsJson()
    }

    fun clearAllData(createStarterVehicle: Boolean = true) {
        viewModelScope.launch {
            repository.clearAllData(createStarterVehicle)
            showMessage("Todos os dados foram apagados com sucesso.")
        }
    }

    fun resetToDemoData() {
        viewModelScope.launch {
            repository.resetToDemoData()
            showMessage("Dados de demonstração restaurados!")
        }
    }
}
