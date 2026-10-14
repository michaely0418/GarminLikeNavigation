package app.garminlike.nav.ui.hud

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.garminlike.nav.core.model.VehicleType
import app.garminlike.nav.ui.hud.components.DemoNoRouteBanner
import app.garminlike.nav.ui.hud.components.ManeuverGuidanceHeader
import app.garminlike.nav.ui.hud.components.PortraitActionBar
import app.garminlike.nav.ui.hud.components.PortraitTripStatsCard
import app.garminlike.nav.ui.hud.components.SafetyAlertBanner
import app.garminlike.nav.ui.hud.components.SpeedometerTelemetryCard
import app.garminlike.nav.ui.hud.components.TripSummaryBar
import app.garminlike.nav.ui.hud.model.GpsStatus
import app.garminlike.nav.ui.hud.model.HudFormatters
import app.garminlike.nav.ui.hud.model.NavHudUiState
import app.garminlike.nav.ui.hud.theme.GarminHudTheme
import app.garminlike.nav.ui.hud.theme.HudColors

/**
 * Mobile Garmin-like Head-Up Display (HUD) Screen.
 *
 * Supports adaptive Portrait and Landscape orientations, three Taiwan vehicle types
 * (Car, Scooter, Heavy Motorcycle), and prominent DEMO / NO ROUTE state handling.
 *
 * Architectural Boundary:
 * - Phone Surface ONLY (Jetpack Compose).
 * - Decoupled from MapLibre (`ui/map`) and Android Auto (`car:app`).
 *
 * Spec reference: docs/HUD_WIREFRAME.md.
 */
@Composable
fun HudScreen(
    state: NavHudUiState,
    modifier: Modifier = Modifier,
    onVehicleTypeChange: (VehicleType) -> Unit = {},
    onToggleMute: () -> Unit = {},
    onResetClick: () -> Unit = {},
    onSelectDemoState: (NavHudUiState) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    GarminHudTheme {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = HudColors.BackgroundBlack
        ) {
            if (isLandscape) {
                LandscapeHudLayout(
                    state = state,
                    onVehicleTypeChange = onVehicleTypeChange,
                    onToggleMute = onToggleMute,
                    onResetClick = onResetClick,
                    onSelectDemoState = onSelectDemoState
                )
            } else {
                PortraitHudLayout(
                    state = state,
                    onVehicleTypeChange = onVehicleTypeChange,
                    onToggleMute = onToggleMute,
                    onResetClick = onResetClick,
                    onSelectDemoState = onSelectDemoState
                )
            }
        }
    }
}

/**
 * Landscape HUD Layout (Car Windshield / Dashboard Mount).
 * Spec reference: docs/HUD_WIREFRAME.md Section 3.1.
 */
