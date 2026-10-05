package com.example.data

import com.example.model.FuelRefillWithStats
import com.example.model.MaintenanceItemWithStatus
import com.example.model.MaintenanceStatusLevel
import com.example.model.VehicleDashboardStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.Calendar

class FuelRepository(private val dao: FuelDao) {

    val allVehicles: Flow<List<VehicleEntity>> = dao.getAllVehicles()
    val activeVehicle: Flow<VehicleEntity?> = dao.getActiveVehicle()

    fun getFuelLogs(vehicleId: Long): Flow<List<FuelLogEntity>> =
        dao.getFuelLogsForVehicle(vehicleId)

    fun getMaintenanceItems(vehicleId: Long): Flow<List<MaintenanceItemEntity>> =
        dao.getMaintenanceItemsForVehicle(vehicleId)

    fun getMaintenanceHistory(vehicleId: Long): Flow<List<MaintenanceHistoryEntity>> =
        dao.getMaintenanceHistoryForVehicle(vehicleId)

    // Compute detailed list of refueling logs with km/L and cost/km
    fun getFuelRefillsWithStats(vehicleId: Long): Flow<List<FuelRefillWithStats>> {
        return dao.getFuelLogsForVehicle(vehicleId).map { logs ->
            // logs are sorted DESC by odometerKm
            // To calculate consumption between refills, we need the chronologically previous log
            val sortedAsc = logs.sortedBy { it.odometerKm }
            val statsMap = mutableMapOf<Long, Triple<Double?, Double?, Double?>>()

            var previousFullTankLog: FuelLogEntity? = null
            var fuelAccumulator = 0.0

            for (log in sortedAsc) {
                fuelAccumulator += log.liters
                if (previousFullTankLog != null && log.isFullTank) {
                    val dist = log.odometerKm - previousFullTankLog.odometerKm
                    if (dist > 0 && fuelAccumulator > 0) {
                        val kmL = dist / fuelAccumulator
                        val costPerKm = log.totalCost / dist
                        statsMap[log.id] = Triple(dist, kmL, costPerKm)
                    }
                    fuelAccumulator = 0.0
                    previousFullTankLog = log
                } else if (previousFullTankLog == null && log.isFullTank) {
                    previousFullTankLog = log
                    fuelAccumulator = 0.0
                    statsMap[log.id] = Triple(null, null, null)
                } else {
                    statsMap[log.id] = Triple(null, null, null)
                }
            }

            logs.map { log ->
                val stats = statsMap[log.id]
                FuelRefillWithStats(
                    log = log,
                    distanceTraveledKm = stats?.first,
                    kmPerLiter = stats?.second,
                    costPerKm = stats?.third
                )
            }
        }
    }

