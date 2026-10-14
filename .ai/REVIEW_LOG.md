# Cross-AI Review Log — Stage 0 Scaffold & Benchmark Review

**Document Version:** 1.2.0  
**Initial Review Date:** 2026-10-14  
**Revision Date:** 2026-10-14 (Addressing ChatGPT PR #3 Round 2 Review R9 & R10)  
**Authors / Reviewers:** Gemini (Stage 0 Deliverables Owner) & ChatGPT (Scaffold Owner & Reviewer)  
**Current PR:** [#3 (Target: `develop`)](https://github.com/michaely0418/GarminLikeNavigation/pull/3)  
**Status:** **REVISED / PENDING CHATGPT FINAL ACCEPTANCE**

---

## 1. Executive Summary & Verification Lifecycle

To avoid overclaiming test maturity and ensure safety-critical navigation accuracy, this project enforces four distinct verification levels:

* **L1 (`schema_validated`):** Validated by `python scripts/validate_benchmarks.py` against Draft 2020-12 JSON Schema.
* **L2 (`source_verified`):** Verified against authoritative, dated government gazettes or regulatory tables. Items relying on municipal portals or news pages are explicitly tagged `RECHECK_REQUIRED`.
* **L3 (`routing_executed`):** Headless routing engine path execution and geometry check. (**NOT RUN in Stage 0**; scheduled for Stage 2/3).
* **L4 (`road_tested`):** On-vehicle physical GPS road testing in Taiwan. (**NOT RUN in Stage 0**; scheduled for Stage 8/9).

---

## 2. ChatGPT First-Round Review Findings & Gemini Resolutions (PR #3)

| # | Item / File | Severity | Review Finding (ChatGPT) | Resolution / Action Taken (Gemini) | Evidence / Status |
|---|---|---|---|---|---|
| **R1** | `TW-SEG-15` / `docs/VEHICLE_RULES.md` & Benchmarks | **BLOCKING** | 台74線開放範圍誤植為 0k–37k 全線 ALLOWED。公路局公告為開放 `0k+000–34k+000`，**禁止 `34k+000–39k+235`**（大里二至霧峰端）。需拆分子路段並增設禁行案例。 | 1. `VEHICLE_RULES.md` 拆分為 `TW-SEG-15A`（0k–34k ALLOWED）與 `TW-SEG-15B`（34k–39.2k FORBIDDEN）。<br>2. Benchmark 增加 `TW-TC-17`（大里二至霧峰端 FORBIDDEN）與 `TW-TC-18`（快官至大里二 ALLOWED）。<br>3. 附上公路局精確公告連結。 | **RESOLVED**<br>[公路局快速公路公告表](https://www.thb.gov.tw/News_ExpresswaySection.aspx?n=462&sms=13790) |
| **R2** | `TW-SEG-04` / `docs/VEHICLE_RULES.md` & Benchmarks | **BLOCKING** | 台76線禁行里程誤植為 26k+500–31k+400。公路局公告禁行終點為 **`32k+600`**（林厝IC至中興系統段，含八卦山隧道及銜接段共計 6.1 公里）。 | 1. `VEHICLE_RULES.md` 更新里程為 `26k+500–32k+600`。<br>2. `taiwan_vehicle_benchmarks.json` 中的 `TW-TC-04` 更新案例文案、notes 與條文段落為 26k+500 至 32k+600 段。 | **RESOLVED**<br>[公路局快速公路公告表](https://www.thb.gov.tw/News_ExpresswaySection.aspx?n=462&sms=13790) |
| **R3** | `TW-SEG-01` / `docs/VEHICLE_RULES.md` & Benchmarks | **BLOCKING** | 國3甲來源精度不足（指向一般法律頁且字號日期有偏差）。需提供精確公告連結：高公局 101.06.28 管字第1016005186號公告（101.07.01 實施）。 | 1. `VEHICLE_RULES.md` 與 `TW-TC-01` 更新發布日期（2012-06-28）、生效日期（2012-07-01）與管字第1016005186號。<br>2. 直接連結更新為交通部法規檢索系統直接公報頁面。 | **RESOLVED**<br>[交通部法規公報 4686](https://motclaw.motc.gov.tw/webMotcLaw2018/SLaw/Content?soid=4686) |
| **R4** | Legal URLs & Verification Status | **BLOCKING** | 大量使用首頁或新聞列表作為 `legalReference` 卻標示 `VERIFIED`。需改為精確官方條文／公告連結，無單一永久公報者需降級。 | 1. 全面審查所有 18 筆 Benchmark 與路段矩陣。<br>2. 國1、國5、台64、台76、台74、國3甲連結至公路局/高公局管制表或全國法規資料庫單一條文。<br>3. 市民高架、新生高架、台北大橋、蘇花改、南迴改等暫無永久單一公報 ID 者，標籤全部降級為 `RECHECK_REQUIRED`，維持 fail-closed。 | **RESOLVED**<br>`taiwan_vehicle_benchmarks.json` & `docs/VEHICLE_RULES.md` |
| **R5** | `TW-SEG-12` / 省道快速公路通則 | **BLOCKING** | 台61與快速公路不可概括全線 ALLOWED；不可單憑 `highway=trunk` 或路號單獨推論。 | 在 `docs/VEHICLE_RULES.md` 明確註記：快速公路必須按公告里程逐段驗證（如台61香山平面交織段、八里端、鳳鼻尾隧道銜接處按現地標誌管制；台74 34k-39k 禁行等），禁止全線粗糙泛化。 | **RESOLVED**<br>`docs/VEHICLE_RULES.md#L4-L5` |
| **R6** | 驗證與驗收語意修正 | **BLOCKING** | `validate_benchmarks.py` 通過僅代表 JSON Schema 與案例數量有效，不能聲稱為完整驗證通過。需修正「16 verified」「G2 BASELINE READY」等過度宣稱。 | 1. 全面修正文件與記錄，改用 `schema_validated` (18 cases PASS)、`source_verified`（僅限已核對公報之案例）。<br>2. 明確標示 `routing_executed = NOT RUN`、`road_tested = NOT RUN`。<br>3. G2 狀態更正為 `IN PROGRESS (Specification Baseline & Curated Benchmark Draft)`。 | **RESOLVED**<br>`README.md`, `REVIEW_LOG.md`, `STAGE0_CHECKLIST.md` |
| **R7** | `mustTraverseSegments` 人工草稿狀態 | **BLOCKING** | 使用 `ROAD_NAME_HINT` 或 `TW-SEG-xx` 屬於人工草稿，在未建立 OSM Way ID 映射前不能當成可執行自動斷言。 | 在 `tests/routing-cases/README.md` 及 Benchmark 各案例之 `comments` 明確標註 `draft/manual` 狀態，並訂定 Stage 2 綁定 `OSM_WAY_ID` 與 `GEOMETRY_CORRIDOR` 之工作邊界。 | **RESOLVED**<br>`tests/routing-cases/README.md` |
| **R8** | CI Workflow Runner 修復 | **CI / BUILD** | PR #3 沿用舊版 workflow 會在 `setup-android@v3` 失敗。PR #2 已在 `develop` 修正 Android SDK 管理路徑。 | 將 `origin/develop`（commit `c4cc59b`）合併至本分支，帶入最新 `.github/workflows/android-ci.yml` 修復。 | **RESOLVED**<br>Merge commit `419b479` |
| **R9** | `TW-SEG-01` / `VEHICLE_RULES.md` & `TW-TC-01` | **HIGH / FIX REQUIRED** | 國3甲大型重機排氣量門檻誤植為「開放 550cc 以上」。高公局與國警局明確公告國3甲比照快速公路，全線開放排氣量逾 250c.c. 大型重型機車（含黃牌與紅牌）。 | 1. 更正 `VEHICLE_RULES.md` 國3甲門檻為 250c.c. 以上（含黃牌），補充高公局與國道警局FAQ連結。<br>2. `TW-TC-01` 新增 `vehicleAttributes: { engineDisplacementCc: 300, plateColor: "YELLOW" }` 屬性，確保引擎不排除黃牌。 | **RESOLVED**<br>[高公局國3甲通報](https://www.freeway.gov.tw/Publish.aspx?cnid=193&p=358)<br>[國道公路警察局FAQ](https://www.hpb.npa.gov.tw/ch/app/faq/view?id=2606&module=faq&serno=A24717) |
| **R10** | `TW-SEG-15` / `VEHICLE_RULES.md` & `TW-TC-17/18` | **HIGH / FIX REQUIRED** | 台74線 34K 分界為「草湖交流道」，非「大里二交流道」。大里二交流道位於主線 32K+854，草湖交流道位於 34K+000。 | 1. `VEHICLE_RULES.md` 正名 `TW-SEG-15A`（快官至草湖 0k-34k）與 `TW-SEG-15B`（草湖至霧峰 34k-39.2k），附上工程樁號佐證。<br>2. 更新 `TW-TC-17` 與 `TW-TC-18` 案例文案、草湖交流道經緯度座標與命名。 | **RESOLVED**<br>[公路局快速公路公告表](https://www.thb.gov.tw/News_ExpresswaySection.aspx?n=462&sms=13790)<br>[公路局工程樁號](https://thbu2.thb.gov.tw/cp.aspx?n=12995) |

---

## 3. Gemini Initial Review Findings on ChatGPT Scaffold (Preserved from v1.0.0)

| ID | File / Component | Severity | Finding | Proposal / Resolution | Evidence / Status |
|---|---|---|---|---|---|
| **ISSUE-01** | `gradlew.bat` (Line 6) | **MEDIUM** | Earlier revisions had accidental control characters / path syntax issues (`\b` in `\bootstrap`) causing PowerShell execution failure on Windows. | Confirmed line 6 contains clean literal string `scripts\bootstrap-gradle-wrapper.ps1`. No escape or syntax errors detected. | **RESOLVED**<br>Inspected binary bytes and executed file checks. |
| **ISSUE-02** | `core:model` / `VehicleModels.kt` | **LOW** | `RoadSegmentDescriptor` has `segmentId`, `osmHighwayTag`, and `attributes`, but lacks an explicit directional vector (e.g. `direction: String?` or `headingDeg: Double?`). Taiwan access rules (e.g. one-way expressway ramps, bridge bike lanes) are strictly directional. | Retain current DTO for Stage 0 freeze to prevent breaking ABI. In Stage 2, pass direction via `attributes["direction"]` or submit an ADR to add `travelDirection: DirectionHint?`. | **ACCEPTED / SCHEDULED STAGE 2**<br>`VehicleModels.kt#L14-L18` |
| **ISSUE-03** | `core:model` / `VehicleModels.kt` | **LOW** | `VehicleType` defines `CAR`, `SCOOTER`, `HEAVY_MOTORCYCLE`. In Taiwan, heavy motorcycles are legally categorized into Yellow plate (250–549cc) and Red plate (>=550cc). Although expressway access is currently identical under Act Art. 92-2, future freeway pilot trials or local rules may differentiate. | Preserve the three-enum ABI in `:core:model`. Differentiate displacement in `RouteRequest.vehicleAttributes` (e.g. `engine_displacement_cc = 300`) during Stage 2/3. | **ACCEPTED / SCHEDULED STAGE 3**<br>`VehicleModels.kt#L3` |
| **ISSUE-04** | `.github/workflows/android-ci.yml` | **LOW** | Line 31 executes `python3 scripts/validate_benchmarks.py --allow-draft`. Now that 18 cases are delivered, `--allow-draft` should eventually be removed to ensure regressions (<15 cases) fail CI. | Keep `--allow-draft` for initial bootstrap PR; remove in follow-up CI tightening once PR is merged into `develop`. | **SCHEDULED FOR STAGE 1**<br>`android-ci.yml#L35` |
| **ISSUE-05** | `core:interfaces` / `ISearchProvider.kt` | **LOW** | `search(request: SearchRequest)` returns `List<PlaceCandidate>` without pagination / cursor metadata. Large result sets or incremental autocomplete scrolls cannot be paginated. | Sufficient for Stage 0/Stage 1. When implementing live search in Stage 4, consider introducing `SearchResult(candidates: List<PlaceCandidate>, nextPageToken: String?)`. | **DEFERRED TO STAGE 4**<br>`ISearchProvider.kt#L8` |
| **ISSUE-06** | `settings.gradle.kts` / Toolchain | **MEDIUM** | Missing Java 17 toolchain auto-provisioning when running on Windows with higher JDK (e.g. JDK 21). | HOTFIX-003 added `org.gradle.toolchains.foojay-resolver-convention` version 1.0.0, resolving toolchain auto-download. | **RESOLVED**<br>`settings.gradle.kts#L18-L20` |

---

## 4. Current Verification & Execution Status

| Test / Inspection | Execution Command | Result | Verification Level & Notes |
|---|---|---|---|
| **Benchmark Schema & Case Count** | `python scripts/validate_benchmarks.py` | **PASS** | **L1 (`schema_validated`)**: 18 cases valid, unique case IDs, strictly conforms to `schema.json`. |
| **Legal Sourcing Audit** | Cross-check against official bulletins | **PASS (Partially Verified)** | **L2 (`source_verified`)**: Key national gazettes verified; municipal/news items explicitly downgraded to `RECHECK_REQUIRED`. |
| **PowerShell Bootstrap Path** | Binary string check on `gradlew.bat` line 6 | **PASS** | Confirmed clean string `scripts\bootstrap-gradle-wrapper.ps1`. |
| **Gradle Wrapper JAR Hash** | Check existing `gradle-wrapper.jar` | **PASS** | SHA-256 matches `81A82AAEA5ABCC8FF68B3DFCB58B3C3C429378EFD98E7433460610FECD7AE45F`. |
| **Routing Engine Execution** | N/A | **NOT RUN** | **L3 (`routing_executed`)**: Scheduled for Stage 2/3 Docker GraphHopper implementation. |
| **Physical Road Testing** | N/A | **NOT RUN** | **L4 (`road_tested`)**: Scheduled for Stage 8/9. |
| **GitHub Actions CI (Remote)** | PR #3 build workflow on GitHub | **IN PROGRESS / PENDING RUN** | Merged `c4cc59b` from `develop` to resolve SDK runner issue. |

---

## 5. Status of Project Gates G1–G6

* **G1 (Google Data Policy):** **BLOCKED (Fail-Closed).** Default `google_places_enabled = false`. Google Places coordinates will not be routed on MapLibre/OSM.
* **G2 (Taiwan Legal Access):** **IN PROGRESS (Specification Baseline Established / Engine Verification Pending Stage 2).** 18 segment records, split sub-segments, and benchmark drafts established.
* **G3 (Android Auto):** **NOT IMPLEMENTED.** Architecturally isolated in `:car:app`. Scheduled for Stage 8.
* **G4 (API Keys & Costs):** **POLICY ENFORCED.** Zero credentials in source code. BYOK Keystore storage design approved.
* **G5 (Offline Rerouting O1):** **NOT IMPLEMENTED.** Explicitly marked as non-goal for Stage 0. UI exposes degraded continuation indicator only.
* **G6 (Public Search Capacity):** **POLICY ENFORCED.** Public Nominatim limited to manual test calls with User-Agent and rate limiting.

---

## 6. Stage 0 Recommendation

**READY FOR CHATGPT SECOND-ROUND AUDIT.** All 8 blocking review items raised by ChatGPT have been addressed, verified via local schema tools, and committed to PR #3 targeting `develop`.
