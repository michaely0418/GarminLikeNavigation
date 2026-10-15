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

## 2026-10-15 DS1621xs+ hardware confirmation / S2-A Resource Baseline

Product Owner supplied DSM System Information screenshot showing:

| Item | DSM-reported value | Verification caveat |
|---|---|---|
| NAS model | Synology DS1621xs+ | Owner screenshot |
| CPU | Intel Xeon D-1527, 4 cores, 2.2 GHz | Owner screenshot |
| Physical RAM | **64 GB** | Owner screenshot; differs from Synology's **published max 32 GB** (2 x 16 GB), so outside manufacturer's published memory validation |
| DSM version | **DSM 7.4.1-90080 Update 1** | Product Owner DSM screenshot dated 2026-10-15 |
| Container Manager version | **24.0.2-1706 (installed / running)** | Product Owner Package Center screenshot, reports latest online version also 24.0.2-1706; this is the **Synology package** version, not independently verified Engine/Compose CLI version |
| Storage Space 1 free capacity | **5.6 TB free / 34.9 TB total; Btrfs; 29.3 TB used** | DSM Storage Manager screenshot; underlying Docker mount directory still needs verification |

Source for official 32GB maximum: [Synology DS1621xs+ Product Specifications](https://global.download.synology.com/download/Document/Hardware/ProductSpec/DiskStation/21-year/DS1621xs%2B/enu/Product_Spec_DS1621xs%2B_enu.pdf). **Do not infer that DSM recognizing 64GB means Synology endorses the upgrade or guarantees long-term stability.**

### Revised experiment configuration (INITIAL LIMITS ONLY)

- Start with a **single frozen full-Taiwan OSM PBF** (no compulsory small-region crop). Both engines receive copies of the same verified SHA-256 input.
- Valhalla NAS Compose: **no hard CPU quota on this DSM kernel** / **12GB memory limit** / 2 Valhalla server threads (not a hard 2-core cap).
- GraphHopper 11.1 on Java 25: **no hard CPU quota on this DSM kernel** / **12GB memory limit** / **8GB maximum JVM heap**.
- Start/stop engines sequentially, reserve NAS capacity for DSM, storage, and other running containers. Track actual memory pressure, OOM restarts, disk growth, and graph import runtime. On trouble, lower throughput, tune caps after measurement or compare the **same** buffered local crop on both.
- Provision ~50GB free disk space **as a provisional planning reserve**, not a guaranteed engine requirement. Verify actual free space and cache growth before continuing.
- Validate performance under the NAS owner's existing workloads before evaluating suitability for sustained routing service.

### Remaining required facts before NAS deployment

- [x] DSM exact version: **7.4.1-90080 Update 1** (DSM screenshot).
- [x] Container Manager package **24.0.2-1706** is installed and running, screenshot confirms same online latest version; Docker Engine and Compose plugin internal versions still not separately queried.
- [ ] Actual DSM free/available memory under normal services (the 64GB capacity alone is not availability).
- [x] Storage Space 1 reports **5.6 TB free** of **34.9 TB total** (Btrfs); initial 50 GB data/index budget has ample storage headroom.
- [ ] Verify that Container Manager project paths actually reside on the intended volume (e.g. `/volume1/docker`), and confirm its own filesystem permissions and quota.
- [x] DSM reports an **APC UPS detected** and a **465 GB read-write SSD cache** on the storage space; these are available features, **not** proof of sufficient UPS battery runtime or a separate 465 GB data volume.
- [ ] Verify UPS battery runtime / graceful shutdown and the existing backup schedule; avoid overlapping NAS backup/indexing jobs with full-Taiwan OSM graph builds.
- [ ] Check Issue #6 `main`/`develop` branch protection before any Draft PR integration.
- [ ] Run real engine import, API queries and route compliance tests separately; currently **NOT RUN**.

### Storage/safety details obtained from DSM screenshot (2026-10-15)

- Storage Space 1 is **Btrfs**, with approximately **34.9 TB total**, **29.3 TB used** and **5.6 TB available**. These are user-interface rounded values, not byte-exact filesystem readings.
- An SSD **read-write cache** of **465 GB** (reported **462.5 GB available** within the cache display) is attached. It is *not* an independent data volume; route graph and PBF must be stored in the proper Btrfs volume/project directory, not inside a cache directory.
- DSM shows an attached **American Power Conversion (APC) UPS**, but runtime and safe shutdown policy are unverified.
- With ample disk and 64 GB recognized RAM, initial **full Taiwan Geofabrik PBF** import is a reasonable test target at **12GB RAM per engine, with no CFS CPU quota**, **sequentially**, while recording actual resource use. This remains an experimental resource allocation.
- **Do not paste or record** identifiers, QuickConnect IDs, Synology account addresses or NAS serial numbers from DSM screenshots in public issues or documentation. Only hardware/OS/volume data needed for testing is recorded.
- Container Manager **package version 24.0.2-1706 verified running**. Available RAM under normal service, actual project path/share permissions, and container Engine/Compose runtime versions remain pending. No NAS container has been deployed by ChatGPT.

### 2026-10-15 Container Manager screenshot acceptance

- Screenshot shows **Container Manager** (package displayed under Docker Inc., publisher Synology Inc.) **已啟動**, **已安裝版本 24.0.2-1706**, **最新線上版本 24.0.2-1706**, and **安裝位置 儲存空間 1**.
- This confirms the Synology package is present on Storage Space 1 and up-to-date as displayed by Package Center. **It does not verify the absolute filesystem path for each future Project, nor the Docker Compose CLI/runtime version or any running Valhalla/GraphHopper container.**
- Next preflight: choose a dedicated writable Shared Folder path on the intended Btrfs volume; review DSM Resource Monitor's *available* physical memory; check Package Center Container Manager Project create UI before importing or running the official Valhalla container.
- Only after verifying the volume path and free NAS memory should the owner stage one frozen Geofabrik Taiwan `.osm.pbf`, record SHA-256, then mount it read/write into `/custom_files` for controlled first-time tile builds.

### 2026-10-15 Resource Monitor and File Station verification

The Product Owner supplied two new DSM screenshots. **Only the following non-identifying facts are recorded:**

| Resource Monitor memory category | DSM-reported value |
|---|---|
| Total physical RAM | 64 GB |
| Used percentage | 54% |
| In use | 34.1 GB |
| Available | **19 GB** |
| Cached | 9 GB |
| Reserved | 1.4 GB |
| Buffered | 453 MB |

**Interpretation:** The physical installed capacity is 64 GB, but the **current available** memory reported by DSM is only **19 GB**, while existing NAS applications/services are using substantial RAM. The cached 9 GB is shown separately; never add it to the 19 GB available without knowing the DSM memory accounting/reclaimability semantics. The existing `mem_limit: 12g` templates set container ceilings, **not preallocated reservations**. If a container grows to its 12GB ceiling, there could be as little as ~7GB of reported available RAM remaining, excluding concurrent changes in other NAS loads. Avoid claiming this is safe for the existing workloads merely because the NAS has 64 GB.

**Immediate safe next actions:**
1. Proceed with **directory and OSM PBF preparation only**. File Station screenshot shows `docker/garmin-routing/` and the two named subfolders `graphhopper/`, `valhalla/`; it does **not** verify the `data/` subfolders, the absolute `/volumeN` path, the POSIX filesystem permissions or a running container.
2. Before running graph build, verify the intended `docker` share and physical volume, check current Resource Monitor available memory again, and avoid other peak NAS jobs. As a conservative target, schedule import when DSM shows **at least ~24GB available** if keeping the 12GB container cap, allowing approximately 12GB remaining headroom at peak. This is a **local operational precaution**, not a formal minimum RAM guarantee; other memory consumption can fluctuate.
3. If peak memory approaches the cap or the NAS's available RAM becomes uncomfortably low, stop import and log peak RSS, OOM events and NAS service impact. Tuning the cap or using an identical smaller OSM crop for both engines is allowed for PoC, but never run both index builders concurrently.
4. Next factual confirmation: DSM Control Panel → Shared Folder (actual `docker` share on target volume); check `valhalla/data` and `graphhopper/data` exist and are writable; download a single public Geofabrik Taiwan PBF and record the source/date/SHA-256 before copying it into both local directories.
5. Continue to keep GitHub PR #13 Draft and Stage 2 backend unconnected from the mobile App. No live OSM graph import or route legality verification has occurred.

**Privacy:** File Station screenshot does not verify an absolute host path. Avoid copying device identifiers, account names or LAN addresses into public source or PR comments.

### 2026-10-15 Geofabrik Taiwan OSM PBF staging — three-way checksum PASS (owner PowerShell evidence)

The Product Owner provided a Windows PowerShell output showing successful file comparison using `Get-FileHash -Algorithm SHA256`:

| Local / NAS target | Size (bytes) | Hash comparison |
|---|---:|---|
| Local downloaded `taiwan.osm.pbf` | **327581009** (~312.41 MiB) | reference SHA-256 |
| Valhalla `data/taiwan.osm.pbf` on NAS | **327581009** | `Local = Valhalla: True` |
| GraphHopper `data/taiwan.osm.pbf` on NAS | **327581009** | `Local = GraphHopper: True` |

**`SameDataset: True` → PBF three-way byte equality PASS.**

- PBF SHA-256: `259876A0E2C56B4102636A035CB073616FFAFEA6342A494B53F027C023E8FB82` (previous visible local Get-FileHash output).
- This confirms the same **fixed PBF snapshot** is now staged for both engines; no need to download again. Keep the PBF unchanged during initial benchmarking. The hash is a data-integrity identifier, not proof of official map correctness or Taiwan road legality.
- The NAS project root is accessed from Windows as a `docker/garmin-routing` SMB share, but **the underlying NAS Unix/volume path remains unverified**; before entering a path in Container Manager, confirm DSM Control Panel → Shared Folder storage location or equivalent NAS CLI path.
- Source site: [Geofabrik Taiwan Extract](https://download.geofabrik.de/asia/taiwan.html). Exact source snapshot date/time cannot be established solely from hash/size screenshot; if known locally record it in the private experiment notes, do not invent a timestamp.
- **Readiness:** PBF staging completed; Synology `garmin-valhalla-poc` has not been created or started. Initial memory available previously reported **19GB**, below suggested cautious target **24GB** for a container with a 12GB memory ceiling. Before actual full-country graph build, confirm NAS memory and choose an appropriately low-load window. No Android Stage 1 changes.

### 2026-10-15 Synology container creation error — NanoCPUs gate

- The user created Container Manager Project `garmin-valhalla-poc` under the now-visible host path **`/volume1/docker/garmin-routing/valhalla`**. Image pull/extraction and network creation succeeded, but Docker **container creation** failed with visible error prefix `NanoCPUs can not be set, as your kernel does not support CPU CFS...`; **zero containers** shown, no graph build yet.
- Cause: old NAS Compose included `cpus: 2.0`. Docker maps this to CFS-based NanoCPUs, unsupported on the current DSM host kernel/cgroup configuration. This does **not** indicate corrupted or incompatible Taiwan PBF input.
- Both Synology engine templates now **omit `cpus`**, retaining the 12GB memory limit. Valhalla `server_threads=2` is an application worker count, **not** a host CPU hard cap. During graph build observe actual NAS CPU resource pressure; if necessary pause other workloads or use an identical smaller PBF region for both engines. Do not replace `cpus` with another CFS quota field.
- The repository preflight validator and 3 new offline tests reject the unsupported CPU quota forms. Existing project must be updated via **Project → Details → YAML Configurations** and redeployed; never delete the user's verified PBF folder. Before triggering the expensive first import check up-to-date DSM available RAM and use a low-load maintenance window.
- **Real NAS recovery / actual importer success remain PENDING**, notwithstanding CI green.

### 2026-10-15 Valhalla — first NAS-local route smoke PASS

Product Owner provided a screenshot of a real `curl -sS ... POST http://127.0.0.1:8002/route` executed **in the Synology NAS Linux shell**, then parsed the resulting JSON with `python3`. This confirms one actual `auto` route, **not legal vehicle navigation**:

| Evidence field | Observed result |
|---|---|
| NAS-local `/route` HTTP result | **HTTP 200** |
| Static public endpoints | Taipei Main Station vicinity (25.0478,121.5170) → Taipei 101 vicinity (25.0338,121.5645) |
| `trip.summary.length` | **6.239 km** |
| `trip.summary.time` | **939.77 seconds** (~15m 40s) |
| `trip.legs` count | **1** |
| `trip.legs[0].shape` present | **True** |
| Route legality verified | **FALSE / NOT PERFORMED** |
| Through-Windows SSH tunnel HTTP | **NOT AVAILABLE**: server refused `direct-tcpip` (`administratively prohibited`) |

**Milestone status:** Official Valhalla Docker container started on DSM; OSM graph generated and tiles loaded (261 tiles reported by the same build); NAS-local `/status` returned HTTP 200; actual `/route` returned route length/time/encoded geometry. Mark **VALHALLA_LOCAL_ROUTE_SMOKE = PASS**.

**Still NOT RUN:** repeated request latency p50/p95, cold/warm response comparison, graph-cache size, peak CPU/RAM/SSD I/O, GraphHopper on identical SHA-256 OSM, alternate routes and direction, legal-road hard rejects for scooter/heavy motorcycle. Do not promote the result to a navigation-ready environment or connect Stage 1 Android HUD. The route ETA is a model estimate without measured traffic.

**Next gate:** measure NAS-local Valhalla warm-up and at least 20 repeat latencies with the same non-personal public endpoints; record p50/p95, SHA-256 `259876A0E2C56B4102636A035CB073616FFAFEA6342A494B53F027C023E8FB82`, and engine build version `3.9.1-d558ba3db`; review DSM live CPU/RAM and graph directory usage. Only afterward evaluate the GraphHopper 11.1 comparator sequentially. Avoid changing SSH policy or exposing host ports merely to benchmark.
