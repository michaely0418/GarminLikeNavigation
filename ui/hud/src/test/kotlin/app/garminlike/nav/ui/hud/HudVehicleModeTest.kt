package app.garminlike.nav.ui.hud

import app.garminlike.nav.core.model.VehicleType
import app.garminlike.nav.ui.hud.model.HudFormatters
import app.garminlike.nav.ui.hud.model.NavHudUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class HudVehicleModeTest {

    @Test
    fun `supports all three core vehicle types exhaustively`() {
        val types = VehicleType.entries

        assertEquals(3, types.size)
        assertEquals(listOf(VehicleType.CAR, VehicleType.SCOOTER, VehicleType.HEAVY_MOTORCYCLE), types)

        types.forEach { type ->
            val label = HudFormatters.formatVehicleName(type)
            val badge = HudFormatters.formatVehicleTag(type)
            val initialState = NavHudUiState.demoInitial(type)

            assertNotEquals("", label)
            assertNotEquals("", badge)
            assertEquals(type, initialState.vehicleType)
        }
    }

    @Test
    fun `switching vehicle modes updates state correctly`() {
        val carState = NavHudUiState.demoInitial(VehicleType.CAR)
        val scooterState = carState.copy(vehicleType = VehicleType.SCOOTER)
        val heavyState = scooterState.copy(vehicleType = VehicleType.HEAVY_MOTORCYCLE)

        assertEquals(VehicleType.CAR, carState.vehicleType)
        assertEquals(VehicleType.SCOOTER, scooterState.vehicleType)
        assertEquals(VehicleType.HEAVY_MOTORCYCLE, heavyState.vehicleType)

        assertEquals("[🚗 汽車]", HudFormatters.formatVehicleTag(carState.vehicleType))
        assertEquals("[🛵 機車]", HudFormatters.formatVehicleTag(scooterState.vehicleType))
        assertEquals("[🏍️ 重機]", HudFormatters.formatVehicleTag(heavyState.vehicleType))
    }
}
