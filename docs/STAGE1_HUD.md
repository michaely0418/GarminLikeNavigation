# Stage 1 — Mobile Compose HUD Prototype (`:ui:hud`)

**Document Version:** 1.2.0  
**Effective Date:** 2026-10-14  
**Revision Date:** 2026-10-14 (Addressing ChatGPT PR #8 Round 2 Review R2-H1 & R2-H3)  
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

## 2. Review Resolutions & Verification Lifecycle

### 2.1 Round 1 Review (H1–H5):
| Item | Severity | ChatGPT Review Finding | Gemini Resolution | Status / Evidence |
|---|---|---|---|---|
| **H1** | **HIGH** | `TopStatusBar` 預設以 `!isGpsLost` 顯示 `GPS: 良好`，會虛報定位狀態。 | 導入四態列舉 `GpsStatus` (`UNKNOWN`, `SEARCHING`, `VALID`, `LOST`)；`demoInitial` 預設為 `UNKNOWN`（顯示「GPS: 未連接」）；僅在獲取有效 Fix 時始顯示「良好」。 | **RESOLVED**<br>`NavHudUiState.kt`, `HudFormatters.kt`, `HudFormattersTest.kt` |
| **H2** | **HIGH** | 模擬測速資料使用警政署等具體機關名稱與 `confidence=1.0`，偽裝成真實官方資料。 | 1. 模擬資料一律標記 `DEMO_MOCK_` 前綴、`confidence = 0.0`、`routeCorrelated = false`、無 evidenceId。<br>2. `SafetyAlertBanner` 顯示醒目 `[⚡ 模擬資料 (非實際執法點)]`。<br>3. 新增單元測試防禦。 | **RESOLVED**<br>`SafetyAlertBanner.kt`, `NavHudUiStateTest.kt` |
| **H3** | **HIGH** | Portrait 底部 `SpeedometerTelemetryCard` 與 `TripSummaryBar` 置於同一 Row，在 390dp/320dp 螢幕或大字體下會產生橫向裁切；`DemoModeButton` 高度不足。 | 1. 重構 Portrait 底部：改為雙欄 `[數位儀表] + [直向旅程卡]`，下方配置全寬 `PortraitActionBar`。<br>2. `DemoModeButton` 與底欄按鍵全面保證 $\ge 48\text{dp}$ 駕駛觸控高度。<br>3. 支援 320dp 窄螢幕與 `fontScale >= 1.3`。 | **RESOLVED**<br>`HudScreen.kt`, `TripSummaryBar.kt`, `DemoNoRouteBanner.kt` |
| **H4** | **MEDIUM** | `DemoNoRouteBanner` 的車種按鈕 active 狀態判定錯誤（機車／重機永遠 false，汽車永遠 true）。 | 引入 `DemoModeSelection` 列舉與 `demoSelection` 衍生狀態；無路線時高亮「無路線」，模擬引導時依據 `vehicleType` 精確高亮汽車、機車或重機。 | **RESOLVED**<br>`DemoNoRouteBanner.kt`, `NavHudUiStateTest.kt` |
| **H5** | **INTEGRATION** | 與 PR #7 共用檔案變更（`libs.versions.toml`, `android-ci.yml`）需整合驗證；APK 需有整合入口。 | 1. 訂定 PR #7 與 PR #8 合併 recipe，整合後 CI 需同時執行 `:core:model:test :core:navigation:test :ui:hud:test :app:assembleDebug`。<br>2. 本 PR 保持分離邊界，待 Stage 1 驗收階段再由協調 PR 串接 Activity。 | **DOCUMENTED**<br>`docs/STAGE1_HUD.md`, `.ai/CURRENT_TASK.md` |

### 2.2 Round 2 Review (R2-H1, R2-H3):
| Item | Severity | ChatGPT Review Finding | Gemini Resolution | Status / Evidence |
|---|---|---|---|---|
| **R2-H1** | **MEDIUM** | Demo 模式即使 `GpsStatus.VALID` 也不能顯示「GPS: 良好」，應誠實標記「GPS: 模擬」並使用中性藍色，避免駕駛誤判為真實定位。 | 1. 擴充純函式 `formatHudGpsLabel(status, isDemoMode)`：在 `isDemoMode = true` 且 `VALID` 時回傳 `"GPS: 模擬"`，色彩映射為 `HudColors.AzureBlue`。<br>2. 僅在 `!isDemoMode` 且 `VALID` 時顯示「GPS: 良好」（翡翠綠）。<br>3. 新增完整單元測試覆蓋 Demo 狀態下的文字與顏色映射。 | **RESOLVED**<br>`HudFormatters.kt`, `HudScreen.kt`, `HudFormattersTest.kt` |
| **R2-H3** | **MEDIUM** | Portrait 旅程卡在極端 320dp 寬度與 `fontScale >= 1.3` 下，剩餘里程與抵達時間並排仍有換行擠壓風險；且需嚴謹標註實機驗證狀態。 | 1. `PortraitTripStatsCard` 重構為垂直單欄堆疊（剩餘里程、預計抵達、行車時間各占獨立一行），獲得完整卡片寬度，徹底根絕橫向排版擠壓。<br>2. 橫向左側欄追加 `verticalScroll` 防禦小高度螢幕。<br>3. 實機 Compose UI 渲染標記為 **`NOT RUN`**（待後續真機 QA 執行），不虛報驗證結果。 | **RESOLVED**<br>`TripSummaryBar.kt`, `HudScreen.kt` |

---

## 3. Implemented Components

| Component | File Path | Description & Compliance |
|---|---|---|
| **Theme & Colors** | `ui/hud/.../theme/HudTheme.kt` | OLED high-contrast dark palette (`#121212`), Emerald Green (`#00C853`), Azure Blue (`#00B0FF`), Crimson Red (`#D50000`), Vivid Amber (`#FF6D00`). |
| **HUD State Model** | `ui/hud/.../model/NavHudUiState.kt` | Pure data class holding telemetry, maneuver, alert, and vehicle states. Contains realistic demo fixtures for CAR, SCOOTER, and HEAVY_MOTORCYCLE. |
| **Telemetry Formatters** | `ui/hud/.../model/HudFormatters.kt` | Pure functions for `formatSpeed` (m/s to km/h), `formatDistance` (<1km rounded to 10m, >=1km in km), `formatDuration`, and vehicle labels. |
| **Taiwan Speed Sign** | `ui/hud/.../components/TaiwanSpeedLimitBadge.kt` | CNS 1438 regulatory circular speed limit sign with thick red circular ring, pure white background, and bold black numerals. Supports overspeed warning glow. |
| **Maneuver Icons** | `ui/hud/.../components/ManeuverIcon.kt` | Vector Canvas paths for high-glanceability turn arrows (Straight, Turn Left/Right, Keep Left/Right, U-Turn, Arrive). |
| **Maneuver Header** | `ui/hud/.../components/ManeuverGuidanceHeader.kt` | Turn direction icon, large countdown distance readout (>= 36sp), instruction text, and distance progress countdown bar. |
| **Speedometer Card** | `ui/hud/.../components/SpeedometerTelemetryCard.kt` | High-glanceability digital speed readout (>= 44sp), km/h label, Taiwan regulatory speed sign, and status badge. |
| **Safety Alert Toast** | `ui/hud/.../components/SafetyAlertBanner.kt` | Sourced alert banner (Fixed Speed Camera, Section Speed Enforcement, Traffic Enforcement) with source attribution, distance, and mock disclaimer. |
| **Demo / No Route Notice** | `ui/hud/.../components/DemoNoRouteBanner.kt` | Stage 1 clear DEMO / NO ROUTE banner explaining prototype boundary, plus interactive quick-switch buttons (>=48dp touch targets) for testing all 3 vehicle modes. |
| **Landscape Trip Summary** | `ui/hud/.../components/TripSummaryBar.kt#TripSummaryBar` | Remaining distance, arrival ETA, duration, offline continuation indicator, vehicle type chip, and safety touch targets (>= 56dp x 56dp). |
| **Portrait Trip Stats** | `ui/hud/.../components/TripSummaryBar.kt#PortraitTripStatsCard` | Dedicated vertical card for remaining distance, arrival ETA, and duration. Solves H3 horizontal squeezing. |
| **Portrait Action Bar** | `ui/hud/.../components/TripSummaryBar.kt#PortraitActionBar` | Full-width bottom action row for vehicle mode switching, audio mute, and reset (>=48dp height). |
| **Adaptive Screen Layout** | `ui/hud/.../HudScreen.kt` | Automatically adapts to Landscape (3-column layout) and Portrait (vertical stacked layout). Contains 9 Compose `@Preview` fixtures. |

---

## 4. Supported Orientations & Vehicle Modes

### 4.1 Orientations:
- **Landscape (Car Windshield / Dashboard Mount):**
  - Top status bar: `[🚗 CAR] GPS: 未連接 / 良好`
  - Left Column (~32%): Maneuver Card (`ManeuverGuidanceHeader`) + Demo control.
  - Center Column (~48%): Map Viewport Placeholder (Stage 1 architectural detachment).
  - Right Column (~20%): Speed & Alert Card (`SpeedometerTelemetryCard` + `SafetyAlertBanner`).
  - Bottom Bar: `TripSummaryBar` (Distance, ETA, Time, Vehicle Badge, Mute, Recenter).
- **Portrait (Motorcycle Handlebar / Phone Car Mount):**
  - Top status bar: `[🛵 SCOOTER] GPS: 未連接 / 良好`
  - Maneuver Card (`ManeuverGuidanceHeader`).
  - Alert / Demo Banner.
  - Middle: Map Viewport Placeholder (`Modifier.weight(1f)`).
  - Bottom Telemetry: `[數位儀表] + [直向旅程卡]`
  - Bottom Actions: `PortraitActionBar` (`[🚗 汽車 (切換)]` + `[🔊]` + `[🔄]`).

### 4.2 Vehicle Modes:
1. **`VehicleType.CAR` (小客車):** Configured for standard freeways, expressways, and urban navigation.
2. **`VehicleType.SCOOTER` (普通機車):** Configured with two-stage left turn待轉 cues and motorcycle-specific guidance prompts.
3. **`VehicleType.HEAVY_MOTORCYCLE` (大型重機):** Configured for expressway cruise speeds and legal route guidance.

---

## 5. Verification & Testing

- **JVM Unit Tests (`ui/hud/src/test/kotlin/` — 18 `@Test` methods total, 100% PASS):**
  - `HudFormattersTest.kt` (11 tests): Speed conversions (mps to km/h, null/NaN handling), distance rounding (<1000m and >=1000m), duration formatting, vehicle badges, overspeed logic, and `formatHudGpsLabel` covering both demo mode (`GPS: 模擬`) and live GPS (`GPS: 良好`) states (H1, R2-H1).
  - `NavHudUiStateTest.kt` (5 tests): Validates default state is strictly unrouted (`isUnrouted = true`, `gpsStatus = UNKNOWN`, `speedLimitKmh = null`, `currentSpeedKmh = 0`), validates Car/Scooter/Heavy Motorcycle demo fixtures with mock alerts (H2), validates `demoSelection` (H4).
  - `HudVehicleModeTest.kt` (2 tests): Exhaustive enum coverage and state immutability when toggling vehicle types.
- **Compose Previews (9 total, Compilation verified):**
  - `HudLandscapeCarPreview`
  - `HudLandscapeScooterPreview`
  - `HudLandscapeHeavyMotorcyclePreview`
  - `HudPortraitCarPreview` (390x840)
  - `HudPortraitScooterPreview` (390x840)
  - `HudPortraitHeavyMotorcyclePreview` (390x840)
  - `HudNoRoutePreview` (390x840)
  - `HudPortraitNarrow320Preview` (320x640 narrow test)
  - `HudPortraitLargeFontPreview` (390x840 with `fontScale = 1.3f`)
- **Device UI Execution Status:**
  - **`NOT RUN` (Physical Device UI Rendering):** Following cross-review alignment, on-device UI rendering is classified as `NOT RUN` pending physical device QA / instrumented UI testing by Product Owner / Test Team on hardware. Only JVM headless unit tests and Compose preview compilation have been verified in this PR.

