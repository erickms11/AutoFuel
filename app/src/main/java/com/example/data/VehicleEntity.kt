package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val brand: String = "",
    val modelYear: Int = 2023,
    val plate: String = "",
    val currentOdometerKm: Double = 0.0,
    val fuelCapacityLiters: Double = 50.0,
    val preferredFuel: String = "Gasolina",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
