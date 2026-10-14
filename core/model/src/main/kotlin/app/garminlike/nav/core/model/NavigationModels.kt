package app.garminlike.nav.core.model

enum class NavigationPhase { IDLE, ROUTE_READY, GUIDING, PAUSED, COMPLETED, ERROR }

data class NavState(
    val phase: NavigationPhase = NavigationPhase.IDLE,
    val activeRoute: NavRoute? = null,
    val lastLocation: LocationSample? = null,
    val distanceRemainingMeters: Double? = null,
    val etaEpochMillis: Long? = null,
    val offRoute: Boolean = false,
    val offlineRouteContinuation: Boolean = false,
    val errorMessage: String? = null
)

data class NavigationVoicePrompt(val message: String, val priority: Int = 0)