    // Compute comprehensive dashboard statistics
    fun getDashboardStats(vehicleId: Long, currentVehicleKm: Double): Flow<VehicleDashboardStats> {
        return dao.getFuelLogsForVehicle(vehicleId).map { logs ->
            if (logs.isEmpty()) {
                return@map VehicleDashboardStats()
            }

            val totalFuelSpent = logs.sumOf { it.totalCost }
            val totalLiters = logs.sumOf { it.liters }

            val sortedAsc = logs.sortedBy { it.odometerKm }
            var totalValidDist = 0.0
            var totalValidLiters = 0.0

            var prevFullTank: FuelLogEntity? = null
            var accumulatedLiters = 0.0
            var latestKmL: Double? = null

            // Categorized stats for Flex comparison
            var gasDist = 0.0
            var gasLiters = 0.0
            var ethanolDist = 0.0
            var ethanolLiters = 0.0

            for (log in sortedAsc) {
                accumulatedLiters += log.liters
                if (prevFullTank != null && log.isFullTank) {
                    val dist = log.odometerKm - prevFullTank.odometerKm
                    if (dist > 0 && accumulatedLiters > 0) {
                        val kmL = dist / accumulatedLiters
                        latestKmL = kmL
                        totalValidDist += dist
                        totalValidLiters += accumulatedLiters

                        if (log.fuelType.contains("Gasolina", ignoreCase = true)) {
                            gasDist += dist
                            gasLiters += accumulatedLiters
                        } else if (log.fuelType.contains("Etanol", ignoreCase = true)) {
                            ethanolDist += dist
                            ethanolLiters += accumulatedLiters
                        }
                    }
                    accumulatedLiters = 0.0
                    prevFullTank = log
                } else if (prevFullTank == null && log.isFullTank) {
                    prevFullTank = log
                    accumulatedLiters = 0.0
                }
            }

            val averageKmL = if (totalValidLiters > 0) totalValidDist / totalValidLiters else 0.0
            val averageCostKm = if (totalValidDist > 0 && totalFuelSpent > 0) totalFuelSpent / totalValidDist else 0.0

            // Current month spent
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)
            val monthlySpent = logs.filter {
                val cal = Calendar.getInstance().apply { timeInMillis = it.dateEpochMillis }
                cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
            }.sumOf { it.totalCost }

            val gasAvg = if (gasLiters > 0) gasDist / gasLiters else null
            val ethanolAvg = if (ethanolLiters > 0) ethanolDist / ethanolLiters else null

            VehicleDashboardStats(
                totalDistanceLoggedKm = totalValidDist,
                averageKmPerLiter = averageKmL,
                averageCostPerKm = averageCostKm,
                totalFuelSpent = totalFuelSpent,
                totalLitersFilled = totalLiters,
                lastKmPerLiter = latestKmL,
                gasolineAvgKmPerL = gasAvg,
                ethanolAvgKmPerL = ethanolAvg,
                monthlySpent = monthlySpent
            )
        }
    }

    suspend fun insertVehicle(vehicle: VehicleEntity): Long {
        return dao.insertVehicle(vehicle)
    }

    suspend fun updateVehicle(vehicle: VehicleEntity) {
        dao.updateVehicle(vehicle)
    }

    suspend fun setActiveVehicle(id: Long) {
        dao.setActiveVehicle(id)
    }

    suspend fun deleteVehicle(vehicle: VehicleEntity) {
        dao.deleteVehicle(vehicle)
    }

    suspend fun addFuelLog(log: FuelLogEntity): Long {
        val id = dao.insertFuelLog(log)
        dao.updateVehicleOdometerIfHigher(log.vehicleId, log.odometerKm)
        return id
    }

    suspend fun deleteFuelLog(log: FuelLogEntity) {
        dao.deleteFuelLog(log)
    }

    suspend fun addMaintenanceItem(item: MaintenanceItemEntity): Long {
        return dao.insertMaintenanceItem(item)
    }

    suspend fun updateMaintenanceItem(item: MaintenanceItemEntity) {
        dao.updateMaintenanceItem(item)
    }

    suspend fun deleteMaintenanceItem(item: MaintenanceItemEntity) {
        dao.deleteMaintenanceItem(item)
    }

    suspend fun recordMaintenancePerformed(
        item: MaintenanceItemEntity,
        serviceKm: Double,
        serviceDateMillis: Long,
        cost: Double,
        workshop: String,
        notes: String
    ) {
        val history = MaintenanceHistoryEntity(
            maintenanceItemId = item.id,
            vehicleId = item.vehicleId,
            title = item.title,
            serviceKm = serviceKm,
            serviceDateEpochMillis = serviceDateMillis,
            cost = cost,
            workshop = workshop,
            notes = notes
        )
        dao.insertMaintenanceHistory(history)

        val updatedItem = item.copy(
            lastServiceKm = serviceKm,
            lastServiceDateEpochMillis = serviceDateMillis
        )
        dao.updateMaintenanceItem(updatedItem)
        dao.updateVehicleOdometerIfHigher(item.vehicleId, serviceKm)
    }

    suspend fun seedInitialDataIfEmpty() {
        val existing = dao.getAllVehicles().firstOrNull()
        if (!existing.isNullOrEmpty()) return

        val now = System.currentTimeMillis()
        val oneMonthAgo = now - 30L * 24 * 3600 * 1000
        val twoMonthsAgo = now - 60L * 24 * 3600 * 1000
        val sixMonthsAgo = now - 180L * 24 * 3600 * 1000

        val initialVehicle = VehicleEntity(
            name = "Chevrolet Onix 1.0 Flex",
            brand = "Chevrolet",
            modelYear = 2022,
            plate = "ABC1D23",
            currentOdometerKm = 34500.0,
            fuelCapacityLiters = 44.0,
            preferredFuel = "Gasolina Comum",
            isActive = true,
            createdAt = now
        )
        val vehicleId = dao.insertVehicle(initialVehicle)

        // Seed Fuel Logs (Realistic fuel history with full tank refills for accurate km/L)
        val fuelLogs = listOf(
            FuelLogEntity(
                vehicleId = vehicleId,
                odometerKm = 33200.0,
                liters = 38.5,
                pricePerLiter = 5.79,
                totalCost = 222.91,
                fuelType = "Gasolina Comum",
                isFullTank = true,
                dateEpochMillis = twoMonthsAgo,
                gasStationName = "Posto Shell Avenida",
                notes = "Encheu o tanque para viagem"
            ),
            FuelLogEntity(
                vehicleId = vehicleId,
                odometerKm = 33850.0,
                liters = 40.2,
                pricePerLiter = 5.85,
                totalCost = 235.17,
                fuelType = "Gasolina Comum",
                isFullTank = true,
                dateEpochMillis = oneMonthAgo,
                gasStationName = "Posto Ipiranga Centro",
                notes = "Uso misto cidade/estrada (650 km rodados com 40.2L = ~16.1 km/L)"
            ),
            FuelLogEntity(
                vehicleId = vehicleId,
                odometerKm = 34500.0,
                liters = 42.0,
                pricePerLiter = 5.89,
                totalCost = 247.38,
                fuelType = "Gasolina Aditivada",
                isFullTank = true,
                dateEpochMillis = now - 3L * 24 * 3600 * 1000,
                gasStationName = "Posto Petrobras Rodovia",
                notes = "Tanque cheio (650 km com 42L = ~15.4 km/L)"
            )
        )
        for (log in fuelLogs) {
            dao.insertFuelLog(log)
        }

        // Seed Standard Maintenance Schedule Items
        val maintenanceItems = listOf(
            MaintenanceItemEntity(
                vehicleId = vehicleId,
                title = "Troca de Óleo do Motor e Filtro",
                category = "Motor",
                intervalKm = 10000,
                intervalMonths = 6,
                lastServiceKm = 25000.0,
                lastServiceDateEpochMillis = sixMonthsAgo,
                notes = "Usar óleo sintético 5W30 aprovado pela montadora"
            ),
            MaintenanceItemEntity(
                vehicleId = vehicleId,
                title = "Filtro de Ar do Motor",
                category = "Filtros",
                intervalKm = 10000,
                intervalMonths = 12,
                lastServiceKm = 25000.0,
                lastServiceDateEpochMillis = sixMonthsAgo,
                notes = "Verificar acúmulo de poeira e trocar se necessário"
            ),
            MaintenanceItemEntity(
                vehicleId = vehicleId,
                title = "Rodízio e Balanceamento dos Pneus",
                category = "Pneus",
                intervalKm = 10000,
                intervalMonths = 6,
                lastServiceKm = 30000.0,
                lastServiceDateEpochMillis = oneMonthAgo,
                notes = "Rodízio em X e calibração periódica (32 PSI)"
            ),
            MaintenanceItemEntity(
                vehicleId = vehicleId,
                title = "Pastilhas e Discos de Freio",
                category = "Freios",
                intervalKm = 25000,
                intervalMonths = 24,
                lastServiceKm = 10000.0,
                lastServiceDateEpochMillis = now - 400L * 24 * 3600 * 1000,
                notes = "Checar espessura da pastilha e fluído DOT 4"
            ),
            MaintenanceItemEntity(
                vehicleId = vehicleId,
                title = "Filtro do Ar-Condicionado / Cabine",
                category = "Ar e Conforto",
                intervalKm = 10000,
                intervalMonths = 12,
                lastServiceKm = 25000.0,
                lastServiceDateEpochMillis = sixMonthsAgo,
                notes = "Higienização e troca do elemento filtrante"
            ),
            MaintenanceItemEntity(
                vehicleId = vehicleId,
                title = "Correia Dentada e Tensor",
                category = "Motor",
                intervalKm = 50000,
                intervalMonths = 48,
                lastServiceKm = 0.0,
                lastServiceDateEpochMillis = now - 500L * 24 * 3600 * 1000,
                notes = "Item crítico! Troca preventiva aos 50.000 km"
            )
        )
        dao.insertMaintenanceItems(maintenanceItems)
    }

    suspend fun clearAllData(createStarterVehicle: Boolean = false) {
        dao.clearAllFuelLogs()
        dao.clearAllMaintenanceHistory()
        dao.clearAllMaintenanceItems()
        dao.clearAllVehicles()

        if (createStarterVehicle) {
            val starter = VehicleEntity(
                name = "Meu Veículo",
                brand = "",
                modelYear = Calendar.getInstance().get(Calendar.YEAR),
                plate = "",
                currentOdometerKm = 0.0,
                fuelCapacityLiters = 50.0,
                preferredFuel = "Gasolina Comum",
                isActive = true,
                createdAt = System.currentTimeMillis()
            )
            dao.insertVehicle(starter)
        }
    }

    suspend fun resetToDemoData() {
        clearAllData(createStarterVehicle = false)
        seedInitialDataIfEmpty()
    }

    suspend fun exportDataAsJson(): String {
        val vehicles = dao.getAllVehiclesList()
        val fuelLogs = dao.getAllFuelLogsList()
        val maintenanceItems = dao.getAllMaintenanceItemsList()
        val maintenanceHistory = dao.getAllMaintenanceHistoryList()

        val root = org.json.JSONObject()
        root.put("app", "AutoFuel")
        root.put("version", "1.0")
        val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
        root.put("exportDate", isoFormat.format(java.util.Date()))
        root.put("exportTimestampMillis", System.currentTimeMillis())

        val statsObj = org.json.JSONObject()
        statsObj.put("totalVehicles", vehicles.size)
        statsObj.put("totalFuelLogs", fuelLogs.size)
        statsObj.put("totalMaintenanceItems", maintenanceItems.size)
        statsObj.put("totalMaintenanceHistoryRecords", maintenanceHistory.size)
        root.put("summary", statsObj)

        val vehiclesArray = org.json.JSONArray()
        for (v in vehicles) {
            val vObj = org.json.JSONObject()
            vObj.put("id", v.id)
            vObj.put("name", v.name)
            vObj.put("brand", v.brand)
            vObj.put("modelYear", v.modelYear)
            vObj.put("plate", v.plate)
            vObj.put("currentOdometerKm", v.currentOdometerKm)
            vObj.put("fuelCapacityLiters", v.fuelCapacityLiters)
            vObj.put("preferredFuel", v.preferredFuel)
            vObj.put("isActive", v.isActive)
            vObj.put("createdAt", v.createdAt)
            vehiclesArray.put(vObj)
        }
        root.put("vehicles", vehiclesArray)

        val logsArray = org.json.JSONArray()
        for (l in fuelLogs) {
            val lObj = org.json.JSONObject()
            lObj.put("id", l.id)
            lObj.put("vehicleId", l.vehicleId)
            lObj.put("odometerKm", l.odometerKm)
            lObj.put("liters", l.liters)
            lObj.put("pricePerLiter", l.pricePerLiter)
            lObj.put("totalCost", l.totalCost)
            lObj.put("fuelType", l.fuelType)
            lObj.put("isFullTank", l.isFullTank)
            lObj.put("dateEpochMillis", l.dateEpochMillis)
            lObj.put("gasStationName", l.gasStationName)
            lObj.put("notes", l.notes)
            logsArray.put(lObj)
        }
        root.put("fuelLogs", logsArray)

        val maintArray = org.json.JSONArray()
        for (m in maintenanceItems) {
            val mObj = org.json.JSONObject()
            mObj.put("id", m.id)
            mObj.put("vehicleId", m.vehicleId)
            mObj.put("title", m.title)
            mObj.put("category", m.category)
            mObj.put("intervalKm", m.intervalKm)
            mObj.put("intervalMonths", m.intervalMonths)
            mObj.put("lastServiceKm", m.lastServiceKm)
            mObj.put("lastServiceDateEpochMillis", m.lastServiceDateEpochMillis)
            mObj.put("notes", m.notes)
            maintArray.put(mObj)
        }
        root.put("maintenanceItems", maintArray)

        val histArray = org.json.JSONArray()
        for (h in maintenanceHistory) {
            val hObj = org.json.JSONObject()
            hObj.put("id", h.id)
            hObj.put("maintenanceItemId", h.maintenanceItemId)
            hObj.put("vehicleId", h.vehicleId)
            hObj.put("title", h.title)
            hObj.put("serviceKm", h.serviceKm)
            hObj.put("serviceDateEpochMillis", h.serviceDateEpochMillis)
            hObj.put("cost", h.cost)
            hObj.put("workshop", h.workshop)
            hObj.put("notes", h.notes)
            histArray.put(hObj)
        }
        root.put("maintenanceHistory", histArray)

        return root.toString(2)
    }
}
