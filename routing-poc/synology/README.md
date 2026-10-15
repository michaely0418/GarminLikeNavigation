# Stage 2 S2-A0 — Synology NAS / Container Manager PoC SOP

**STATUS: CONFIGURATION ONLY / DEPLOYMENT NOT RUN / TAIWAN ROAD LAW NOT VERIFIED.**

Target: Synology DS1621xs+ (Intel Xeon D-1527, 4 cores at 2.2 GHz, linux/amd64). **The Product Owner provided a 2026-10-15 DSM screenshot reporting `實體記憶體大小 64 GB`**. This is owner-provided observed RAM, **not** the official supported maximum. Synology's published DS1621xs+ memory limit is **32 GB (2 x 16 GB)**: a recognized 64 GB upgrade is outside that published validation limit, and does not guarantee long-running reliability or Synology warranty/support. Confirm the current DSM/Container Manager version, available RAM under NAS load and storage free space before import.

References:
- Synology Container Manager > Project: https://kb.synology.com/en-global/DSM/help/ContainerManager/docker_project
- Synology DS1621xs+ hardware: https://global.download.synology.com/download/Document/Hardware/ProductSpec/DiskStation/21-year/DS1621xs%2B/cht/Product_Spec_DS1621xs%2B_cht.pdf
- Official Valhalla: https://github.com/valhalla/valhalla/blob/master/docker/README.md
- Official GraphHopper: https://github.com/graphhopper/graphhopper/blob/master/README.md
- Geofabrik Taiwan: https://download.geofabrik.de/asia/taiwan.html
- Road-law specification: `docs/VEHICLE_RULES.md`; Stage 2 governance: `docs/API_POLICY.md`.

## 1. NAS prerequisites — 2026-10-15 DSM screenshot results

**User-confirmed:** DS1621xs+, Xeon D-1527 (4 cores / 2.2 GHz), physical RAM **64 GB**, DSM **7.4.1-90080 Update 1**. DSM Storage Manager reports **Storage Space 1: Btrfs, 34.9 TB total / 29.3 TB used / 5.6 TB available**. A 465 GB **read-write SSD cache** is present. An **APC UPS** is detected; actual battery runtime and UPS shutdown configuration are unverified.

- **Container Manager confirmed installed and running:** Package Center screenshot reports **24.0.2-1706**, with latest online **24.0.2-1706**, installed on **Storage Space 1**. The package version is not a verified Docker Engine or Docker Compose binary version. **Still needed:** NAS available RAM under normal workload (Resource Monitor), actual project volume/share path and writable permissions.
- **Initial disk planning:** reserve around **50 GB** for the raw input, two graph indexes and logs (planning estimate, not a measured requirement). The DSM screenshot's **5.6 TB free** is ample for this reserve. Do not infer that `儲存空間 1` means files are mounted at `/volume1`; verify the intended volume/project Shared Folder path before running.
- **SSD cache is not a storage volume:** do not configure a graph/PBF bind mount using the cache capacity; use normal NAS Btrfs Shared Folder storage. Cache write amplification, graph build I/O and snapshots must be monitored.
- **UPS presence is not runtime assurance:** confirm APC UPS shutdown behavior and battery runtime if running a long initial graph import.
- **Privacy:** DSM screenshots may display NAS account, QuickConnect ID and serial number. Do not copy those into a public GitHub Issue, log, SOP or screenshot shared externally.

### GUI preflight checklist

1. **Package Center → Installed → Container Manager**: **PASS as reported by 2026-10-15 screenshot** (package 24.0.2-1706, 已啟動, installed on Storage Space 1). Do not equate this package number with Docker Engine/Compose CLI versions; verify runtime compatibility during Project configuration.
2. **Resource Monitor → Memory / CPU**: capture actual available RAM and NAS load while normal containers and services are running; 64 GB physical RAM does not equal 64 GB free.
3. **Storage Manager → Storage / Volumes** and **Control Panel → Shared Folder**: locate the intended shared folder `docker` and its actual volume. If it does not exist, create a separate NAS Shared Folder for `garmin-routing`, rather than modifying existing application directories.
4. **Container Manager → Project**: confirm it can create Compose projects, and do not click Build/Start until the local OSM data and correct volume path are ready.
5. **UPS & backup window**: avoid overlapping full-Taiwan import with backup, rebuild, scrubbing, snapshots or other high-I/O tasks.
3. **RAM planning for observed 64 GB (estimate, not an engine hard requirement):**
   - First attempt: **full Taiwan PBF**, using the **same exact input SHA-256** on both engines; no mandatory Taipei/New Taipei cropping solely due to RAM.
   - **Valhalla:** `server_threads=2`, `mem_limit: 12g`; **no Docker hard CPU cap** because DSM kernel lacks working CFS quota support.
   - **GraphHopper:** `mem_limit: 12g`, JVM `-Xms1g -Xmx8g`; **no Docker hard CPU cap**. Java heap is only part of process/container memory; file caches, JVM native memory and indexing overhead also matter.
   - Run **one engine at a time** for fair comparison and to protect DSM services. Treat the other ~52 GB as shared OS/NAS headroom, **not** guaranteed freely available (current apps and cache still consume it).
   - If either importer reaches its 12 GB memory limit, **stop and record OOM / peak resources**; consider a controlled increase after checking actual DSM memory pressure, or fall back to the **same** buffered, cropped PBF for both engines. Do not increase blindly.
   - Initial storage **planning reserve ~50 GB**, verify free capacity and actual graph sizes. Downloaded PBF size does not predict index/cache size.
   - Since 64 GB exceeds Synology's **official 32 GB** limit, watch for ECC/memory warnings and NAS stability; do not claim this hardware configuration is manufacturer-certified.
