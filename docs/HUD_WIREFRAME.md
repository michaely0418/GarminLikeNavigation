# Mobile HUD Wireframe & Android Auto Boundary Specification — Stage 0

**Document Version:** 1.0.0  
**Effective Date:** 2026-10-14  
**Primary Author / Reviewer:** Gemini (Stage 0 Owner) / ChatGPT (Reviewer)  
**Target Module:** `:ui:hud` (Android Jetpack Compose UI) vs `:car:app` (Car App Library)

---

## 1. Architectural Boundary & Separation of Concerns

This specification governs the design of the in-drive Head-Up Display (HUD) for mobile phones.

```text
                  +-----------------------------------+
                  |       INavigationCore (JVM)       |
                  |     - StateFlow<NavState>         |
                  |     - Flow<NavigationVoicePrompt> |
                  +-----------------------------------+
                                    |
                    +---------------+---------------+
                    |                               |
                    v                               v
        +-----------------------+       +-----------------------+
        |        :ui:hud        |       |        :car:app       |
        |  (Android / Compose)  |       |   (Car App Library)   |
        | - Phone Display ONLY  |       | - Head Unit (AA) ONLY |
        | - High-contrast HUD   |       | - NavigationTemplate  |
        | - Custom animations   |       | - Host Template Enforced
        | - Full touch gesture  |       | - Strict Driver Rules |
        +-----------------------+       +-----------------------+
```

### 1.1 Strict Invariants:
1. **Phone Surface vs Car Screen:** `:ui:hud` is built strictly with Jetpack Compose for Android smartphones (windshield mount, dashboard dock, or motorcycle handlebar clamp).
2. **No Arbitrary Compose in Android Auto:** Android Auto host projection enforces rigid driver distraction guidelines via `androidx.car.app.navigation.model.NavigationTemplate`. Arbitrary Compose layouts or Canvas overlays CANNOT be projected onto Android Auto head units. `:car:app` will consume the same core DTOs independently in Stage 8.
3. **Pure State Consumer:** `:ui:hud` does not calculate routes, speed limits, or camera warnings. It observes `NavState` from `INavigationCore` and `List<SafetyAlert>` from `ISafetyEngine`.

---

## 2. Visual Ergonomics & Garmin-Like Design Language

* **Color Palette (High-Contrast Dark Theme):**
  * Background: `#121212` (OLED black battery-saver for two-wheelers).
  * Maneuver Accent: Emerald Green (`#00C853`) for standard highway/turns; Azure Blue (`#00B0FF`) for complex multi-lane splits.
  * Safety Alerts & Speed Violations: Crimson Red (`#D50000`) for active camera alerts and over-speed; Vivid Amber (`#FF6D00`) for approaching caution zones.
  * Typography: Ultra-bold sans-serif numerals; minimum 44sp for digital speedometer, 36sp for turn distance countdown.
* **Taiwan Regulatory Speed Limit Badge:** Circular sign with white background (`#FFFFFF`), thick red circular ring (`#D50000`), and bold black numerals (`#000000`).
* **Glanceability Rule:** All critical information (speed, speed limit, next turn direction and distance) must be legible within a 1.5–2.0 second glance under direct sunlight.

---

## 3. UI Layout Wireframes (ASCII Mockups)

### 3.1 Landscape Wireframe (Car Windshield / Dashboard Mount)

```text
+----------------------------------------------------------------------------------------------------+
|  [🚗 CAR] GPS: GOOD                                                   ⚡ OFFLINE CONTINUATION     |
+------------------------------------+-------------------------------------------+-------------------+
| << NEXT MANEUVER >>                | << LIVE MAP & JUNCTION VIEW >>            | << SPEED & ALERTS >>|
|                                    |                                           |                   |
|       / \                          |     |     |   \                       |     +---------+   |
|      /   \    350 m                |     |  *  |    \                      |     |  ( 60 ) |   |
|        |                           |     |     |     \                     |     |  LIMIT  |   |
|        |                           |     |     |      \                    |     +---------+   |
|                                    |     |  ^  |       |                   |                   |
| 靠左行駛 進入                      |     |  |  |       |                   |       58          |
| 國道三號甲線 (台北聯絡道)          |     |     |       |                   |       km/h        |
|                                    |     (Route Polyline)                      |                   |
| 車道導引:                          |                                           | [!] 前方 400m     |
| [ ^ ][ ^ ][ \ ][ \ ]               |                                           | 固定測速 60km/h   |
|   |    |                           |                                           | 警政署 2026-09    |
+------------------------------------+-------------------------------------------+-------------------+
| 剩餘: 4.8 km  |  抵達: 17:45  |  時間: 8 分鐘                                  | [🔇 靜音] [🔄 重設]|
+----------------------------------------------------------------------------------------------------+
```

### 3.2 Portrait Wireframe (Motorcycle Handlebar / Phone Car Mount)

