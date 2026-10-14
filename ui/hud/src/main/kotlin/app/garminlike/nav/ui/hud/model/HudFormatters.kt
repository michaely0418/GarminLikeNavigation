package app.garminlike.nav.ui.hud.model

import app.garminlike.nav.core.model.VehicleType
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Pure Kotlin formatting utilities for HUD telemetry and visual readouts.
 * Independently testable on the JVM.
 */
object HudFormatters {

    /**
     * Converts speed in meters per second to kilometers per hour.
     * Clamps negative or non-finite numbers to 0.
     */
    fun formatSpeed(speedMps: Double?): Int {
        if (speedMps == null || !speedMps.isFinite() || speedMps < 0.0) return 0
        return (speedMps * 3.6).roundToInt().coerceAtLeast(0)
    }

    /**
     * Formats distance into glanceable human-readable units.
     * - Under 1000 meters: rounded to nearest 10 meters (e.g. "350 m").
     * - 1000 meters or more: formatted in kilometers with 1 decimal place (e.g. "4.8 km").
     * - Null or invalid: "--".
     */
    fun formatDistance(meters: Double?): String {
        if (meters == null || !meters.isFinite() || meters < 0.0) return "--"
        return if (meters < 1000.0) {
            val rounded = ((meters / 10.0).roundToInt() * 10).coerceAtLeast(0)
            "$rounded m"
        } else {
            val km = meters / 1000.0
            String.format(Locale.US, "%.1f km", km)
        }
    }

    /**
     * Formats duration in minutes into localized traditional Chinese time display.
     * Examples: "8 分鐘", "1 小時 15 分".
     */
    fun formatDuration(minutes: Long): String {
        if (minutes <= 0) return "--"
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        return if (hours > 0) {
            if (remainingMinutes > 0) {
                "${hours} 小時 ${remainingMinutes} 分"
            } else {
                "${hours} 小時"
            }
        } else {
            "${minutes} 分鐘"
        }
    }

    /**
     * Returns the localized vehicle name.
     */
    fun formatVehicleName(type: VehicleType): String = when (type) {
        VehicleType.CAR -> "小客車"
        VehicleType.SCOOTER -> "普通機車"
        VehicleType.HEAVY_MOTORCYCLE -> "大型重機"
    }

    /**
     * Returns the bracketed vehicle badge with emoji for top HUD status bar.
     */
    fun formatVehicleTag(type: VehicleType): String = when (type) {
        VehicleType.CAR -> "[🚗 汽車]"
        VehicleType.SCOOTER -> "[🛵 機車]"
        VehicleType.HEAVY_MOTORCYCLE -> "[🏍️ 重機]"
    }

    /**
     * Evaluates whether the current vehicle speed exceeds the posted speed limit.
     */
    fun isOverspeed(currentSpeedKmh: Int, limitKmh: Int?): Boolean {
        if (limitKmh == null || limitKmh <= 0) return false
        return currentSpeedKmh > limitKmh
    }

    /**
     * Localized status label for GPS tracking state with explicit demo mode awareness.
     * Prevents false claims of "GPS: 良好" during simulation and unverified states.
     *
     * Spec reference: Review R2-H1 requirement.
     */
    fun formatHudGpsLabel(status: GpsStatus, isDemoMode: Boolean): String = when {
        isDemoMode && status == GpsStatus.VALID -> "GPS: 模擬"
        isDemoMode && status == GpsStatus.UNKNOWN -> "GPS: 未連接 (展示)"
        isDemoMode && status == GpsStatus.SEARCHING -> "GPS: 搜尋中 (展示)"
        isDemoMode && status == GpsStatus.LOST -> "GPS: 訊號中斷 (展示)"
        status == GpsStatus.VALID -> "GPS: 良好"
        status == GpsStatus.SEARCHING -> "GPS: 搜尋中..."
        status == GpsStatus.LOST -> "GPS: 訊號中斷"
        else -> "GPS: 未連接"
    }

    /**
     * Localized status label for production GPS tracking state.
     */
    fun formatGpsStatusLabel(status: GpsStatus): String = formatHudGpsLabel(status, isDemoMode = false)
}
