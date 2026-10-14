# Stage 1 — Mobile Compose HUD Prototype (`:ui:hud`)

**Document Version:** 1.0.0  
**Effective Date:** 2026-10-14  
**Primary Owner:** Gemini (Stage 1 HUD Owner)  
**Reviewer:** ChatGPT (Cross-AI Reviewer)  
**Branch:** `feature/stage1-hud`  
**Target Module:** `:ui:hud`  

---

## 1. Architectural Overview & Separation of Concerns

This deliverable implements the mobile Garmin-like Head-Up Display (HUD) prototype in **Jetpack Compose** according to the `docs/HUD_WIREFRAME.md` specification.

### 1.1 Strict Boundary Invariants:
1. **Phone Surface ONLY:** `:ui:hud` is built with Jetpack Compose exclusively for phone mounts (windshield mount, dashboard dock, or motorcycle handlebar clamp).
2. **No Arbitrary Android Auto Projection:** Android Auto host projection requires rigid template models (`androidx.car.app.navigation.model.NavigationTemplate`). Compose canvas or arbitrary view hierarchies cannot be projected onto Android Auto head units. `:car:app` will consume the core DTOs independently in Stage 8.
3. **Decoupled State & Zero Live Engine Assumptions:**
   - Because routing and safety engines are scheduled for Stage 2/3, the HUD explicitly displays a prominent **DEMO / NO ROUTE** state instead of mocking fake real-time GPS speeds, fake ETAs, or fake speed camera triggers.
   - All HUD models (`NavHudUiState`) and formatting utilities (`HudFormatters`) are pure Kotlin data structures completely decoupled from Compose runtime, enabling headless JVM unit testing.
4. **Untouched Foreign Modules:** Does NOT touch `app/MainActivity.kt` or `ui/map/` (owned by ChatGPT in PR #7).

---

## 2. Implemented Components

| Component | File Path | Description & Compliance |
|---|---|---|
| **Theme & Colors** | `ui/hud/.../theme/HudTheme.kt` | OLED high-contrast dark palette (`#121212`), Emerald Green (`#00C853`), Azure Blue (`#00B0FF`), Crimson Red (`#D50000`), Vivid Amber (`#FF6D00`). |
| **HUD State Model** | `ui/hud/.../model/NavHudUiState.kt` | Pure data class holding telemetry, maneuver, alert, and vehicle states. Contains realistic demo fixtures for CAR, SCOOTER, and HEAVY_MOTORCYCLE. |
| **Telemetry Formatters** | `ui/hud/.../model/HudFormatters.kt` | Pure functions for `formatSpeed` (m/s to km/h), `formatDistance` (<1km rounded to 10m, >=1km in km), `formatDuration`, and vehicle labels. |
| **Taiwan Speed Sign** | `ui/hud/.../components/TaiwanSpeedLimitBadge.kt` | CNS 1438 regulatory circular speed limit sign with thick red circular ring, pure white background, and bold black numerals. Supports overspeed warning glow. |
| **Maneuver Icons** | `ui/hud/.../components/ManeuverIcon.kt` | Vector Canvas paths for high-glanceability turn arrows (Straight, Turn Left/Right, Keep Left/Right, U-Turn, Arrive). |
| **Maneuver Header** | `ui/hud/.../components/ManeuverGuidanceHeader.kt` | Turn direction icon, large countdown distance readout (>= 36sp), instruction text, and distance progress countdown bar. |
| **Speedometer Card** | `ui/hud/.../components/SpeedometerTelemetryCard.kt` | High-glanceability digital speed readout (>= 44sp), km/h label, Taiwan regulatory speed sign, and status badge. |
| **Safety Alert Toast** | `ui/hud/.../components/SafetyAlertBanner.kt` | Sourced alert banner (Fixed Speed Camera, Section Speed Enforcement, Traffic Enforcement) with source attribution and distance. |
| **Demo / No Route Notice** | `ui/hud/.../components/DemoNoRouteBanner.kt` | Stage 1 clear DEMO / NO ROUTE banner explaining prototype boundary, plus interactive quick-switch buttons for testing all 3 vehicle modes. |
| **Trip Summary Bar** | `ui/hud/.../components/TripSummaryBar.kt` | Remaining distance, arrival ETA, duration, offline continuation indicator, vehicle type chip, and safety touch targets (>= 56dp x 56dp). |
| **Adaptive Screen Layout** | `ui/hud/.../HudScreen.kt` | Automatically adapts to Landscape (3-column layout) and Portrait (vertical stacked layout). Contains 7 Compose `@Preview` fixtures. |

---

## 3. Supported Orientations & Vehicle Modes

### 3.1 Orientations:
- **Landscape (Car Windshield / Dashboard Mount):**
  - Top status bar: `[🚗 CAR] GPS: GOOD`
  - Left Column (~32%): Maneuver Card (`ManeuverGuidanceHeader`) + Demo control.
  - Center Column (~48%): Map Viewport Placeholder (Stage 1 architectural detachment).
  - Right Column (~20%): Speed & Alert Card (`SpeedometerTelemetryCard` + `SafetyAlertBanner`).
  - Bottom Bar: `TripSummaryBar` (Distance, ETA, Time, Vehicle Badge, Mute, Recenter).
- **Portrait (Motorcycle Handlebar / Phone Car Mount):**
  - Top status bar: `[🛵 SCOOTER] GPS: GOOD`
  - Top: Maneuver Card (`ManeuverGuidanceHeader`).
  - Alert / Demo Banner.
  - Middle: Map Viewport Placeholder.
  - Bottom: Speedometer Card (Left) + Trip Summary Bar (Right).

### 3.2 Vehicle Modes:
1. **`VehicleType.CAR` (小客車):** Configured for standard freeways, expressways, and urban navigation.
2. **`VehicleType.SCOOTER` (普通機車):** Configured with two-stage left turn待轉 cues and motorcycle-specific guidance prompts.
3. **`VehicleType.HEAVY_MOTORCYCLE` (大型重機):** Configured for expressway cruise speeds and legal route guidance.

---

## 4. Verification & Testing

- **JVM Unit Tests (`ui/hud/src/test/kotlin/`):**
  - `HudFormattersTest.kt`: Speed conversions (mps to km/h, null/NaN handling), distance rounding (<1000m and >=1000m), duration formatting, vehicle badges, overspeed logic.
  - `NavHudUiStateTest.kt`: Validates default state is strictly unrouted (`isUnrouted = true`, `speedLimitKmh = null`, `currentSpeedKmh = 0`), validates Car/Scooter/Heavy Motorcycle demo fixtures.
  - `HudVehicleModeTest.kt`: Exhaustive enum coverage and state immutability when toggling vehicle types.
- **Compose Previews:**
  - `HudLandscapeCarPreview`
  - `HudLandscapeScooterPreview`
  - `HudLandscapeHeavyMotorcyclePreview`
  - `HudPortraitCarPreview`
  - `HudPortraitScooterPreview`
  - `HudPortraitHeavyMotorcyclePreview`
  - `HudNoRoutePreview`