```text
+---------------------------------------------------------+
| [🛵 SCOOTER]  GPS: EXCELLENT               17:05  98%🔋 |
+---------------------------------------------------------+
| << MANEUVER CARD >>                                     |
|    |                                                    |
|  --+-->     150 m                                       |
|    |                                                    |
| 兩段式左轉  (依標誌待轉)                                |
| 至 重新路一段 / 機車專用道引道                          |
| 車道提示: [機車兩段待轉區]                              |
+---------------------------------------------------------+
| << CAMERA / SAFETY ALERT TOAST >>                       |
| [!] 區間測速起點 | 距離 250m | 速限 50 km/h (全天候執法) |
+---------------------------------------------------------+
|                                                         |
|                << LIVE MAP VIEW >>                      |
|                                                         |
|                        |                                |
|                        |   /                            |
|                        v--/                             |
|                       /                                 |
|                                                         |
+---------------------------------------------------------+
| << SPEED & SPEED LIMIT >>    | << TRIP STATS & PROGRESS >>|
|        +---------+           |                          |
|        |  ( 50 ) |           | 剩餘: 2.3 km             |
|        +---------+           |                          |
|                              | 抵達: 17:12              |
|            46                |                          |
|            km/h              | 預計: 7 分鐘             |
|                              |                          |
| [車速正常]                   | [ 重新規劃 ]             |
+---------------------------------------------------------+
```

---

## 4. Key UI Components & State Flow

### 4.1 State Hierarchy (`NavHudUiState`)
```kotlin
data class NavHudUiState(
    val vehicleType: VehicleType = VehicleType.CAR,
    val phase: NavigationPhase = NavigationPhase.IDLE,
    val currentSpeedKmh: Int = 0,
    val speedLimitKmh: Int? = null,
    val isOverspeed: Boolean = false,
    val nextManeuver: Maneuver? = null,
    val distanceToManeuverMeters: Double? = null,
    val activeAlert: SafetyAlert? = null,
    val remainingDistanceKm: Double = 0.0,
    val remainingDurationMinutes: Long = 0,
    val etaFormatted: String = "--:--",
    val isOfflineMode: Boolean = false,
    val isGpsLost: Boolean = false
)
```

### 4.2 Component Breakdown
1. **`ManeuverGuidanceHeader`**:
   * Displays directional SVG arrow (Turn Left, Right, Fork, Merge, U-Turn, Roundabout).
   * Countdown distance bar animated in sync with `distanceToManeuverMeters`.
   * Clear, localized instruction text in Traditional Chinese (e.g., `靠右行駛 進入匝道`).
2. **`SpeedSafetyTelemetryCard`**:
   * Digital speed readout derived from `LocationSample.speedMps * 3.6`.
   * Standard Taiwan circular speed limit sign with overspeed pulsing ring when `currentSpeedKmh > speedLimitKmh`.
3. **`SafetyAlertBanner`**:
   * Sourced alert badge (`sourceId`, `sourcePublishedAtEpochMillis`).
   * Visual icon for Speed Camera, Section Enforcement, Red Light Camera.
   * Distance countdown along route (`distanceAlongRouteMeters`).
4. **`TripStatusBar`**:
   * Formatted remaining distance (`X.X km`), estimated duration, and arrival clock (`HH:mm`).
   * Vehicle mode chip (`[CAR]`, `[SCOOTER]`, `[HEAVY_MOTORCYCLE]`).
   * Offline continuation chip (`[離線模式]`) displayed when `NavState.offlineRouteContinuation == true`.

---

## 5. Touch Target & Safety Constraints

1. **Minimum Touch Area:** Minimum 56dp x 56dp on all interactive elements (Mute, Recenter, Reroute, Stop).
2. **Driver Distraction Lockout:**
   * When moving (`speedMps > 1.5` / ~5 km/h), complex configuration settings, search inputs, and list scrolling are locked.
   * Only one-touch emergency actions are exposed during guidance: `Mute Audio`, `Toggle Overview Map`, and `Cancel Navigation`.

---

## 6. Android Auto Cross-Boundary Matrix

| Feature | Phone HUD (`:ui:hud`) | Android Auto (`:car:app`) |
|---|---|---|
| **Rendering Framework** | Jetpack Compose (`androidx.compose.ui`) | Car App Library (`androidx.car.app`) Templates |
| **Host Lifecycle** | Standard Android `Activity` / `ViewModel` | `CarAppService` / `Session` / `Screen` |
| **Custom UI Elements** | Unrestricted Canvas, vector graphics, custom fonts | Restricted to `NavigationTemplate`, `Trip`, `Step`, `Maneuver` |
| **Driver Lockout Policy** | App-managed safety lock (speed threshold) | Google Play Automotive strict template & step limits |
| **Target Hardware** | Smartphone screen (portrait or landscape mount) | Car in-dash head unit display (via USB/Wireless AA) |
| **Data Sharing** | Collects directly from `:core:interfaces` | Collects directly from `:core:interfaces` |
