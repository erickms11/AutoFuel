package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelDao {

    // Vehicles
    @Query("SELECT * FROM vehicles ORDER BY id ASC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE isActive = 1 LIMIT 1")
    fun getActiveVehicle(): Flow<VehicleEntity?>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getVehicleById(id: Long): VehicleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity): Long

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Delete
    suspend fun deleteVehicle(vehicle: VehicleEntity)

    @Query("UPDATE vehicles SET isActive = CASE WHEN id = :selectedId THEN 1 ELSE 0 END")
    suspend fun setActiveVehicle(selectedId: Long)

    @Query("UPDATE vehicles SET currentOdometerKm = :newOdometer WHERE id = :vehicleId AND currentOdometerKm < :newOdometer")
    suspend fun updateVehicleOdometerIfHigher(vehicleId: Long, newOdometer: Double)

    // Fuel Logs
    @Query("SELECT * FROM fuel_logs WHERE vehicleId = :vehicleId ORDER BY odometerKm DESC, dateEpochMillis DESC")
    fun getFuelLogsForVehicle(vehicleId: Long): Flow<List<FuelLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelLog(log: FuelLogEntity): Long

    @Update
    suspend fun updateFuelLog(log: FuelLogEntity)

    @Delete
    suspend fun deleteFuelLog(log: FuelLogEntity)

    @Query("SELECT * FROM fuel_logs WHERE vehicleId = :vehicleId ORDER BY odometerKm DESC LIMIT 1")
    suspend fun getLatestFuelLog(vehicleId: Long): FuelLogEntity?

    // Maintenance Items
    @Query("SELECT * FROM maintenance_items WHERE vehicleId = :vehicleId ORDER BY title ASC")
    fun getMaintenanceItemsForVehicle(vehicleId: Long): Flow<List<MaintenanceItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenanceItem(item: MaintenanceItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenanceItems(items: List<MaintenanceItemEntity>)

    @Update
    suspend fun updateMaintenanceItem(item: MaintenanceItemEntity)

    @Delete
    suspend fun deleteMaintenanceItem(item: MaintenanceItemEntity)

    // Maintenance History
    @Query("SELECT * FROM maintenance_history WHERE vehicleId = :vehicleId ORDER BY serviceDateEpochMillis DESC")
    fun getMaintenanceHistoryForVehicle(vehicleId: Long): Flow<List<MaintenanceHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenanceHistory(history: MaintenanceHistoryEntity): Long

    // Data Export Queries
    @Query("SELECT * FROM vehicles ORDER BY id ASC")
    suspend fun getAllVehiclesList(): List<VehicleEntity>

    @Query("SELECT * FROM fuel_logs ORDER BY odometerKm DESC, dateEpochMillis DESC")
    suspend fun getAllFuelLogsList(): List<FuelLogEntity>

    @Query("SELECT * FROM maintenance_items ORDER BY id ASC")
    suspend fun getAllMaintenanceItemsList(): List<MaintenanceItemEntity>

    @Query("SELECT * FROM maintenance_history ORDER BY serviceDateEpochMillis DESC")
    suspend fun getAllMaintenanceHistoryList(): List<MaintenanceHistoryEntity>

    // Clear Data Queries
    @Query("DELETE FROM fuel_logs")
    suspend fun clearAllFuelLogs()

    @Query("DELETE FROM maintenance_history")
    suspend fun clearAllMaintenanceHistory()

    @Query("DELETE FROM maintenance_items")
    suspend fun clearAllMaintenanceItems()

    @Query("DELETE FROM vehicles")
    suspend fun clearAllVehicles()
}