4. Do not expose test API through DDNS, Cloudflare Tunnel, reverse proxy, router forwarding, DSM portal, or a NAS LAN/public host-port binding. Compose port bindings **`127.0.0.1` only**. No TLS or auth is configured.
5. Obtain/update the image digest after initial pull and lock it when making a reproducible comparison. `latest` / `25-jre` are exploratory placeholders, not immutable versions.

## 2. File Station folder layout

Create these **separate Container Manager Project folders** in an existing Shared Folder named `docker` (adjust if installed on another volume):

```text
/volume1/docker/garmin-routing/
  valhalla/
    docker-compose.yml            <- from routing-poc/synology/compose.valhalla.yml
    data/
      taiwan.osm.pbf              <- original *same* Geofabrik snapshot
      [Valhalla generated indexes/tiles after import]
  graphhopper/
    docker-compose.yml            <- from routing-poc/synology/compose.graphhopper.yml
    graphhopper-config.yml        <- from routing-poc/synology/graphhopper-config.yml
    data/
      taiwan.osm.pbf              <- EXACT SAME SHA256 as Valhalla input
      graphhopper-web-11.1.jar    <- upstream official release
      logs/                      <- writable directory if JVM log appender needs it
      [GraphHopper graph-cache/ after import]
```

`data/` is mounted as a host bind directory, so it persists when a **container** is stopped or recreated. Still confirm target folders before using Container Manager **Delete/Clean** or deleting files; do not assume project deletion preserves NAS data.

Do **not** upload PBF, extracted graph cache, binaries, real GPS history or credentials to public GitHub.

## 3. Prepare identical OSM input

Download one frozen snapshot from Geofabrik (`https://download.geofabrik.de/asia/taiwan-latest.osm.pbf`) to Windows or NAS and record download UTC time and SHA-256. `latest` changes over time; upload a **copy of the exact same file** to each project as `data/taiwan.osm.pbf`.

Example PowerShell on a Windows PC with a NAS SMB share named `docker`:

```powershell
$Pbf = 'C:\Temp\taiwan.osm.pbf'
Invoke-WebRequest 'https://download.geofabrik.de/asia/taiwan-latest.osm.pbf' -OutFile $Pbf
Get-FileHash -Algorithm SHA256 $Pbf

$NasShare = '\\NAS_IP\docker\garmin-routing'
Copy-Item $Pbf "$NasShare\valhalla\data\taiwan.osm.pbf"
Copy-Item $Pbf "$NasShare\graphhopper\data\taiwan.osm.pbf"
Get-FileHash -Algorithm SHA256 "$NasShare\valhalla\data\taiwan.osm.pbf"
Get-FileHash -Algorithm SHA256 "$NasShare\graphhopper\data\taiwan.osm.pbf"
```

Replace `NAS_IP` with the real NAS address. Do **not** proceed to comparative timing until all three SHA-256 digests match. Full-Taiwan Geofabrik PBF is roughly hundreds of MB compressed; graph-cache can consume considerably more. For 8GB memory, use a **single identically cropped/buffered extract** for both engines instead; cropping near a road boundary affects reachability and is unsuitable for legal routing conclusions.

## 4. Synology Container Manager — create Valhalla Project

