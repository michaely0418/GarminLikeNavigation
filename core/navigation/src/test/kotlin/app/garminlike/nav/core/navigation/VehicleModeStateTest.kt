package app.garminlike.nav.core.navigation

import app.garminlike.nav.core.model.VehicleType
import org.junit.Assert.assertEquals
import org.junit.Test

class VehicleModeStateTest {
    @Test fun defaultsToCar() {
        assertEquals(VehicleType.CAR, VehicleModeState().mode.value)
    }

    @Test fun canSwitchBetweenAllThreeVehicleProfiles() {
        val state = VehicleModeState()
        state.select(VehicleType.SCOOTER)
        assertEquals(VehicleType.SCOOTER, state.mode.value)
        state.select(VehicleType.HEAVY_MOTORCYCLE)
        assertEquals(VehicleType.HEAVY_MOTORCYCLE, state.mode.value)
        state.select(VehicleType.CAR)
        assertEquals(VehicleType.CAR, state.mode.value)
    }
}
