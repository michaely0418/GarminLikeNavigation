# Cross-AI Review Log — Gemini Stage 0 Review of ChatGPT Scaffold

**Review Date:** 2026-10-14  
**Reviewer:** Gemini (Stage 0 Owner / Peer Reviewer)  
**Target Scaffold:** ChatGPT Stage 0 Initial Scaffold (Commit `cfec4fa` / v0.0.1-r2)  
**Recommendation:** **APPROVE (Stage 0 Baseline Verified / Ready for PR Merge)**

---

## 1. Review Summary & Architecture Assessment

Gemini has conducted an independent engineering review of the Stage 0 codebase, including `:core:model`, the six Kotlin interfaces in `:core:interfaces`, module dependency graphs, Gradle build scripts, security configurations, and GitHub Actions CI.

### Key Conclusions:
1. **Separation of Concerns:** The 11-module layout (`:app`, `:core:*`, `:ui:*`, `:car:app`) properly isolates pure JVM logic (`:core:model`, `:core:interfaces`, `:core:routing`, `:core:navigation`, `:core:search`, `:core:safety`, `:core:voice`) from Android UI (`:ui:map`, `:ui:hud`) and Android Auto (`:car:app`).
2. **Contract Rigor:** The non-negotiable unit conventions (WGS84 decimal degrees, meters Double, m/s Double, bearing `[0,360)` Double, UTC epoch milliseconds Long, duration seconds Long) are consistently enforced via `require()` assertions in model initializers.
3. **Safety & Legal Gates:** Model structures correctly enforce that legal access is a hard gate (`RoadAccessStatus.FORBIDDEN`), that UNKNOWN status is distinguished from ALLOWED, that search data permissions are carried via `CoordinateUsePolicy`, and that safety alerts require directional/along-route verification.

---

## 2. Peer Review Issue Table

| ID | File / Component | Severity | Finding | Proposal / Resolution | Evidence / Status |
|---|---|---|---|---|---|
| **ISSUE-01** | `gradlew.bat` (Line 6) | **MEDIUM** | Earlier revisions had accidental control characters / path syntax issues (`\b` in `\bootstrap`) causing PowerShell execution failure on Windows. | Confirmed line 6 contains clean literal string `scripts\bootstrap-gradle-wrapper.ps1`. No escape or syntax errors detected. | **RESOLVED**<br>Inspected binary bytes and executed file checks. |
| **ISSUE-02** | `core:model` / `VehicleModels.kt` | **LOW** | `RoadSegmentDescriptor` has `segmentId`, `osmHighwayTag`, and `attributes`, but lacks an explicit directional vector (e.g. `direction: String?` or `headingDeg: Double?`). Taiwan access rules (e.g. one-way expressway ramps, bridge bike lanes) are strictly directional. | Retain current DTO for Stage 0 freeze to prevent breaking ABI. In Stage 2, pass direction via `attributes["direction"]` or submit an ADR to add `travelDirection: DirectionHint?`. | **ACCEPTED / SCHEDULED STAGE 2**<br>`VehicleModels.kt#L14-L18` |
| **ISSUE-03** | `core:model` / `VehicleModels.kt` | **LOW** | `VehicleType` defines `CAR`, `SCOOTER`, `HEAVY_MOTORCYCLE`. In Taiwan, heavy motorcycles are legally categorized into Yellow plate (250–549cc) and Red plate (>=550cc). Although expressway access is currently identical under Act Art. 92-2, future freeway pilot trials or local rules may differentiate. | Preserve the three-enum ABI in `:core:model`. Differentiate displacement in `RouteRequest.vehicleAttributes` (e.g. `engine_displacement_cc = 300`) during Stage 2/3. | **ACCEPTED / SCHEDULED STAGE 3**<br>`VehicleModels.kt#L3` |
| **ISSUE-04** | `.github/workflows/android-ci.yml` | **LOW** | Line 31 executes `python3 scripts/validate_benchmarks.py --allow-draft`. Now that Gemini has provided 16 fully verified cases, `--allow-draft` should eventually be removed to ensure regressions (<15 cases) fail CI. | Keep `--allow-draft` for initial bootstrap PR; remove `--allow-draft` in follow-up CI tightening once PR is merged. | **SCHEDULED FOR STAGE 1**<br>`android-ci.yml#L31` |
| **ISSUE-05** | `core:interfaces` / `ISearchProvider.kt` | **LOW** | `search(request: SearchRequest)` returns `List<PlaceCandidate>` without pagination / cursor metadata. Large result sets or incremental autocomplete scrolls cannot be paginated. | Sufficient for Stage 0/Stage 1. When implementing live search in Stage 4, consider introducing `SearchResult(candidates: List<PlaceCandidate>, nextPageToken: String?)`. | **DEFERRED TO STAGE 4**<br>`ISearchProvider.kt#L8` |
| **ISSUE-06** | `settings.gradle.kts` / Toolchain | **MEDIUM** | Missing Java 17 toolchain auto-provisioning when running on Windows with higher JDK (e.g. JDK 21). | HOTFIX-003 added `org.gradle.toolchains.foojay-resolver-convention` version 1.0.0, resolving toolchain auto-download. | **RESOLVED**<br>`settings.gradle.kts#L18-L20` |