1. Using File Station, put `compose.valhalla.yml` into `/volume1/docker/garmin-routing/valhalla/docker-compose.yml`; create `data` and upload `taiwan.osm.pbf`.
2. Container Manager → **專案 / Project** → **新增 / Create**.
3. Project Name: `garmin-valhalla-poc`; Project Path: `/volume1/docker/garmin-routing/valhalla`. Choose use existing/upload compose YAML for `docker-compose.yml`, or paste the checked template.
4. Verify port mapping `127.0.0.1:8002:8002`, data bind mount `./data:/custom_files`, `platform=linux/amd64`, **no Docker CFS CPU quota** (actual kernel does not support NanoCPUs) and an initial 12GB memory cap (based on the owner's 64GB DSM report, not a measured consumption figure). **Do not enable Web Station web portal.**
5. Start only Valhalla. First startup will import the local PBF and produce tiles; monitor Project → Container → Log and Project → Statistics. Initial import can be CPU/RAM/I/O intensive; allow time and stop for OOM, DSM pressure, storage exhaustion or repeated restarts.
6. When ready, only test its endpoint from the NAS local loopback or through the SSH tunnel below. A generic `auto` route proves API availability only.

## 5. Synology Container Manager — create GraphHopper Project

1. Stop Valhalla first, to avoid two simultaneous graph imports.
2. In the graphhopper project directory put:
   - `compose.graphhopper.yml` renamed to `docker-compose.yml`.
   - `graphhopper-config.yml` (derived from upstream `11.x/config-example.yml`, application server **bind_host: 0.0.0.0** internally; Admin remains bound to localhost inside container).
   - The exact same SHA-256 `data/taiwan.osm.pbf`.
   - Official GraphHopper 11.1 JAR: `https://repo1.maven.org/maven2/com/graphhopper/graphhopper-web/11.1/graphhopper-web-11.1.jar` → `data/graphhopper-web-11.1.jar`.
   - Ensure `data/logs` can be written if log file appender is enabled.
3. Create Container Manager Project `garmin-graphhopper-poc`, path `/volume1/docker/garmin-routing/graphhopper`.
4. Compose uses Eclipse Temurin **Java 25 JRE** + official GraphHopper JAR, generic `car` profile, **no Docker CPU CFS cap**, total container RAM cap 12GB and JVM `-Xmx8g`. Confirm current installed NAS RAM supports this allocation. GraphHopper upstream's sample config binds to localhost by default; using the repository-provided modified config is **necessary inside Docker** to serve port 8989 through its host-loopback bind.
5. Start only GraphHopper. Monitor graph import/build and Project logs. Ensure `/data/graph-cache` is generated. Do not infer legal CAR / SCOOTER / HEAVY_MOTORCYCLE compliance from the demo `car` profile.

## 6. Windows 11 → NAS local-only SSH tunnel for safe API access

DSM → Control Panel → Terminal & SNMP → temporarily enable SSH, ensure NAS login and SSH port are correct. **Do not forward the SSH port or expose the route APIs to the Internet.**

With Valhalla active, open one PowerShell window, keeping it open:

```powershell
ssh -N -L 8002:127.0.0.1:8002 -p 22 <DSM_USER>@<NAS_IP>
```

Then in **another** PowerShell window from the project checkout:

```powershell
python routing-poc/scripts/smoke_valhalla.py --base-url http://127.0.0.1:8002
```

For GraphHopper when it alone is running:

```powershell
ssh -N -L 8989:127.0.0.1:8989 -p 22 <DSM_USER>@<NAS_IP>
```

The generic GraphHopper endpoint is `http://127.0.0.1:8989/route?point=25.0478,121.5170&point=25.0338,121.5645&profile=car&points_encoded=false` when accessed **on the Windows SSH tunnel**. Its result is NOT a lawful Taiwan navigation instruction. Disable DSM SSH when done unless you have a separate reason to keep it.

If remote tunnel access does not work, first verify service listening inside the NAS and check SSH and service logs; do not solve it by changing `127.0.0.1` to `0.0.0.0` on the host.

## 7. Repeatable fair performance comparison

Do one engine at a time on the **same NAS**, same frozen dataset SHA256, roughly equal memory caps, sequential execution, and no other heavy NAS job (the DSM kernel does **not** permit Compose `cpus`, so no hard two-core limit is assumed). Record actual JVM heap / cache/profile differences so the results are not misrepresented as identical workloads.

1. Record engine JAR version or image ID/digest, dataset SHA-256, NAS physical RAM, DSM/Container Manager version, CPU, free space, build start/end UTC, peak memory, graphs size, OOM flag and relevant logs.
2. Build/rebuild cache once per engine, separately from route latency.
3. Warm-up `3` calls; take `20` timed queries on two fixed public landmark fixtures (station → 101, and reverse). This is a **low sample exploratory comparison**, not a production SLA.
4. Use the Python script with the Windows SSH tunnel active:

```powershell
$Hash = '<ACTUAL_64_CHARACTER_OSM_SHA256>'
$Vimage = '<ACTUAL_VALHALLA_IMAGE_DIGEST>'
python routing-poc/scripts/benchmark_poc.py --engine valhalla --base-url http://127.0.0.1:8002 --dataset-sha256 $Hash --engine-image $Vimage --output valhalla-result.json

# Stop Valhalla, start GraphHopper, open tunnel to 8989, then:
$Gimage = '<ACTUAL_TEMURIN_IMAGE_DIGEST_AND_GH_JAR_VERSION>'
python routing-poc/scripts/benchmark_poc.py --engine graphhopper --base-url http://127.0.0.1:8989 --dataset-sha256 $Hash --engine-image $Gimage --output graphhopper-result.json
```

Results contain p50/p95 **observed end-to-end request latency**, distance (km), route duration (s), geometry hash, dataset and engine identity. The two response formats are normalized, **but differing car cost models and graph construction remain non-equivalent**. Use findings only to choose an engine for a later Taiwan law-aware adapter.

## 8. Engine decision matrix / gates

| Dimension | Valhalla official | GraphHopper |
|---|---|---|
| Software source | Maintained upstream scripted container | Upstream JAR 11.1 on official Java runtime; no official upstream GraphHopper Docker image |
| Container | Synology Compose, localhost:8002 | Synology Compose, localhost:8989 |
| OSM inputs | frozen SHA256 PBF | **same** SHA256 PBF |
| Import time, RAM, disk | MEASURE, not yet run | MEASURE, not yet run |
| p50/p95 for public fixtures | MEASURE, not yet run | MEASURE, not yet run |
| Geometry & route instructions | Smoke check planned | Smoke check planned |
| Scooter lawful segments | NOT VERIFIED | NOT VERIFIED |
| Heavy motorcycle lawful segments | NOT VERIFIED | NOT VERIFIED |
| Specific road law restrictions | MUST be hard reject, official evidence | MUST be hard reject, official evidence |
| Unknown high-risk segments | FAIL CLOSED | FAIL CLOSED |

**Before any Android App integration:** G1 Google Places still BLOCKED; G2 Taiwan road regulations still IN PROGRESS. No actual vehicle routing data/UI exposure to end users. Benchmark/schema checks do **not** certify allowed roads.

### 2026-10-15 64GB RAM update
- DSM owner screenshot: `DS1621xs+`, `Xeon D-1527`, `4 cores`, `64 GB` physical memory.
- **Official Synology capacity is only 32GB:** https://global.download.synology.com/download/Document/Hardware/ProductSpec/DiskStation/21-year/DS1621xs%2B/enu/Product_Spec_DS1621xs%2B_enu.pdf . Hardware-compatibility caveat applies.
- Revised initial import target: **full Taiwan OSM**; Valhalla and GraphHopper each get a **12GB** container memory ceiling, GraphHopper 8GB JVM max heap, **no CFS CPU quota**, executed **sequentially**.
- Still unknown: DSM version, Container Manager installed version, current system free memory under load, storage volume, free disk space, and actual OSM import statistics.

### 本次改動項目
- Synology-specific Docker Compose for two engines, source-configured GraphHopper, DSM Project SOP, Windows SSH tunnels, identical PBF and reproducible metrics script.

### 本次要測試項目
- [ ] GitHub CI offline Python unit tests and all Compose configuration syntax checks.
- [ ] User reports real NAS RAM, DSM, Container Manager version and storage capacity.
- [ ] User verifies both engines locally import the same PBF, response parsers work, and metrics are recorded.
- [ ] Gemini cross-reviews templates before merging Draft PR #13.
- [ ] GitHub Issue #6 Rulesets applied and tested before merge.

## Verified DSM data vs outstanding prerequisite (2026-10-15)

| Check | Status |
|---|---|
| DS1621xs+ + 64 GB recognized memory + Xeon D-1527 | **CONFIRMED by owner DSM screenshot** |
| DSM 7.4.1-90080 Update 1 | **CONFIRMED by owner DSM screenshot** |
| Storage Space 1 Btrfs / 34.9 TB total / 5.6 TB free | **CONFIRMED by owner screenshot** |
| SSD read-write cache and APC UPS detected | **CONFIRMED presence**, cache capacity is NOT graph storage; UPS runtime remains unknown |
| Container Manager installed version | **CONFIRMED: 24.0.2-1706**, running (Package Center screenshot); Docker Engine/Compose CLI exact versions remain unverified |
| Available NAS memory under real workload | **NOT YET PROVIDED** |
| Actual target project Shared Folder / mount directory | **NOT YET VERIFIED** |
| Native on-NAS Docker run / OSM import / API benchmark | **NOT RUN** |

The templates remain **PoC-only** and limited to local NAS loopback; avoid exposing the endpoint through LAN forwarding, reverse proxies, DDNS or Cloudflare Tunnel until service authentication and TLS are designed. Taiwan legal vehicle rules and Google Places license gating remain unchanged.

## 2026-10-15 Container Manager preflight — current step-by-step action

**Screenshot accepted:** Container Manager `24.0.2-1706` is **running** and the package is installed on **Storage Space 1**, with the same version shown as the online latest. This does not prove a Docker Project has been created.

### Next low-risk DSM GUI steps (no containers started)

1. Open **Resource Monitor → Memory** with existing normal workloads still running. Record **Available / Used** RAM and CPU load before allocating the 12GB cap. Avoid including personal host details in published screenshots.
2. Open **Control Panel → Shared Folder** (or File Station) and identify the share already hosting your other Container Manager Projects. Check which Volume it resides on and permissions. **Do not change ownership/permissions of existing app directories**.
3. If the intended Shared Folder is `docker` and is on `volume1`, create ONLY these NEW subfolders via File Station:

   ```text
   docker/garmin-routing/valhalla/data/
   docker/garmin-routing/graphhopper/data/
   ```

   If the NAS uses a different shared folder/volume, use its real path instead; `/volume1/docker` in this guide is an **example**, not a verified current location. Don't create a new DSM shared folder simply because its name is in this example without checking what already exists.
4. **Do not start or expose services yet.** At the next gate, download a single Taiwan Geofabrik snapshot, verify SHA-256 and copy the identical PBF to each `data` directory. Configure the Valhalla Project in Synology Container Manager only when real path and NAS available memory are established.
5. GUI Project path is a **filesystem location** under the selected share; the app's `安裝位置：儲存空間 1` does **not** imply every container project is automatically at `/volume1/docker`.

After Resource Monitor/Shared Folder path confirmation, proceed with sections 3 and 4 to upload the PBF and create **Valhalla first**. The GraphHopper comparison remains deferred until Valhalla startup, graphs and smoke response are observed.

## 2026-10-15 — Physical NAS memory and folder gate (owner GUI evidence)

The owner provided DSM **Resource Monitor** and **File Station** screenshots:

- Total 64 GB; usage **54%**; **34.1 GB in use**, **19 GB available**, **9 GB cache**, **1.4 GB reserved**, **453 MB buffered**. These are separately labeled UI categories; **do not double-count cache as guaranteed free memory**.
- File Station shows the path **`docker > garmin-routing`**, with child folders **`graphhopper`** and **`valhalla`**. The screenshots **do not** establish the Unix absolute path, the volume number, that `data` exists within each, or that the user has actually staged an OSM PBF.
- At current **19 GB available**, the original **12 GB per-container memory cap** would leave around 7 GB reported available if the engine reached the cap and other workloads remained unchanged. A conservative operational *planning target* is to run the first full-Taiwan build during a low-load window with around **24GB or more available** RAM before starting, while keeping a close eye on NAS memory/SWAP/OOM. This is not a software minimum requirement or guaranteed headroom; if the NAS's operating memory trends down, STOP and adjust scope/resources.
- Never start GraphHopper and Valhalla at the same time for the first import; neither container is deployed yet.

### Immediate step 1: Ensure only new PoC folders exist

Use File Station to create folders beneath the **existing** `docker/garmin-routing`:

```text
docker/garmin-routing/
  valhalla/
    data/               <- must exist and have sufficient write permission
  graphhopper/
    data/               <- must exist and have sufficient write permission
```

No existing container or directory should be renamed, removed or have its permissions recursively changed. Control Panel → Shared Folder or NAS shell can verify the actual root (do not assume `/volume1/docker` until checked).

### Immediate step 2: Stage identical Geofabrik PBF without launching an engine

On Windows PowerShell after confirming the NAS SMB share `\\NAS_IP\docker` exists and is accessible:

```powershell
$Pbf = Join-Path $env:USERPROFILE 'Downloads\taiwan.osm.pbf'
$Source = 'https://download.geofabrik.de/asia/taiwan-latest.osm.pbf'
Invoke-WebRequest -Uri $Source -OutFile $Pbf
$Expected = (Get-FileHash -Algorithm SHA256 $Pbf).Hash
$NasRoot = '\\NAS_IP\docker\garmin-routing'
Copy-Item -LiteralPath $Pbf -Destination (Join-Path $NasRoot 'valhalla\data\taiwan.osm.pbf')
Copy-Item -LiteralPath $Pbf -Destination (Join-Path $NasRoot 'graphhopper\data\taiwan.osm.pbf')
$Val = (Get-FileHash -Algorithm SHA256 (Join-Path $NasRoot 'valhalla\data\taiwan.osm.pbf')).Hash
$Gh = (Get-FileHash -Algorithm SHA256 (Join-Path $NasRoot 'graphhopper\data\taiwan.osm.pbf')).Hash
[pscustomobject]@{
  LocalHash = $Expected
  ValhallaHash = $Val
  GraphHopperHash = $Gh
  SameDataset = ($Expected -eq $Val -and $Val -eq $Gh)
}
```

Replace `NAS_IP` with the real NAS host/LAN IP on **the local PC only**; do not commit it into the public project. Record date/time, PBF SHA-256 and Geofabrik source; the source's `latest` can change. Do not accidentally save under `docker/garmin-routing` without each engine's `data` subfolder. **Do not start Container Manager Projects until SameDataset=True**, real host volume path is checked, and memory pressure is acceptable.

### Immediate step 3: Report preflight evidence

Return just: verified Container Manager Project path / Shared Folder target, PBF file size and SHA-256 result, whether both `data` folders are present, and the latest Resource Monitor's **available** RAM immediately prior to import. Then create **Valhalla first** with the local-host-only Compose file; GraphHopper remains stopped. The Demo routes, speed limits, Taiwan motorcycle road access and Google Places are **not** verified by this PoC.

## 2026-10-15 — Full-Taiwan dataset file-integrity gate PASSED

Owner PowerShell evidence now confirms:

```text
Local = Valhalla: True
Local = GraphHopper: True
SameDataset: True
Local PBF bytes: 327581009
Valhalla PBF bytes: 327581009
GraphHopper PBF bytes: 327581009
SHA-256: 259876A0E2C56B4102636A035CB073616FFAFEA6342A494B53F027C023E8FB82
```

This is an **OSM dataset equality PASS**, not proof of Valhalla tile-build success or any legal route. No need to re-download the map for the first benchmark; preserve these same two NAS files.

### Next DSM GUI gate: configure Valhalla before starting import

1. **DSM 控制台 → 共用資料夾 → docker**: verify the `docker` Shared Folder's actual volume. The Windows SMB name and File Station breadcrumb do **not** establish the NAS's absolute Linux path. Confirm whether the real path is `/volume1/docker/garmin-routing/valhalla` or another mount.
2. **File Station → docker → garmin-routing → valhalla**: verify both **`data/taiwan.osm.pbf`** and (later) **`docker-compose.yml`**; do not alter the now-verified PBF.
3. Download [the reviewed Valhalla Compose template](compose.valhalla.yml) as a text file and rename it `docker-compose.yml` in that **valhalla** Project folder; the **relative volume** `./data:/custom_files` expects the Compose Project context there. Do not save YAML inside `data`.
4. **Container Manager → 專案 → 新增**: `garmin-valhalla-poc` as project name; the **project path** is the verified *absolute* NAS folder. Choose the **existing compose file / upload YAML** corresponding to `valhalla/docker-compose.yml`, as offered by the installed UI.
5. Review before deployment: image `ghcr.io/valhalla/valhalla-scripted:latest`, container internal port `8002` published on **NAS localhost only**, `./data:/custom_files`, **no `cpus` / CFS quota** (DSM rejects NanoCPUs), `mem_limit: 12g`, and **`restart: "no"`**. Avoid DSM Web Portal or any router/firewall port forward.
6. **Do not click Build/Start yet** if Resource Monitor still reports only ~19GB available and the normal NAS workloads cannot be safely scheduled. Prefer a lower-load time with ~24GB available (operational caution, not engine minimum) and confirm no heavy backup/index job. When approved to execute, start *only* Valhalla; monitor DSM remaining available memory, swap/OOM, filesystem growth and Project logs during graph import.
7. The first run uses `:latest` for exploratory validation; pin an immutable digest after confirming a working image. A container running successfully is **not** evidence of lawful vehicle routing; the route API remains isolated via `127.0.0.1`.

**Before first actual import, ask the Product Owner to confirm the DSM Project path and fresh RAM available value**. Leave GraphHopper Project stopped until Valhalla import and the smoke check are complete.

## 2026-10-15 — INSTALL BLOCKER: Synology NanoCPUs / CPU CFS unsupported (fixed in templates, device retest PENDING)

The owner's Container Manager screenshot clearly shows:

- Project **`garmin-valhalla-poc`** exists with host project path **`/volume1/docker/garmin-routing/valhalla`**, status red and **0 running containers**.
- The official Valhalla Docker image was downloaded and extracted successfully; an isolated Compose network was created, then **container creation failed** with a daemon error beginning **`NanoCPUs can not be set, as your kernel does not support CPU CFS...`**. The tail of the on-screen log is horizontally truncated, so do not assume more words than visible.
- **Root cause:** the *previous* Synology Compose template used `cpus: 2.0`. Docker implements fractional CPU quotas with the Linux CFS bandwidth controller. This DSM kernel environment cannot apply the quota; syntax-only CI did not catch the host incompatibility. The OSM PBF does not cause this failure; PBF import **has not begun**.
- **Fix:** remove `cpus: 2.0` in *both* Synology Valhalla and GraphHopper Compose files. Do **not** replace it with `cpu_quota`, `cpu_period` or `deploy.resources.limits.cpus`: these can trigger the same kernel capability limitation. Keep `mem_limit: 12g`; for Valhalla `server_threads: '2'` reduces the worker count but **does not impose a hard global two-core CPU ceiling**. Monitor CPU use in DSM while importing. GraphHopper also has no hard CPU quota in this version.
- **Preflight CI hardening:** Python `verify_synology_compose.py` now rejects CFS CPU quota settings even though `docker compose config` accepts them; additional unit tests cover `cpus`, `cpu_quota`, `deploy.resources.limits.cpus`. CI remains a syntax/policy check, **not** real NAS compatibility confirmation.

### Recovery SOP — existing Project, without deleting the PBF

1. In File Station, verify `/volume1/docker/garmin-routing/valhalla/data/taiwan.osm.pbf` is still present. **Do not delete or rename the `data/` folder**; the PBF checksum gate already passed.
2. Container Manager → **專案 (Project)** → select **`garmin-valhalla-poc`** → **詳細資訊 (Details)** → **YAML 設定 (YAML Configurations)**. This UI can edit the Project Compose YAML and deploy it with the new settings, per [Synology Project documentation](https://kb.synology.com/en-global/DSM/help/ContainerManager/docker_project). If the GUI exposes a different menu order, locate the project YAML editor, **not** a container runtime resource setting.
3. Remove the line **`cpus: 2.0`** and ensure it does not appear under `deploy.resources.limits`. Easiest: replace the Project Compose content with the updated [`compose.valhalla.yml`](compose.valhalla.yml) in this PR. Preserve `127.0.0.1:8002:8002`, `./data:/custom_files`, `mem_limit: 12g` and `restart: 'no'`.
4. Save the corrected YAML. If prompted to **rebuild / deploy / start**, check DSM Resource Monitor available RAM first and choose a low-activity maintenance window. Prefer around 24GB available with this 12GB limit; previously only 19GB was reported, so **do not start the long full-Taiwan import yet** if NAS memory/CPU pressure is high.
5. If Container Manager's failed project needs a cleanup before rebuilding, use **only this PoC Project's clean operation after confirming exactly what will be removed**. Do **not** click Delete Project or remove host data; Synology warns that Delete removes project containers and data. Do not run `docker system prune` or touch other projects. If the GUI cannot apply YAML due to partial container creation, inspect the single project's status/logs and ask for the new screenshot before doing anything destructive.
6. After applying and starting, confirm **one running Valhalla container** and that logs show graph generation. Monitor NAS available RAM / swap / CPU and free disk during import. Once service is ready, use the documented SSH tunnel and public-landmark API smoke test. This *only* verifies the route-engine PoC, **not** Taiwan vehicle legality.

**Current validation status:** image pull PASS; network create PASS; **container create FAIL**; graph import/API **NOT RUN**. Revised Compose + CI code prepared, **NAS device retest PENDING**.

## Valhalla baseline and next GH comparator — 2026-10-15

### Evidence: completed Valhalla warm query experiment

User NAS SSH screenshot confirms **3 warmup + 20 measured HTTP /route POST requests** using static public landmarks Taipei Main Station → Taipei 101, generic `auto` costing. Results:

```text
MinMs: 40.65
P50Ms: 41.95
P95Ms: 43.81
MaxMs: 44.16
DistanceKM: 6.239
TravelSeconds: 939.77
GeometryAvailable: True
LegalRoutingVerified: False
```

Baseline is **NAS-local (127.0.0.1) only**; HTTPS, Android device-to-NAS network latency, real traffic, and lawful SCOOTER/HEAVY_MOTORCYCLE access remain unmeasured. User's Synology SSH `direct-tcpip` forwarding was denied by sshd, so **test each engine via a normal SSH login and commands on the NAS shell** instead of Windows `ssh -L` tunnels. Do not weaken SSH forwarding policy or open a LAN port just for PoC testing.

### GraphHopper preparation — Windows PowerShell + File Station (NO engine start yet)

1. Do **not** touch the existing `graphhopper/data/taiwan.osm.pbf`: its SHA-256 previously matched Valhalla's input and the Windows source. Use `/volume1/docker/garmin-routing/graphhopper` (NAS project path verified for Valhalla, but verify this GraphHopper folder exists in the same share).
2. **Stop (not delete)** `garmin-valhalla-poc` in Container Manager. Stopping a container leaves host bind-mounted PBF and routing graph tiles intact. Leave the stopped project in place for later comparisons.
3. In Windows PowerShell, download the official GraphHopper **11.1 executable JAR** from [upstream's documented Maven Central artifact](https://github.com/graphhopper/graphhopper/blob/master/README.md), record its SHA-256; **do not download any OSM data again**.

   ```powershell
   $Jar = Join-Path $env:USERPROFILE "Downloads\graphhopper-web-11.1.jar"
   Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/graphhopper/graphhopper-web/11.1/graphhopper-web-11.1.jar" -OutFile $Jar
   Get-Item $Jar | Select-Object Name,Length
   Get-FileHash -Algorithm SHA256 $Jar
   ```

   Only use the upstream Maven file, not a random third-party Docker image. Check it is a plausible-size JAR (not an HTML error page).
4. Copy the JAR via SMB to **`docker/garmin-routing/graphhopper/data/graphhopper-web-11.1.jar`**; compare local and NAS SHA-256:

   ```powershell
   $Dir = "\\NAS_IP\docker\garmin-routing\graphhopper"
   Copy-Item -LiteralPath $Jar -Destination (Join-Path $Dir "data\graphhopper-web-11.1.jar")
   $Original = (Get-FileHash -Algorithm SHA256 $Jar).Hash
   $OnNas = (Get-FileHash -Algorithm SHA256 (Join-Path $Dir "data\graphhopper-web-11.1.jar")).Hash
   Write-Host "SameJar:" ($Original -eq $OnNas)
   ```

   Substitute NAS_IP **locally only**. Never commit LAN address, DSM account or passwords to a public GitHub PR.
5. Save the **reviewed** [GraphHopper Compose](compose.graphhopper.yml) as `graphhopper/docker-compose.yml` and [GraphHopper configuration](graphhopper-config.yml) as `graphhopper/graphhopper-config.yml`. Both filenames must be exact. The reviewed Compose does **not** contain `cpus: 2.0` or CPU CFS restrictions because Synology rejected NanoCPUs. Host port remains **`127.0.0.1:8989:8989`**; application `bind_host: 0.0.0.0` is only **inside** Docker.
6. Ensure `graphhopper/data/logs` is writable for the configuration's log appender. The Java process runs within the `/data` working directory and has `-Xmx8g` within a **12GB container memory ceiling**; check DSM Resource Monitor available RAM **immediately before** launching, and do not start both importers at once.
7. Container Manager → **Project → Create** with `garmin-graphhopper-poc` and path `/volume1/docker/garmin-routing/graphhopper`; review Compose and resource limits before deploying.
8. First GraphHopper run may take time importing `taiwan.osm.pbf` to `data/graph-cache`. Observe Project → Container logs, DSM available RAM, CPU, Btrfs free space and other service load. If Java heap/OOM, port, permission or config errors appear, retain evidence and stop; do not delete graph caches or alter the Valhalla project without diagnosis.
9. When logs show the **HTTP server started**, test *NAS-local only* from ordinary `ssh` Linux session:

   ```bash
   curl -sS --max-time 15 -o /tmp/graphhopper_route.json -w 'HTTP %{http_code}\n' --get 'http://127.0.0.1:8989/route' \
     --data-urlencode 'point=25.0478,121.5170' \
     --data-urlencode 'point=25.0338,121.5645' \
     --data-urlencode 'profile=car' \
     --data-urlencode 'points_encoded=false'
   ```

   Then `python3 -c 'import json; d=json.load(open("/tmp/graphhopper_route.json")); p=d["paths"][0]; print("DistanceKM",p["distance"]/1000,"TravelSeconds",p["time"]/1000,"GeometryAvailable",bool(p.get("points")))' `. **HTTP/JSON and geometry must pass first**, before running the 3+20 benchmark.

Note: GraphHopper 11.1's documented server requires **Java 25 or newer**, and source-provided `graphhopper-config.yml` ships a generic `car` profile. It is **not** a Taiwanese law-certified CAR / SCOOTER / HEAVY motorcycle routing profile. Comparing distance, time and p95 for one fixture is *not* a legal route-quality comparison; record additional OD pairs, reverse directions and official road evidence before selection.
