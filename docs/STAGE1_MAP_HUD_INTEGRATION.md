# Stage 1 Map + HUD Integration Preview — 0.1.0

**Linked:** [Issue #9](https://github.com/michaely0418/GarminLikeNavigation/issues/9), [PR #7](https://github.com/michaely0418/GarminLikeNavigation/pull/7), [PR #8](https://github.com/michaely0418/GarminLikeNavigation/pull/8), [Draft PR #10](https://github.com/michaely0418/GarminLikeNavigation/pull/10)

## 本次改動項目
- 合併已審查的 MapLibre GPS 地圖與 Compose Garmin HUD 模組於一個 Android 專案。
- Android 主畫面顯示實際 MapLibre/OSM 地圖、定位、車種切換與網路狀態。
- 主畫面新增「HUD 展示（模擬資料）」按鈕，開啟獨立 `HudPreviewActivity`；提供「返回地圖」按鈕。
- HUD 規劃給手機，`GpsStatus` 在 Demo 狀態明確顯示「模擬」，測速/車速/ETA 為展示資料而非實時導航。
- 車種選擇經 SharedPreferences 同步，在 HUD 更換車種回主畫面可確認狀態。
- Gradle 加入 MapLibre + Compose + Activity Compose；CI 擴大為 core/model、core/navigation、ui/hud 測試與 app APK 編譯。

## 本次要測試項目
1. 在手機安裝整合 CI 產生的 APK；App 啟動不得閃退或回到 Stage 0 空白頁。
2. 地圖顯示、網路圖資、目前位置、MapLibre Attribution 仍正常。
3. Android 15 系統狀態列/導覽列不覆蓋頂部或操作按鈕。
4. 點選「HUD 展示」確實看到 Compose HUD；畫面有 DEMO/NO ROUTE 說明，非真實路徑。
5. 進入 HUD 時依地圖車種初始化；切換汽車/機車/重機後返回地圖，車種選擇同步，重開 App 仍保留。
6. HUD 切換模擬車種時頂端 GPS 顯示「GPS: 模擬」，不出現綠色的真實 GPS 良好宣告。
7. HUD 模擬執法 Banner 清楚標記「非實際執法點」，未接入真實測速資料。
8. HUD 無路線狀態時 ETA/速限不偽裝為即時資料。
9. 正常與 fontScale=1.3：直向 390×840、小尺寸 320×640、橫向 840×390 沒有控件溢出、被壓住或裁切。
10. 返回地圖後 GPS 更新、縮放、重新置中及日夜切換繼續運作。
11. 拒絕精確定位/僅概略定位/定位關閉/斷網不閃退，錯誤狀態清楚。
12. CI 運行 `:core:model:test :core:navigation:test :ui:hud:test :app:assembleDebug` + 18 個 Schema 案例驗證，APK artifact 可下載。

## 已知限制
- HUD 中央地圖畫面目前仍為 MapViewportPlaceholder；尚未整合即時 MapLibre View 至 HUD 畫布。
- 本階段不支援路線規劃、真實 ETA、測速/科技執法資料、AI 語音、Android Auto 或完整離線導航。
- 需要實機和 Emulator 的 UX/版面 QA。僅 CI 編譯成功不表示已測過 Android 裝置。
- G1 Google Places 授權仍 BLOCKED；G2 台灣三車種逐路段法規來源與導航算路仍 IN PROGRESS。

## Git 與驗收狀態
- 整合分支：`feature/stage1-map-hud-integration`（基於 develop）。
- PR #7 和 #8 保持 OPEN；本整合 PR #10 為 Draft。
- `main` 和 `develop` 在整合 QA 核准前均不修改。
- 正式產品使用不得將 HUD Demo Data 當作道路合法性、GNSS 訊號或測速警告的實測資料。

## 2026-10-14 Physical Device QA (Screenshots, Android 15 Xiaomi)

User supplied six screenshots (1 map landscape, 1 idle HUD portrait, 1 idle HUD landscape, 3 vehicle demo portraits).

### Screenshot-verified PASS
- Both phone Activity screens rendered without a crash: MapLibre map and Compose HUD.
- HUD portrait DEMO/NO ROUTE banner, no-route/Car/Scooter/Heavy selectors, warning data disclaimers, Azure Blue `GPS: 模擬`, red circular speed-limit badges.
- Stage 1 HUD landscape layout has three panels and a bottom bar (the left guidance/control panel is scrollable).
- `返回地圖` navigation control visibly present (its click/return effect not independently proven by a screenshot).

### Blocking issue LANDSCAPE-MAP-01
- On a wide landscape screen the map is compressed to a very thin horizontal strip while header, vehicle, HUD, zoom and status controls dominate vertical space.
- Root cause: all controls and the map used the same `LinearLayout.VERTICAL`; the map is the only height-weighted view and receives negligible remaining height.
- Hotfix: landscape `LinearLayout.HORIZONTAL` with the MapLibre view occupying the remaining width and a right `ScrollView` sidebar (260–360dp) for toolbar and status. Portrait remains unchanged. Attribution remains on the map, not behind controls.
- **Post-fix actual phone render NOT RUN** until Product Owner installs a new successful CI artifact.

### Non-blocking HUD landscape usability follow-up
- Landscape screenshot shows part of the orange guidance subtitle ellipsized and the DEMO selector card partly below the visible left column. `verticalScroll` exists, but actual reachability of the controls must be verified by swiping that column.
- Separate narrow 320dp / fontScale 1.3 screenshots remain NOT RUN; do not imply they passed.

### Additional device QA required
1. Landscape map occupies most of the screen and shows streets/attribution; right controls can be scrolled without dragging the map.
2. Landscape map zoom, recenter, day/night, vehicle selection and HUD entry work; portrait after rotation remains usable.
3. Landscape HUD left column scrolls to the DEMO controls without covering the bottom status bar; directions remain readable.
4. HUD change vehicle mode, return to map, check shared persisted selection.
5. Keep explicit DEMO notices and absence of real routing/speed-trap guarantees.