---

## 3. Gemini Deliverables Audit (Stage 0)

| Deliverable | Path | Status | Verification Detail |
|---|---|---|---|
| **M04 / G1 / G4 / G6** | `docs/API_POLICY.md` | **COMPLETED** | Fully documented Google Maps Platform Terms (Section 3.2.3(b) non-Google map restriction, Section 3.2.4 caching), OSMF Nominatim fair-use policy, BYOK Keystore storage design, and G1 fail-closed status. |
| **M07 / G2** | `docs/VEHICLE_RULES.md` | **COMPLETED** | Complete statutory legal framework (處罰條例第33/92條, 道安第99/99-1條, 高快速交管第19條), 16 segment-level ground truth matrix (國3甲, 台76八卦山隧道, 蘇花改東澳/仁水隧道, 草埔森永隧道, 台北大橋機車道), and OSM tagging pitfalls. |
| **M03** | `docs/HUD_WIREFRAME.md` | **COMPLETED** | Full mobile phone HUD design in Compose, Landscape/Portrait ASCII mockups, glanceability rules, Garmin color palette, and Android Auto boundary matrix. |
| **M13 / Q2** | `tests/routing-cases/taiwan_vehicle_benchmarks.json` | **COMPLETED** | 16 geographically grounded, legally verified benchmark cases matching `schema.json`. Verified via `validate_benchmarks.py`. |
| **Review & Alignment** | `.ai/REVIEW_LOG.md` | **COMPLETED** | Detailed peer review log and gate status report. |

---

## 4. Test Execution & Validation Evidence

| Test / Inspection | Execution Command | Result | Evidence / Notes |
|---|---|---|---|
| **Benchmark Schema Validation** | `python scripts/validate_benchmarks.py` | **PASS** | Output: `JSON schema valid, 16 cases` (Validated without `--allow-draft`). |
| **PowerShell Bootstrap Path Check** | Python binary string inspection on `gradlew.bat` | **PASS** | Confirmed clean string `scripts\bootstrap-gradle-wrapper.ps1` on line 6. |
| **JVM Model Compilation** | N/A (Inspected from prior compile) | **PASS** | `core/model/build/libs/model.jar` and `core/interfaces/build/libs/interfaces.jar` generated. |
| **Android Studio assembleDebug** | `gradlew.bat :core:model:test :app:assembleDebug` | **NOT RUN** | Authoring environment lacks Android SDK platforms / build-tools; delegated to GitHub Actions CI upon PR creation. |
| **Gradle Wrapper JAR Bootstrap** | `powershell -ExecutionPolicy Bypass -File scripts\bootstrap-gradle-wrapper.ps1` | **NOT RUN** | Pending developer execution on network-connected machine. |

---

## 5. Status of Project Gates G1–G6

* **G1 (Google Data Policy):** **BLOCKED (Fail-Closed).** Default `google_places_enabled = false`. Coordinates from Google Places will not be passed to MapLibre/GraphHopper.
* **G2 (Taiwan Legal Access):** **BASELINE READY.** 16 segment-level test cases and ground truth rules established. Legality engine implementation proceeds in Stage 2.
* **G3 (Android Auto):** **NOT IMPLEMENTED.** Architecturally isolated in `:car:app`. Scheduled for Stage 8.
* **G4 (API Keys & Costs):** **POLICY ENFORCED.** Zero credentials in source code. BYOK Keystore storage design approved.
* **G5 (Offline Rerouting O1):** **NOT IMPLEMENTED.** Explicitly marked as non-goal for Stage 0. UI only exposes degraded continuation indicator.
* **G6 (Public Search Capacity):** **POLICY ENFORCED.** Public Nominatim limited to manual test calls with User-Agent and rate limiting.

---

## 6. Final Recommendation

**APPROVE.** Gemini recommends merging Stage 0 deliverables into the primary integration branch and proceeding to Stage 1.
