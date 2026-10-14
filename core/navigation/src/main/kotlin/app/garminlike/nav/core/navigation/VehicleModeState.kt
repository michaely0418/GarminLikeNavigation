package app.garminlike.nav.core.navigation

import app.garminlike.nav.core.model.VehicleType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Stage 1 vehicle choice is UI state only. Legal road access is NOT yet
 * implemented and vehicle selection must not be presented as active routing.
 */
class VehicleModeState(initial: VehicleType = VehicleType.CAR) {
    private val mutableMode = MutableStateFlow(initial)
    val mode: StateFlow<VehicleType> = mutableMode

    fun select(vehicleType: VehicleType) {
        mutableMode.value = vehicleType
    }
}
