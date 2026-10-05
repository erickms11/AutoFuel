package com.example.model

import com.example.data.FuelLogEntity
import com.example.data.MaintenanceItemEntity
import kotlin.math.max

enum class MaintenanceStatusLevel {
    OVERDUE,   // Vencido (Vermelho)
    DUE_SOON,  // Atenção / Próximo (Amarelo)
    OK         // Em dia (Verde)
}

data class FuelRefillWithStats(
    val log: FuelLogEntity,
    val distanceTraveledKm: Double?,
    val kmPerLiter: Double?,
    val costPerKm: Double?
)

data class VehicleDashboardStats(
    val totalDistanceLoggedKm: Double = 0.0,
    val averageKmPerLiter: Double = 0.0,
    val averageCostPerKm: Double = 0.0,
    val totalFuelSpent: Double = 0.0,
    val totalLitersFilled: Double = 0.0,
    val lastKmPerLiter: Double? = null,
    val gasolineAvgKmPerL: Double? = null,
    val ethanolAvgKmPerL: Double? = null,
    val monthlySpent: Double = 0.0,
    val overdueCount: Int = 0,
    val dueSoonCount: Int = 0
)

data class MaintenanceItemWithStatus(
    val item: MaintenanceItemEntity,
    val targetKm: Double,
    val targetDateEpochMillis: Long,
    val remainingKm: Double,
    val remainingDays: Long,
    val progress: Float, // 0.0f (brand new) to 1.0f (reached/overdue)
    val status: MaintenanceStatusLevel,
    val statusMessage: String
) {
    companion object {
        fun compute(item: MaintenanceItemEntity, currentVehicleKm: Double, currentTimeMillis: Long): MaintenanceItemWithStatus {
            val targetKm = item.lastServiceKm + item.intervalKm
            val intervalMillis = item.intervalMonths.toLong() * 30L * 24L * 60L * 60L * 1000L
            val targetDateMillis = item.lastServiceDateEpochMillis + intervalMillis

            val remainingKm = targetKm - currentVehicleKm
            val remainingMillis = targetDateMillis - currentTimeMillis
            val remainingDays = remainingMillis / (1000L * 60L * 60L * 24L)

            // Calculate progress based on whichever is closer (km or time)
            val kmUsed = currentVehicleKm - item.lastServiceKm
            val kmProgress = if (item.intervalKm > 0) (kmUsed / item.intervalKm.toDouble()).coerceIn(0.0, 1.5) else 0.0

            val timeUsed = currentTimeMillis - item.lastServiceDateEpochMillis
            val timeProgress = if (intervalMillis > 0) (timeUsed.toDouble() / intervalMillis.toDouble()).coerceIn(0.0, 1.5) else 0.0

            val maxProgress = max(kmProgress, timeProgress).toFloat().coerceIn(0f, 1f)

            val status: MaintenanceStatusLevel
            val message: String

            if (remainingKm <= 0 || remainingDays <= 0) {
                status = MaintenanceStatusLevel.OVERDUE
                val overdueKm = -remainingKm
                val overdueDays = -remainingDays
                message = when {
                    remainingKm <= 0 && remainingDays <= 0 ->
                        "Vencido há ${overdueKm.toInt()} km e ${overdueDays} dias!"
                    remainingKm <= 0 ->
                        "Vencido há ${overdueKm.toInt()} km!"
                    else ->
                        "Vencido há ${overdueDays} dias!"
                }
            } else if (remainingKm <= 1000 || remainingDays <= 15) {
                status = MaintenanceStatusLevel.DUE_SOON
                message = when {
                    remainingKm <= 1000 && remainingDays <= 15 ->
                        "Atenção: faltam ${remainingKm.toInt()} km ou ${remainingDays} dias"
                    remainingKm <= 1000 ->
                        "Atenção: faltam apenas ${remainingKm.toInt()} km"
                    else ->
                        "Atenção: faltam apenas ${remainingDays} dias"
                }
            } else {
                status = MaintenanceStatusLevel.OK
                message = "Em dia (faltam ${remainingKm.toInt()} km ou ${remainingDays} dias)"
            }

            return MaintenanceItemWithStatus(
                item = item,
                targetKm = targetKm,
                targetDateEpochMillis = targetDateMillis,
                remainingKm = remainingKm,
                remainingDays = remainingDays,
                progress = maxProgress,
                status = status,
                statusMessage = message
            )
        }
    }
}
