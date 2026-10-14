package app.garminlike.nav.ui.hud

import app.garminlike.nav.core.model.NavigationPhase
import app.garminlike.nav.core.model.VehicleType
import app.garminlike.nav.ui.hud.model.NavHudUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavHudUiStateTest {

    @Test
    fun `demoInitial defaults to clear DEMO and NO ROUTE unrouted state`() {
        val state = NavHudUiState.demoInitial(VehicleType.CAR)

        assertEquals(VehicleType.CAR, state.vehicleType)
        assertEquals(NavigationPhase.IDLE, state.phase)
        assertTrue(state.isUnrouted)
        assertFalse(state.isGuiding)
        assertTrue(state.isDemoMode)

        // Verifies no fabricated telemetry in idle/unrouted state
        assertEquals(0, state.currentSpeedKmh)
        assertNull(state.speedLimitKmh)
        assertFalse(state.isOverspeed)
        assertNull(state.nextManeuver)
        assertNull(state.distanceToManeuverMeters)
        assertNull(state.activeAlert)
        assertEquals("--:--", state.etaFormatted)
        assertEquals(0.0, state.remainingDistanceKm, 0.001)

        assertEquals("未開始導航 (展示模式)", state.demoInstructionText)
        assertNotNull(state.demoSubInstructionText)
    }

    @Test
    fun `demoCarGuiding conforms to Car HUD wireframe specs`() {
        val state = NavHudUiState.demoCarGuiding()

        assertEquals(VehicleType.CAR, state.vehicleType)
        assertEquals(NavigationPhase.GUIDING, state.phase)
        assertTrue(state.isGuiding)
        assertFalse(state.isUnrouted)

        assertEquals(58, state.currentSpeedKmh)
        assertEquals(60, state.speedLimitKmh)
        assertFalse(state.isOverspeed)

        assertNotNull(state.nextManeuver)
        assertEquals(350.0, state.distanceToManeuverMeters!!, 0.001)
        assertNotNull(state.laneGuidanceText)
        assertNotNull(state.activeAlert)
        assertEquals("17:45", state.etaFormatted)
        assertEquals(4.8, state.remainingDistanceKm, 0.001)
        assertEquals(8, state.remainingDurationMinutes)
    }

    @Test
    fun `demoScooterGuiding conforms to Scooter HUD wireframe specs`() {
        val state = NavHudUiState.demoScooterGuiding()

        assertEquals(VehicleType.SCOOTER, state.vehicleType)
        assertEquals(NavigationPhase.GUIDING, state.phase)
        assertTrue(state.isGuiding)

        assertEquals(46, state.currentSpeedKmh)
        assertEquals(50, state.speedLimitKmh)
        assertFalse(state.isOverspeed)

        assertNotNull(state.nextManeuver)
        assertEquals(150.0, state.distanceToManeuverMeters!!, 0.001)
        assertTrue(state.nextManeuver!!.instruction.contains("兩段式左轉"))
        assertEquals("17:12", state.etaFormatted)
        assertEquals(2.3, state.remainingDistanceKm, 0.001)
    }

    @Test
    fun `demoHeavyMotorcycleGuiding conforms to Heavy Motorcycle HUD wireframe specs`() {
        val state = NavHudUiState.demoHeavyMotorcycleGuiding()

        assertEquals(VehicleType.HEAVY_MOTORCYCLE, state.vehicleType)
        assertEquals(NavigationPhase.GUIDING, state.phase)
        assertTrue(state.isGuiding)

        assertEquals(72, state.currentSpeedKmh)
        assertEquals(80, state.speedLimitKmh)
        assertFalse(state.isOverspeed)

        assertNotNull(state.nextManeuver)
        assertEquals(1200.0, state.distanceToManeuverMeters!!, 0.001)
        assertTrue(state.nextManeuver!!.instruction.contains("台64線"))
        assertEquals("18:05", state.etaFormatted)
        assertEquals(12.5, state.remainingDistanceKm, 0.001)
    }
}
