package app.garminlike.nav.ui.hud.model

import app.garminlike.nav.core.model.GeoPoint
import app.garminlike.nav.core.model.Maneuver
import app.garminlike.nav.core.model.ManeuverType
import app.garminlike.nav.core.model.NavigationPhase
import app.garminlike.nav.core.model.SafetyAlert
import app.garminlike.nav.core.model.SafetyAlertKind
import app.garminlike.nav.core.model.VehicleType

/**
 * Pure Kotlin UI state for the mobile Garmin-like HUD.
 *
 * Decoupled from Compose runtime to enable thorough JVM unit testing and
 * standalone previews.
 *
 * Spec reference: docs/HUD_WIREFRAME.md Section 4.1.
 */
data class NavHudUiState(
    val vehicleType: VehicleType = VehicleType.CAR,
    val phase: NavigationPhase = NavigationPhase.IDLE,
    val currentSpeedKmh: Int = 0,
    val speedLimitKmh: Int? = null,
    val isOverspeed: Boolean = false,
    val nextManeuver: Maneuver? = null,
    val distanceToManeuverMeters: Double? = null,
    val laneGuidanceText: String? = null,
    val activeAlert: SafetyAlert? = null,
    val remainingDistanceKm: Double = 0.0,
    val remainingDurationMinutes: Long = 0,
    val etaFormatted: String = "--:--",
    val isOfflineMode: Boolean = false,
    val isGpsLost: Boolean = false,
    val isMuted: Boolean = false,
    val isDemoMode: Boolean = true,
    val demoInstructionText: String? = null,
    val demoSubInstructionText: String? = null
) {
    /** True if actively guiding along a validated route. */
    val isGuiding: Boolean get() = phase == NavigationPhase.GUIDING

    /** True if there is no active route, clearly indicating unrouted / idle state. */
    val isUnrouted: Boolean get() = phase == NavigationPhase.IDLE || nextManeuver == null

    companion object {
        /**
         * Default unrouted state for Stage 1.
         * Explicitly displays DEMO / NO ROUTE rather than fabricating real-time navigation telemetry.
         */
        fun demoInitial(vehicleType: VehicleType = VehicleType.CAR): NavHudUiState = NavHudUiState(
            vehicleType = vehicleType,
            phase = NavigationPhase.IDLE,
            currentSpeedKmh = 0,
            speedLimitKmh = null,
            isOverspeed = false,
            nextManeuver = null,
            distanceToManeuverMeters = null,
            laneGuidanceText = null,
            activeAlert = null,
            remainingDistanceKm = 0.0,
            remainingDurationMinutes = 0,
            etaFormatted = "--:--",
            isOfflineMode = false,
            isGpsLost = false,
            isMuted = false,
            isDemoMode = true,
            demoInstructionText = "未開始導航 (展示模式)",
            demoSubInstructionText = "Stage 1 尚未連接即時路徑與測速引擎"
        )

        /**
         * Sample simulation state for CAR navigation demonstration.
         * Corresponds to wireframe mockup in docs/HUD_WIREFRAME.md Section 3.1.
         */
        fun demoCarGuiding(): NavHudUiState = NavHudUiState(
            vehicleType = VehicleType.CAR,
            phase = NavigationPhase.GUIDING,
            currentSpeedKmh = 58,
            speedLimitKmh = 60,
            isOverspeed = false,
            nextManeuver = Maneuver(
                type = ManeuverType.KEEP_LEFT,
                at = GeoPoint(25.0189, 121.5721),
                instruction = "靠左行駛 進入 國道三號甲線 (台北聯絡道)",
                distanceFromStartMeters = 350.0
            ),
            distanceToManeuverMeters = 350.0,
            laneGuidanceText = "[ ^ ][ ^ ][ \\ ][ \\ ]",
            activeAlert = SafetyAlert(
                alertId = "ALERT-NPA-001",
                kind = SafetyAlertKind.SPEED_CAMERA,
                sourceId = "警政署 2026-09",
                sourcePublishedAtEpochMillis = 1756684800000L,
                expiresAtEpochMillis = null,
                cameraBearingDeg = 180.0,
                directionMatch = true,
                distanceAlongRouteMeters = 400.0,
                confidence = 1.0,
                message = "前方 400m 固定測速 60km/h",
                routeCorrelated = true,
                evidenceIds = listOf("TW-CAM-001")
            ),
            remainingDistanceKm = 4.8,
            remainingDurationMinutes = 8,
            etaFormatted = "17:45",
            isOfflineMode = false,
            isGpsLost = false,
            isMuted = false,
            isDemoMode = true,
            demoInstructionText = "靠左行駛 進入 國道三號甲線",
            demoSubInstructionText = "台北聯絡道往深坑"
        )

        /**
         * Sample simulation state for SCOOTER navigation demonstration.
         * Corresponds to wireframe mockup in docs/HUD_WIREFRAME.md Section 3.2.
         */
        fun demoScooterGuiding(): NavHudUiState = NavHudUiState(
            vehicleType = VehicleType.SCOOTER,
            phase = NavigationPhase.GUIDING,
            currentSpeedKmh = 46,
            speedLimitKmh = 50,
            isOverspeed = false,
            nextManeuver = Maneuver(
                type = ManeuverType.TURN_LEFT,
                at = GeoPoint(25.0601, 121.4988),
                instruction = "兩段式左轉 (依標誌待轉)",
                distanceFromStartMeters = 150.0
            ),
            distanceToManeuverMeters = 150.0,
            laneGuidanceText = "機車兩段待轉區",
            activeAlert = SafetyAlert(
                alertId = "ALERT-SEC-002",
                kind = SafetyAlertKind.SECTION_SPEED,
                sourceId = "新北市警局 2026-08",
                sourcePublishedAtEpochMillis = 1754006400000L,
                expiresAtEpochMillis = null,
                cameraBearingDeg = 90.0,
                directionMatch = true,
                distanceAlongRouteMeters = 250.0,
                confidence = 1.0,
                message = "區間測速起點 | 速限 50 km/h (全天候執法)",
                routeCorrelated = true,
                evidenceIds = listOf("TW-SEC-002")
            ),
            remainingDistanceKm = 2.3,
            remainingDurationMinutes = 7,
            etaFormatted = "17:12",
            isOfflineMode = false,
            isGpsLost = false,
            isMuted = false,
            isDemoMode = true,
            demoInstructionText = "兩段式左轉 (依標誌待轉)",
            demoSubInstructionText = "至 重新路一段 / 機車專用道引道"
        )

        /**
         * Sample simulation state for HEAVY_MOTORCYCLE navigation demonstration.
         * Highlights expressways legality and higher cruise speeds.
         */
        fun demoHeavyMotorcycleGuiding(): NavHudUiState = NavHudUiState(
            vehicleType = VehicleType.HEAVY_MOTORCYCLE,
            phase = NavigationPhase.GUIDING,
            currentSpeedKmh = 72,
            speedLimitKmh = 80,
            isOverspeed = false,
            nextManeuver = Maneuver(
                type = ManeuverType.KEEP_RIGHT,
                at = GeoPoint(25.0412, 121.4655),
                instruction = "靠右行駛 往 台64線快速公路 匝道",
                distanceFromStartMeters = 1200.0
            ),
            distanceToManeuverMeters = 1200.0,
            laneGuidanceText = "大型重型機車可通行主線",
            activeAlert = SafetyAlert(
                alertId = "ALERT-TRAF-003",
                kind = SafetyAlertKind.TRAFFIC_ENFORCEMENT,
                sourceId = "公路局快速公路通報",
                sourcePublishedAtEpochMillis = 1751328000000L,
                expiresAtEpochMillis = null,
                cameraBearingDeg = 270.0,
                directionMatch = true,
                distanceAlongRouteMeters = 800.0,
                confidence = 1.0,
                message = "前方 800m 快速公路科技執法",
                routeCorrelated = true,
                evidenceIds = listOf("TW-SEG-07")
            ),
            remainingDistanceKm = 12.5,
            remainingDurationMinutes = 14,
            etaFormatted = "18:05",
            isOfflineMode = false,
            isGpsLost = false,
            isMuted = false,
            isDemoMode = true,
            demoInstructionText = "靠右行駛 往 台64線",
            demoSubInstructionText = "快速公路主線速限 80 km/h"
        )
    }
}
