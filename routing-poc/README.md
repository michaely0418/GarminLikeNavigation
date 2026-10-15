# Stage 2 — 台灣路由引擎隔離 PoC（準備版）

> **安全邊界：** 這是 Docker API 可用性、匯入、效能測試環境。它**不是**可以依賴的台灣汽車／機車／大型重機合法導航。未完成逐路段法律驗證，禁止把 PoC 結果呈現在使用者導航 UI 或當成可實際上路的路線。

## 狀態

- `feature/stage2-routing-poc-preflight` 與 `main` / `develop` 隔離。
- Valhalla：已撰寫官方映像檔的本機專用 Compose 範例，**尚未在 Docker 主機執行／驗證**。
- GraphHopper：只做選型比較與接口研究，**尚未建立可重現的影像與 profile 配置**。
- CAR/SCOOTER/HEAVY_MOTORCYCLE：目前都**未具備台灣法規合規的路由實作**。下面的 Valhalla `auto` 僅是 API 冒煙測試。
- OSM PBF 不納入 Git；存於 `routing-poc/data/valhalla`，需手動下載。

## 官方來源

- Valhalla 官方 Docker：https://github.com/valhalla/valhalla/blob/master/docker/README.md
- GraphHopper Profiles：https://github.com/graphhopper/graphhopper/blob/master/docs/core/profiles.md
- Geofabrik 台灣 OSM：https://download.geofabrik.de/asia/taiwan.html
- OSM 授權／署名：https://www.openstreetmap.org/copyright
- 本專案規則：`docs/VEHICLE_RULES.md`、`docs/API_POLICY.md`、`docs/ADR-001-routing-deployment.md`。

**2026 備註：** 舊 `nilsnolde/docker-valhalla` 專案已封存，不應將其舊映像來源當成本 PoC 的長期維護方案；範本使用 upstream `ghcr.io/valhalla/valhalla-scripted`。

## Windows 11 / PowerShell 操作 SOP（先選定 Docker 主機）

前置條件：已安裝 Docker Desktop 或 Docker Engine、Docker Compose v2、PowerShell，並有足夠可用 RAM/磁碟空間匯入當時的台灣 OSM PBF。實際資源需求必須在所選設備量測，不先臆測已通過。

1. 於專案根目錄切換到獨立 PoC 分支（勿覆寫未提交的本地變更）：
   ```powershell
   git fetch origin
   git switch feature/stage2-routing-poc-preflight
   cd routing-poc
   Copy-Item .env.example .env
   ```
2. 從 Geofabrik 下載整份台灣 PBF（資料大小約數百 MB，依實際來源而變）並記錄 SHA-256：
   ```powershell
   .\scripts\download_taiwan_osm.ps1
   ```
   注意 `taiwan-latest.osm.pbf` 會隨來源更新。**比較兩引擎前必須凍結同一份檔案並記錄 SHA-256、時間戳**。不要使用會不一致的兩次 `latest` 下載。
3. 確認 Compose 設定（不啟動）：
   ```powershell
   docker compose -f .\compose.valhalla.yml config
   ```
4. 僅在已選定、可承擔匯入資源的主機上啟動。第一次匯入可能需要長時間和大量磁碟 I/O：
   ```powershell
   docker compose -f .\compose.valhalla.yml up -d
   docker compose -f .\compose.valhalla.yml logs -f --tail=100 valhalla
   ```
   預設埠僅綁定 `127.0.0.1:8002`，不是給手機或外網連線的服務。**不得開 Port Forward 或部署至公網**；未設 TLS / Auth，不能當作正式 App API。
5. 若匯入成功且 API 啟動，在另外一個終端執行：
   ```powershell
   python .\scripts\smoke_valhalla.py
   ```
   此固定資料只要求 Valhalla 回傳一條「臺北車站附近 → 台北 101 附近」的示範 API route，回傳摘要與 encoded shape hash。**即使成功也不代表三車種合法**。
6. 觀察資源與版本：
   ```powershell
   docker stats
   docker image inspect (docker compose -f .\compose.valhalla.yml images -q valhalla)
   ```
   記錄實際映像 digest、OSM PBF SHA-256、Docker/Engine 版本、匯入耗時及 API 延遲，再作兩引擎公平比較。請勿把 `:latest` 視為固定版本。
7. 停止測試（保留圖資快取，下次可重用）：
   ```powershell
   docker compose -f .\compose.valhalla.yml down
   ```

## 不使用 Docker 的預先檢查

```powershell
python -m unittest discover -s .\tests -p "test_*.py" -v
```

僅驗證 smoke script 解析格式與錯誤處理，**不會連網、不會產生合法路線、不代表 Docker 建置 PASS**。

## 版本、授權及限制

- `VALHALLA_IMAGE` 預設 `latest` 僅供第一輪探索；一旦確認可用版本，應改為不變的 `image@sha256:...`，再提交經過交叉驗證的組態。
- PBF OSM / Geofabrik 存在 ODbL 授權及署名義務，後續公開部署與二次資料分發需檢查授權及衍生資料要求。
- 不將 Google Places 座標、任何真實使用者 GPS 歷史或憑證匯入測試路由。
- 不在上線環境使用 Valhalla 內建 `auto` 或 GraphHopper 通用 `motorcycle` profile 冒充台灣法規已驗證的 CAR/HEAVY_MOTORCYCLE/SCOOTER。
- 下一步需確認實際 Docker 主機（Windows Docker Desktop、Linux VM 或 Synology NAS），選定最小測試資料範圍後再執行並記錄結果。
