# Stage 2 — 隔離的 Docker 路由 PoC（尚未通過實機合法性驗證）

本資料夾僅供開發機本機測試 Valhalla 開源路由引擎，並為後續與 GraphHopper 比較蒐集可重現證據。**不會修改 Stage 1 Android APK**、不會提供正式導航／測速／即時道路合法性保證。

## 0. 前提

- Docker Engine / Docker Desktop + Compose V2，可正常拉取 GHCR Images。
- 預留充足磁碟及 RAM 供 OSM 圖檔匯入；實際需求依測試區域和引擎版本量測，不以推估當實測。
- 預設僅將 HTTP 測試 API 綁定主機 `127.0.0.1:8002`，**不要直接向 Internet、家中路由器 WAN 或手機公開該 HTTP API**。
- 先確認 [Issue #6](https://github.com/michaely0418/GarminLikeNavigation/issues/6) main/develop Rulesets。當前 Branch 僅 Draft，未合併。

## 1. 取得 OSM 地圖檔

從專案根目錄執行：

**Windows PowerShell**

```powershell
New-Item -ItemType Directory -Force -Path .\routing-poc\data\valhalla | Out-Null
curl.exe --fail --location --output .\routing-poc\data\valhalla\taiwan-latest.osm.pbf https://download.geofabrik.de/asia/taiwan-latest.osm.pbf
Get-FileHash .\routing-poc\data\valhalla\taiwan-latest.osm.pbf -Algorithm SHA256
```

**Linux / WSL**

```bash
mkdir -p routing-poc/data/valhalla
curl -fL --output routing-poc/data/valhalla/taiwan-latest.osm.pbf https://download.geofabrik.de/asia/taiwan-latest.osm.pbf
sha256sum routing-poc/data/valhalla/taiwan-latest.osm.pbf
```

請把來源 URL、實際下載時間 UTC、SHA-256、檔案大小記到測試紀錄。以 `latest` 檔案反覆匯入不是可重現的實驗；正式比較時必須保留相同快照並使用相同圖檔。

來源：Geofabrik Taiwan https://download.geofabrik.de/asia/taiwan.html （ODbL，遵守來源著作權與 OpenStreetMap 貢獻者署名）。

## 2. 啟動本機 Valhalla PoC

```powershell
Copy-Item .\routing-poc\.env.example .\routing-poc\.env
docker compose --env-file .\routing-poc\.env -f .\routing-poc\compose.yml config
docker compose --env-file .\routing-poc\.env -f .\routing-poc\compose.yml up -d
docker compose --env-file .\routing-poc\.env -f .\routing-poc\compose.yml logs --follow
```

Docker 會讀取 `routing-poc/data/valhalla/` 下的 PBF 來建構 routing tiles；**首次建置可能需要較長時間**。測試 API 尚未啟動前不要把 8002 暫時沒有回應視為路由失敗。若容器 OOM，先減少 `server_threads`，並檢查 Docker Memory 分配，勿直接將資源不足歸咎於道路資料。

### 可重現性注意

`.env.example` 使用上游社群 Docker Image 的 `latest` 做為**第一次拉取的入門值**，它是變動版本，**尚不符合測試報告的 image pin Gate**。

首次成功 pull 後，請查看 `docker image inspect` 的 `RepoDigests`，在自己的 `.env` 以 `...@sha256:...` 固定映像，並記錄引擎實際版本：

```powershell
docker image inspect ghcr.io/nilsnolde/docker-valhalla/valhalla:latest --format '{{json .RepoDigests}}'
```

若相同 PBF 的比較無法確定 Image Digest／Version，必須標示「NOT REPRODUCIBLE」。

## 3. 僅以汽車 Auto Profile 驗證 API 管線

服務就緒後，從開發機本機執行（僅測試範例座標，不代表實際可行駛路線）：

```powershell
curl.exe --get "http://127.0.0.1:8002/route" --data-urlencode 'json={"locations":[{"lat":25.0330,"lon":121.5654},{"lat":25.0375,"lon":121.5637}],"costing":"auto","units":"kilometers"}'
```

驗證回應 HTTP 200、`trip` 與 `legs`、有可解碼的 route shape，且錯誤能被處理；量測冷啟動、warm request latency、RSS / peak RAM、磁碟使用量。**不要把引擎原生 `auto`、`motorcycle` 或 `bicycle` Costing 映射為台灣三車種合法通行**，除非逐路段法規驗證及禁行 hard rejection 確實落實。

## 4. 安全和道路法規 Gates

- 僅本機 PoC；不向手機、公網或反向代理發布未鑑權的路由 API。
- 有涉及 GPS 軌跡／家住地址等資料的測試，不得上傳公共 Repository。使用公開且非個人化的固定測試點。
- 道路 `FORBIDDEN` **直接拒絕**，不得透過 cost penalty 繞過；`UNKNOWN` 在高風險道路預設 fail closed。
- 所有含 `RECHECK_REQUIRED` 路段的輸出，若未補官方通行公告與方向證據，均標示 **UNVERIFIED**。
- G1 Google Places 法遵仍 BLOCKED，不得使用 Google Places 經緯度餵給 MapLibre/Valhalla/GraphHopper。
- GitHub CI 在此階段僅檢驗 Android 原有 Test/Build，不代表 Docker 已被部署、route engine 已通過台灣車種法規驗證。

## 5. 停止並保留測試圖資

```powershell
docker compose --env-file .\routing-poc\.env -f .\routing-poc\compose.yml down
```

`down` 停止容器，bind-mounted `routing-poc/data/` 仍保留，避免每次重跑都要重新下載與建圖。要重置圖資前請先備份 hash 和測試結果。

## 6. 官方／上游參考

- Valhalla 官方：https://github.com/valhalla/valhalla
- 容器範例使用的社群 Valhalla：https://github.com/nilsnolde/docker-valhalla
- GraphHopper 官方部署：https://github.com/graphhopper/graphhopper/blob/master/docs/core/deploy.md
- 臺灣 OSM 快照來源：https://download.geofabrik.de/asia/taiwan.html
- 專案既有決策：[ADR-001](../docs/ADR-001-routing-deployment.md)
- Stage 2 追蹤：[Issue #11](https://github.com/michaely0418/GarminLikeNavigation/issues/11)

### 本次改動項目
- Stage 2 PoC 獨立 `routing-poc/` Compose 範例，預設只對 `127.0.0.1:8002` 提供路由服務。
- 納入 OSM 下載、Hash / Image Digest、API 煙霧測試 SOP，以及法律／授權 Gate。
- 未新增 Android APK 或真實導航功能。

### 本次要測試項目
- [ ] Windows Docker / Compose 可以載入設定並啟動（目前**NOT RUN**）。
- [ ] 正確建立 OSM graph tiles（NOT RUN）。
- [ ] 本機 `/route` API 煙霧測試成功（NOT RUN）。
- [ ] 路由結果可記錄圖資／版本／Hash／延遲（NOT RUN）。
- [ ] `127.0.0.1:8002` 僅對 localhost 開放（NOT RUN）。
- [ ] Gemini Cross Review 與產品負責人決定是否採此 PoC 方案。
