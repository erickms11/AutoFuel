package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fuel_logs",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["vehicleId"]), Index(value = ["odometerKm"])]
)
data class FuelLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehicleId: Long,
    val odometerKm: Double,
    val liters: Double,
    val pricePerLiter: Double,
    val totalCost: Double,
    val fuelType: String,
    val isFullTank: Boolean = true,
    val dateEpochMillis: Long = System.currentTimeMillis(),
    val gasStationName: String = "",
    val notes: String = ""
)
