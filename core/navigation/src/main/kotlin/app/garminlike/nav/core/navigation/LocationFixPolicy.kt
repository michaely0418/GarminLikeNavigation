package app.garminlike.nav.core.navigation

/**
 * Android-independent metadata for selecting a displayed foreground location fix.
 * Monotonic time is preferred when it is available from both Android providers.
 */
data class LocationFixQuality(
    val fixTimeMillis: Long,
    val elapsedRealtimeNanos: Long,
    val accuracyMeters: Float?
)

object LocationFixPolicy {
    private const val RECENT_FIX_WINDOW_MS = 15_000L
    private const val ACCURACY_HYSTERESIS_METERS = 15f

    /**
     * Return false for an out-of-order fix or a substantially less accurate fix
     * while the previous displayed fix is recent.
     *
     * Older fixes are compared using elapsedRealtimeNanos (monotonic clock),
     * falling back to fixTimeMillis only if monotonic timestamps are absent.
     */
    fun shouldAccept(
        previous: LocationFixQuality?,
        incoming: LocationFixQuality,
        nowMillis: Long
    ): Boolean {
        if (previous == null) return true
        val previousAge = nowMillis - previous.fixTimeMillis
        if (previousAge !in 0L..RECENT_FIX_WINDOW_MS) return true

        val older = if (previous.elapsedRealtimeNanos > 0L && incoming.elapsedRealtimeNanos > 0L) {
            incoming.elapsedRealtimeNanos < previous.elapsedRealtimeNanos
        } else {
            incoming.fixTimeMillis < previous.fixTimeMillis
        }
        if (older) return false

        val priorAccuracy = previous.accuracyMeters
        val currentAccuracy = incoming.accuracyMeters
        return priorAccuracy == null || currentAccuracy == null ||
            priorAccuracy + ACCURACY_HYSTERESIS_METERS >= currentAccuracy
    }
}
