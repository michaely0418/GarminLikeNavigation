# Stage 2 Routing PoC — 實作前決策與比較規格

**狀態：PREPARATION ONLY / NOT RUN.** 依 [Issue #11](https://github.com/michaely0418/GarminLikeNavigation/issues/11) 進行；與已穩定的 Stage 1 v0.1.0 分開。**不直接修改 `develop` 或 `main`。**

## 架構與安全閘門

- PoC 第一階段以**自建 Docker 後端**為主，完全遵守已決議的 `docs/ADR-001-routing-deployment.md`。
- `:core:routing` 與 Android Stage 1 UI 保持隔離，現在不把任何示範路由送入 HUD/地圖冒充導航。
- `docs/API_POLICY.md` G1（Google Places）仍 **BLOCKED Fail Closed**；絕不將 Places 坐標作為非 Google 路由引擎資料來源。
- `docs/VEHICLE_RULES.md` G2 仍 **IN PROGRESS**，不以 OSM `highway` 單一標籤當台灣車種通行證據。
- `FORBIDDEN` 路段必須從可行圖 hard reject，而非以大懲罰分數降低優先度；`UNKNOWN` 高風險段 fail closed。
- Stage 2 所有 Benchmark 狀態**必須區分 Schema-only、Engine-tested、Law-verified**。18 筆既有資料目前僅 Schema-only。
- Rule #6 `main`/`develop` Rulesets 尚未建立，本文與初始 PoC code 只存於獨立分支，需管理員完成分支治理再進行整合。

## 引擎比較矩陣（待實測，不填虛構數據）

| 評估項目 | GraphHopper | Valhalla |
|---|---|---|
| 開源引擎 | Java；官方 profiles / custom models / turn costs | C++；官方 `valhalla-scripted` Docker |
| 可運作容器 | **待建立並鎖定具來源的可重現映像** | 初始實驗 Compose 已準備（映像 digest 待鎖定） |
| 車種合法性 | 標準車種／通用 motorcycle **不等於台灣法規** | `auto` / `motorcycle` 通用 costing **不等於台灣法規** |
| 台灣路段 hard reject | 實作與測試待辦 | 實作與測試待辦 |
| OSM 匯入時間 | NOT RUN | NOT RUN |
| Peak RAM / Disk | NOT RUN | NOT RUN |
| 首次回應 p50/p95 | NOT RUN | NOT RUN |
| 路線幾何與指引 | NOT RUN | NOT RUN |
| CAR/SCOOTER/HEAVY 路線 | NOT LEGALLY VERIFIED | NOT LEGALLY VERIFIED |
| 官方道路限制證據 | G2 待驗證 | G2 待驗證 |
| 最終選型 | Product Owner 決策前不得宣告 | Product Owner 決策前不得宣告 |

官方文件：GraphHopper https://github.com/graphhopper/graphhopper/blob/master/docs/core/profiles.md；Valhalla https://github.com/valhalla/valhalla/blob/master/docker/README.md

## 試驗設計

1. **先確定執行主機**：CPU、RAM、容器平台、儲存空間、能否透過固定區域網路存取。第一輪僅綁 localhost，不接受 Android 對外呼叫。
2. **凍結 OSM 資料**：Geofabrik https://download.geofabrik.de/asia/taiwan.html。兩引擎使用**同一份確切 SHA-256** 的 PBF；若記憶體不夠，改採相同範圍的本地裁切版本，並避免裁斷道路後誤把路線判為違法或不可達。
3. **先驗證普通 API 可用性**：只以公開地標起訖進行 CAR-like `auto` smoke test，記錄連線、回傳 JSON、耗時、路線形狀 SHA；標記 NOT LEGALLY VERIFIED。
4. **新增受管制路段測資**：至少包含 freeway/expressway、國3甲、禁止普通機車匝道、大型重機有例外路段；與 `docs/VEHICLE_RULES.md` 各筆 `VERIFIED / RECHECK_REQUIRED` 狀態連結。
5. **引擎安全層**：實作三車種硬性准入前需具備公路主管機關可追溯的方向、里程、道路 ID 映射；如果映射失敗回報 UNKNOWN/NO_SAFE_ROUTE，禁止普通 profile 默許穿越。
6. **公平量測**：記錄 Engine image digest、dataset SHA、車種 profile/legality policy SHA、硬體資源、import duration、RSS/磁碟、API p50/p95、候選 route geometry hash；每筆測試成功／失敗分開呈現。
7. **Gemini 交叉審查**：評估車種法規映射與測試覆蓋，報告 BLOCKER/HIGH/MEDIUM、保留有證據的測試結果供 PO 選型。
8. **PoC 完成閘門**：Product Owner 選擇 GraphHopper 或 Valhalla 並核准合法性策略後，才能開始路由與 Stage 1 App 連線的下一階段。

## 本次改動項目
- 新增 `routing-poc` 的 sandbox Compose、手動 OSM 下載 SHA256 檢查與固定公用地標 API smoke test。
- 新增本比較規格和一份完全不會上網的 parser 單元測試。
- 不新增真實行車導航、不更動 Stage 1 UI、不建立主分支 PR 合併。

## 本次要測試項目
- [ ] `python -m unittest discover -s routing-poc/tests -p "test_*.py"`。
- [ ] Docker Compose config 可正確解析（須由選定 Docker 主機執行）。
- [ ] Geofabrik OSM PBF 手動下載且 SHA256 記錄完成。
- [ ] Valhalla container 匯入完成、服務正常，固定 fixture API smoke PASS（仍 NOT LEGALLY VERIFIED）。
- [ ] GraphHopper 同資料 PoC 及性能比較（尚未實作）。
- [ ] 三車種官方路段合法性驗證（尚未實作）。
