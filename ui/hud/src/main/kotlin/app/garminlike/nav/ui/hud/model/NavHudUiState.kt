package app.garminlike.nav.ui.hud.model

import app.garminlike.nav.core.model.GeoPoint
import app.garminlike.nav.core.model.Maneuver
import app.garminlike.nav.core.model.ManeuverType
import app.garminlike.nav.core.model.NavigationPhase
import app.garminlike.nav.core.model.SafetyAlert
import app.garminlike.nav.core.model.SafetyAlertKind
import app.garminlike.nav.core.model.VehicleType

/**
 * Explicit GPS status representation for the HUD.
 * Prevents falsely assuming a valid GPS fix when running without active location feeds.
 *
 * Spec reference: Review H1 requirement.
 */
enum class GpsStatus {
    /** Location provider uninitialized, detached, or permissions not yet evaluated. */
    UNKNOWN,
    /** Actively waiting for satellite fix or coarse fallback fix. */
    SEARCHING,
    /** Valid GPS fix obtained with acceptable horizontal accuracy. */
    VALID,
    /** GPS signal lost or provider disabled during active navigation. */
    LOST
}

/**
 * Identifies which demo fixture is currently selected in the HUD demo bar.
 * Spec reference: Review H4 requirement.
 */
enum class DemoModeSelection {
    UNROUTED,
    CAR,
    SCOOTER,
    HEAVY_MOTORCYCLE
}

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
    val gpsStatus: GpsStatus = GpsStatus.UNKNOWN,
    val isMuted: Boolean = false,
    val isDemoMode: Boolean = true,
    val demoInstructionText: String? = null,
    val demoSubInstructionText: String? = null
) {
    /** True if actively guiding along a validated route. */
    val isGuiding: Boolean get() = phase == NavigationPhase.GUIDING

    /** True if there is no active route, clearly indicating unrouted / idle state. */
    val isUnrouted: Boolean get() = phase == NavigationPhase.IDLE || nextManeuver == null

    /** Computes which demo button should be visually highlighted. */
    val demoSelection: DemoModeSelection get() = when {
        isUnrouted -> DemoModeSelection.UNROUTED
        vehicleType == VehicleType.CAR -> DemoModeSelection.CAR
        vehicleType == VehicleType.SCOOTER -> DemoModeSelection.SCOOTER
        vehicleType == VehicleType.HEAVY_MOTORCYCLE -> DemoModeSelection.HEAVY_MOTORCYCLE
        else -> DemoModeSelection.UNROUTED
    }

    companion object {
        /**
         * Special sourceId prefix designating simulated test fixtures.
         * Spec reference: Review H2 requirement.
         */
        const val DEMO_SOURCE_PREFIX = "DEMO_MOCK_"

        /**
         * Default unrouted state for Stage 1.
         * Explicitly displays DEMO / NO ROUTE rather than fabricating real-time navigation telemetry.
         * GPS status defaults to UNKNOWN (not VALID) to prevent false claims of real GPS fixes.
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
            gpsStatus = GpsStatus.UNKNOWN,
            isMuted = false,
            isDemoMode = true,
            demoInstructionText = "未開始導航 (展示模式)",
            demoSubInstructionText = "Stage 1 尚未連接即時路徑與測速引擎"
        )

        /**
         * Sample simulation state for CAR navigation demonstration.
         * Uses clearly designated MOCK alert metadata to prevent confusing QA simulation with official bulletins.
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
                alertId = "DEMO-ALERT-001",
                kind = SafetyAlertKind.SPEED_CAMERA,
                sourceId = "${DEMO_SOURCE_PREFIX}TAIPEI_TEST",
                sourcePublishedAtEpochMillis = null,
                expiresAtEpochMillis = null,
                cameraBearingDeg = 180.0,
                directionMatch = true,
                distanceAlongRouteMeters = 400.0,
                confidence = 0.0, // Mock fixture: no production confidence guarantee
                message = "[展示模擬] 前方 400m 固定測速 60km/h",
                routeCorrelated = false,
                evidenceIds = emptyList()
            ),
            remainingDistanceKm = 4.8,
            remainingDurationMinutes = 8,
            etaFormatted = "17:45",
            isOfflineMode = false,
            gpsStatus = GpsStatus.VALID, // Simulated fix for guiding preview
            isMuted = false,
            isDemoMode = true,
            demoInstructionText = "靠左行駛 進入 國道三號甲線",
            demoSubInstructionText = "台北聯絡道往深坑"
        )

        /**
         * Sample simulation state for SCOOTER navigation demonstration.
         * Emphasizes two-stage left turn guidance with clearly flagged mock alerts.
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
                alertId = "DEMO-ALERT-002",
                kind = SafetyAlertKind.SECTION_SPEED,
                sourceId = "${DEMO_SOURCE_PREFIX}NTPC_TEST",
                sourcePublishedAtEpochMillis = null,
                expiresAtEpochMillis = null,
                cameraBearingDeg = 90.0,
                directionMatch = true,
                distanceAlongRouteMeters = 250.0,
                confidence = 0.0,
                message = "[展示模擬] 區間測速起點 | 速限 50 km/h",
                routeCorrelated = false,
                evidenceIds = emptyList()
            ),
            remainingDistanceKm = 2.3,
            remainingDurationMinutes = 7,
            etaFormatted = "17:12",
            isOfflineMode = false,
            gpsStatus = GpsStatus.VALID,
            isMuted = false,
            isDemoMode = true,
            demoInstructionText = "兩段式左轉 (依標誌待轉)",
            demoSubInstructionText = "至 重新路一段 / 機車專用道引道"
        )

        /**
         * Sample simulation state for HEAVY_MOTORCYCLE navigation demonstration.
         * Highlights expressways cruise speed with mock safety alert data.
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
                alertId = "DEMO-ALERT-003",
                kind = SafetyAlertKind.TRAFFIC_ENFORCEMENT,
                sourceId = "${DEMO_SOURCE_PREFIX}HIGHWAY_TEST",
                sourcePublishedAtEpochMillis = null,
                expiresAtEpochMillis = null,
                cameraBearingDeg = 270.0,
                directionMatch = true,
                distanceAlongRouteMeters = 800.0,
                confidence = 0.0,
                message = "[展示模擬] 前方 800m 快速公路執法測試",
                routeCorrelated = false,
                evidenceIds = emptyList()
            ),
            remainingDistanceKm = 12.5,
            remainingDurationMinutes = 14,
            etaFormatted = "18:05",
            isOfflineMode = false,
            gpsStatus = GpsStatus.VALID,
            isMuted = false,
            isDemoMode = true,
            demoInstructionText = "靠右行駛 往 台64線",
            demoSubInstructionText = "快速公路主線速限 80 km/h"
        )
    }
}
