package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "maintenance_items",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["vehicleId"])]
)
data class MaintenanceItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehicleId: Long,
    val title: String,
    val category: String, // Motor, Pneus, Freios, Filtros, Arrefecimento, Elétrica, Outro
    val intervalKm: Int, // e.g. 10000
    val intervalMonths: Int, // e.g. 6 or 12
    val lastServiceKm: Double,
    val lastServiceDateEpochMillis: Long,
    val notes: String = ""
)

@Entity(
    tableName = "maintenance_history",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["vehicleId"]), Index(value = ["maintenanceItemId"])]
)
data class MaintenanceHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val maintenanceItemId: Long,
    val vehicleId: Long,
    val title: String,
    val serviceKm: Double,
    val serviceDateEpochMillis: Long = System.currentTimeMillis(),
    val cost: Double = 0.0,
    val workshop: String = "",
    val notes: String = ""
)
