package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.MaintenanceItemEntity
import com.example.model.MaintenanceItemWithStatus
import com.example.model.MaintenanceStatusLevel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AutoFuel", appName)
    }

    @Test
    fun `maintenance overdue calculation is correct`() {
        val now = System.currentTimeMillis()
        val item = MaintenanceItemEntity(
            vehicleId = 1,
            title = "Troca de Óleo",
            category = "Motor",
            intervalKm = 10000,
            intervalMonths = 6,
            lastServiceKm = 20000.0,
            lastServiceDateEpochMillis = now
        )

        // Vehicle has 31000 km -> 1000 km overdue!
        val status = MaintenanceItemWithStatus.compute(item, 31000.0, now)
        assertEquals(MaintenanceStatusLevel.OVERDUE, status.status)
        assertEquals(-1000.0, status.remainingKm, 0.01)
    }

    @Test
    fun `maintenance ok calculation is correct`() {
        val now = System.currentTimeMillis()
        val item = MaintenanceItemEntity(
            vehicleId = 1,
            title = "Troca de Óleo",
            category = "Motor",
            intervalKm = 10000,
            intervalMonths = 6,
            lastServiceKm = 20000.0,
            lastServiceDateEpochMillis = now
        )

        // Vehicle has 25000 km -> 5000 km remaining -> OK
        val status = MaintenanceItemWithStatus.compute(item, 25000.0, now)
        assertEquals(MaintenanceStatusLevel.OK, status.status)
        assertEquals(5000.0, status.remainingKm, 0.01)
    }

    @Test
    fun `test export data json and clear data`() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = com.example.data.AppDatabase.getDatabase(context)
        val repo = com.example.data.FuelRepository(db.fuelDao())

        // Seed initial data
        repo.seedInitialDataIfEmpty()

        // Export JSON
        val json = repo.exportDataAsJson()
        val jsonObject = org.json.JSONObject(json)
        assertEquals("AutoFuel", jsonObject.getString("app"))
        org.junit.Assert.assertTrue(jsonObject.has("vehicles"))
        org.junit.Assert.assertTrue(jsonObject.has("fuelLogs"))
        org.junit.Assert.assertTrue(jsonObject.has("maintenanceItems"))

        // Test clear
        repo.clearAllData(createStarterVehicle = false)
        val emptyJson = repo.exportDataAsJson()
        val emptyObj = org.json.JSONObject(emptyJson)
        assertEquals(0, emptyObj.getJSONArray("vehicles").length())
        assertEquals(0, emptyObj.getJSONArray("fuelLogs").length())
    }

    @Test
    fun `test market car benchmark calculation`() {
        val dolphin = com.example.model.MarketCarBenchmark.PRESET_CARS.first { it.id == "dolphin_mini" }
        // 9.6 km/kWh, energy 0.90 R$/kWh -> ~0.09375 R$/km
        val costPerKm = dolphin.calculateCostPerKm(gasolinePrice = 5.89, electricityPriceKwh = 0.90)
        assertEquals(0.09375, costPerKm, 0.001)

        val corolla = com.example.model.MarketCarBenchmark.PRESET_CARS.first { it.id == "corolla_hybrid" }
        // 18.5 km/L, gasoline 5.89 -> ~0.31837 R$/km
        val corollaCost = corolla.calculateCostPerKm(gasolinePrice = 5.89, electricityPriceKwh = 0.90)
        assertEquals(5.89 / 18.5, corollaCost, 0.001)
    }
}