@Composable
fun LandscapeHudLayout(
    state: NavHudUiState,
    onVehicleTypeChange: (VehicleType) -> Unit,
    onToggleMute: () -> Unit,
    onResetClick: () -> Unit,
    onSelectDemoState: (NavHudUiState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Top Status Bar
        TopStatusBar(state = state)

        Spacer(modifier = Modifier.height(6.dp))

        // Center 3-column area: Maneuver (~32%), Map Viewport (~48%), Speed/Alerts (~20%)
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Left Column (~32%): Maneuver Card & Demo Control
            Column(
                modifier = Modifier
                    .weight(0.32f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ManeuverGuidanceHeader(
                    maneuver = state.nextManeuver,
                    distanceToManeuverMeters = state.distanceToManeuverMeters,
                    laneGuidanceText = state.laneGuidanceText,
                    demoInstructionText = state.demoInstructionText,
                    demoSubInstructionText = state.demoSubInstructionText
                )

                if (state.isDemoMode) {
                    DemoNoRouteBanner(
                        currentSelection = state.demoSelection,
                        onSelectUnroutedDemo = { onSelectDemoState(NavHudUiState.demoInitial(state.vehicleType)) },
                        onSelectCarDemo = { onSelectDemoState(NavHudUiState.demoCarGuiding()) },
                        onSelectScooterDemo = { onSelectDemoState(NavHudUiState.demoScooterGuiding()) },
                        onSelectHeavyMotorcycleDemo = { onSelectDemoState(NavHudUiState.demoHeavyMotorcycleGuiding()) }
                    )
                }
            }

            // Center Column (~48%): Map Viewport Placeholder (Stage 1 Detached Boundary)
            Box(
                modifier = Modifier
                    .weight(0.48f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(HudColors.SurfaceCard)
                    .border(1.dp, HudColors.CardBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                MapViewportPlaceholder(isLandscape = true)
            }

            // Right Column (~20%): Speed Telemetry & Safety Alerts
            Column(
                modifier = Modifier
                    .weight(0.20f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                SpeedometerTelemetryCard(
                    currentSpeedKmh = state.currentSpeedKmh,
                    speedLimitKmh = state.speedLimitKmh,
                    isOverspeed = state.isOverspeed,
                    modifier = Modifier.fillMaxWidth()
                )

                if (state.activeAlert != null) {
                    SafetyAlertBanner(
                        alert = state.activeAlert,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bottom Trip Summary Bar
        TripSummaryBar(
            remainingDistanceKm = state.remainingDistanceKm,
            etaFormatted = state.etaFormatted,
            remainingDurationMinutes = state.remainingDurationMinutes,
            vehicleType = state.vehicleType,
            isOfflineMode = state.isOfflineMode,
            isMuted = state.isMuted,
            onToggleMute = onToggleMute,
            onCycleVehicleType = { onVehicleTypeChange(cycleVehicleType(state.vehicleType)) },
            onResetClick = onResetClick
        )
    }
}

/**
 * Portrait HUD Layout (Motorcycle Handlebar / Phone Car Mount).
 * Spec reference: docs/HUD_WIREFRAME.md Section 3.2.
 *
 * Redesigned for H3: Eliminates horizontal squeezing on 320dp–390dp screens
 * by cleanly separating Speedometer and Trip Stats into a 2-column telemetry row,
 * supported by a dedicated full-width action bar below.
 */
@Composable
fun PortraitHudLayout(
    state: NavHudUiState,
    onVehicleTypeChange: (VehicleType) -> Unit,
    onToggleMute: () -> Unit,
    onResetClick: () -> Unit,
    onSelectDemoState: (NavHudUiState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Top Status Bar
        TopStatusBar(state = state)

        Spacer(modifier = Modifier.height(6.dp))

        // Maneuver Guidance Card
        ManeuverGuidanceHeader(
            maneuver = state.nextManeuver,
            distanceToManeuverMeters = state.distanceToManeuverMeters,
            laneGuidanceText = state.laneGuidanceText,
            demoInstructionText = state.demoInstructionText,
            demoSubInstructionText = state.demoSubInstructionText
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Safety Alert Toast or Demo Banner
        if (state.activeAlert != null) {
            SafetyAlertBanner(
                alert = state.activeAlert,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        if (state.isDemoMode) {
            DemoNoRouteBanner(
                currentSelection = state.demoSelection,
                onSelectUnroutedDemo = { onSelectDemoState(NavHudUiState.demoInitial(state.vehicleType)) },
                onSelectCarDemo = { onSelectDemoState(NavHudUiState.demoCarGuiding()) },
                onSelectScooterDemo = { onSelectDemoState(NavHudUiState.demoScooterGuiding()) },
                onSelectHeavyMotorcycleDemo = { onSelectDemoState(NavHudUiState.demoHeavyMotorcycleGuiding()) }
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Center Map Viewport Placeholder
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(HudColors.SurfaceCard)
                .border(1.dp, HudColors.CardBorder, RoundedCornerShape(12.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            MapViewportPlaceholder(isLandscape = false)
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bottom Telemetry Row: Speedometer (left) + Trip Stats (right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpeedometerTelemetryCard(
                currentSpeedKmh = state.currentSpeedKmh,
                speedLimitKmh = state.speedLimitKmh,
                isOverspeed = state.isOverspeed,
                isCompact = true
            )

            PortraitTripStatsCard(
                remainingDistanceKm = state.remainingDistanceKm,
                etaFormatted = state.etaFormatted,
                remainingDurationMinutes = state.remainingDurationMinutes,
                isOfflineMode = state.isOfflineMode,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bottom Action Bar: Vehicle mode button + Mute + Recenter
        PortraitActionBar(
            vehicleType = state.vehicleType,
            isMuted = state.isMuted,
            onToggleMute = onToggleMute,
            onCycleVehicleType = { onVehicleTypeChange(cycleVehicleType(state.vehicleType)) },
            onResetClick = onResetClick
        )
    }
}

/**
 * Top Status Bar displaying vehicle mode, GPS status, and offline indicator.
 * Spec reference: Review H1 requirement.
 */
@Composable
private fun TopStatusBar(state: NavHudUiState) {
    val vehicleBadge = HudFormatters.formatVehicleTag(state.vehicleType)
    val gpsStatusLabel = HudFormatters.formatHudGpsLabel(state.gpsStatus, state.isDemoMode)
    val gpsColor = when {
        state.isDemoMode && state.gpsStatus == GpsStatus.VALID -> HudColors.AzureBlue
        state.isDemoMode -> HudColors.TextMuted
        state.gpsStatus == GpsStatus.VALID -> HudColors.EmeraldGreen
        state.gpsStatus == GpsStatus.SEARCHING -> HudColors.VividAmber
        state.gpsStatus == GpsStatus.LOST -> HudColors.CrimsonRed
        else -> HudColors.TextMuted
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = vehicleBadge,
                color = HudColors.PureWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = gpsStatusLabel,
                color = gpsColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (state.isOfflineMode) {
                Text(
                    text = "⚡ 離線接續",
                    color = HudColors.VividAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "手機 HUD 原型",
                color = HudColors.TextMuted,
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Map Viewport Placeholder indicating architectural detachment in Stage 1.
 * Prevents tight coupling with MapLibre or Android Auto templates.
 */
@Composable
private fun MapViewportPlaceholder(isLandscape: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🗺️",
            fontSize = if (isLandscape) 32.sp else 36.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "[ MAP VIEWPORT PLACEHOLDER ]",
            color = HudColors.TextMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Stage 1: 獨立 Compose HUD 介面邊界\n地圖視圖由 Stage 1 MapLibre 模組提供",
            color = HudColors.TextDisabled,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 15.sp
        )
    }
}

private fun cycleVehicleType(current: VehicleType): VehicleType = when (current) {
    VehicleType.CAR -> VehicleType.SCOOTER
    VehicleType.SCOOTER -> VehicleType.HEAVY_MOTORCYCLE
    VehicleType.HEAVY_MOTORCYCLE -> VehicleType.CAR
}

/**
 * Interactive preview wrapper supporting real-time demo switching.
 */
@Composable
fun InteractiveHudDemo(initialState: NavHudUiState = NavHudUiState.demoInitial()) {
    var state by remember { mutableStateOf(initialState) }

    HudScreen(
        state = state,
        onVehicleTypeChange = { newType ->
            state = state.copy(vehicleType = newType)
        },
        onToggleMute = {
            state = state.copy(isMuted = !state.isMuted)
        },
        onResetClick = {
            state = NavHudUiState.demoInitial(state.vehicleType)
        },
        onSelectDemoState = { newState ->
            state = newState
        }
    )
}

// ==========================================
// Previews for 3 Vehicle Modes and Layouts
// ==========================================

@Preview(name = "Landscape Car Guiding", widthDp = 840, heightDp = 390, showBackground = true)
@Composable
fun HudLandscapeCarPreview() {
    GarminHudTheme {
        HudScreen(state = NavHudUiState.demoCarGuiding())
    }
}

@Preview(name = "Landscape Scooter Guiding", widthDp = 840, heightDp = 390, showBackground = true)
@Composable
fun HudLandscapeScooterPreview() {
    GarminHudTheme {
        HudScreen(state = NavHudUiState.demoScooterGuiding())
    }
}

@Preview(name = "Landscape Heavy Motorcycle Guiding", widthDp = 840, heightDp = 390, showBackground = true)
@Composable
fun HudLandscapeHeavyMotorcyclePreview() {
    GarminHudTheme {
        HudScreen(state = NavHudUiState.demoHeavyMotorcycleGuiding())
    }
}

@Preview(name = "Portrait Car Guiding (390x840)", widthDp = 390, heightDp = 840, showBackground = true)
@Composable
fun HudPortraitCarPreview() {
    GarminHudTheme {
        HudScreen(state = NavHudUiState.demoCarGuiding())
    }
}

@Preview(name = "Portrait Scooter Guiding (390x840)", widthDp = 390, heightDp = 840, showBackground = true)
@Composable
fun HudPortraitScooterPreview() {
    GarminHudTheme {
        HudScreen(state = NavHudUiState.demoScooterGuiding())
    }
}

@Preview(name = "Portrait Heavy Motorcycle Guiding (390x840)", widthDp = 390, heightDp = 840, showBackground = true)
@Composable
fun HudPortraitHeavyMotorcyclePreview() {
    GarminHudTheme {
        HudScreen(state = NavHudUiState.demoHeavyMotorcycleGuiding())
    }
}

@Preview(name = "Unrouted Demo State (390x840)", widthDp = 390, heightDp = 840, showBackground = true)
@Composable
fun HudNoRoutePreview() {
    GarminHudTheme {
        HudScreen(state = NavHudUiState.demoInitial(VehicleType.CAR))
    }
}

@Preview(name = "Narrow 320dp Portrait Test", widthDp = 320, heightDp = 640, showBackground = true)
@Composable
fun HudPortraitNarrow320Preview() {
    GarminHudTheme {
        HudScreen(state = NavHudUiState.demoScooterGuiding())
    }
}

@Preview(name = "Large Font 1.3x Portrait Test", widthDp = 390, heightDp = 840, fontScale = 1.3f, showBackground = true)
@Composable
fun HudPortraitLargeFontPreview() {
    GarminHudTheme {
        HudScreen(state = NavHudUiState.demoCarGuiding())
    }
}
