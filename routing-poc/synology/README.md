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

- **Still needed:** Package Center → **Container Manager** installed version, NAS available RAM under normal workload (Resource Monitor), actual project volume/share path and writable permissions.
- **Initial disk planning:** reserve around **50 GB** for the raw input, two graph indexes and logs (planning estimate, not a measured requirement). The DSM screenshot's **5.6 TB free** is ample for this reserve. Do not infer that `儲存空間 1` means files are mounted at `/volume1`; verify the intended volume/project Shared Folder path before running.
- **SSD cache is not a storage volume:** do not configure a graph/PBF bind mount using the cache capacity; use normal NAS Btrfs Shared Folder storage. Cache write amplification, graph build I/O and snapshots must be monitored.
- **UPS presence is not runtime assurance:** confirm APC UPS shutdown behavior and battery runtime if running a long initial graph import.
- **Privacy:** DSM screenshots may display NAS account, QuickConnect ID and serial number. Do not copy those into a public GitHub Issue, log, SOP or screenshot shared externally.

### GUI preflight checklist

1. **Package Center → Installed → Container Manager**: record version and status. If it is not installed, install it from a Synology-supported source only; do not guess compatibility.
2. **Resource Monitor → Memory / CPU**: capture actual available RAM and NAS load while normal containers and services are running; 64 GB physical RAM does not equal 64 GB free.
3. **Storage Manager → Storage / Volumes** and **Control Panel → Shared Folder**: locate the intended shared folder `docker` and its actual volume. If it does not exist, create a separate NAS Shared Folder for `garmin-routing`, rather than modifying existing application directories.
4. **Container Manager → Project**: confirm it can create Compose projects, and do not click Build/Start until the local OSM data and correct volume path are ready.
5. **UPS & backup window**: avoid overlapping full-Taiwan import with backup, rebuild, scrubbing, snapshots or other high-I/O tasks.
3. **RAM planning for observed 64 GB (estimate, not an engine hard requirement):**
   - First attempt: **full Taiwan PBF**, using the **same exact input SHA-256** on both engines; no mandatory Taipei/New Taipei cropping solely due to RAM.
   - **Valhalla:** `cpus: 2.0`, `server_threads=2`, `mem_limit: 12g`.
   - **GraphHopper:** `cpus: 2.0`, `mem_limit: 12g`, JVM `-Xms1g -Xmx8g`. Java heap is only part of process/container memory; file caches, JVM native memory and indexing overhead also matter.
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
4. Verify port mapping `127.0.0.1:8002:8002`, data bind mount `./data:/custom_files`, `platform=linux/amd64`, two CPU cores and an initial 12GB memory cap (based on the owner's 64GB DSM report, not a measured consumption figure). **Do not enable Web Station web portal.**
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
4. Compose uses Eclipse Temurin **Java 25 JRE** + official GraphHopper JAR, generic `car` profile, 2 CPU, total container RAM cap 12GB and JVM `-Xmx8g`. Confirm current installed NAS RAM supports this allocation. GraphHopper upstream's sample config binds to localhost by default; using the repository-provided modified config is **necessary inside Docker** to serve port 8989 through its host-loopback bind.
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

Do one engine at a time on the **same NAS**, same frozen dataset SHA256, roughly equal memory caps, 2 CPU cores, and no other heavy NAS job. Record actual JVM heap / cache/profile differences so the results are not misrepresented as identical workloads.

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
- Revised initial import target: **full Taiwan OSM**; Valhalla and GraphHopper each get a **12GB** container memory ceiling, GraphHopper 8GB JVM max heap, two CPU cores, executed **sequentially**.
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
| Container Manager installed version | **NOT YET PROVIDED** |
| Available NAS memory under real workload | **NOT YET PROVIDED** |
| Actual target project Shared Folder / mount directory | **NOT YET VERIFIED** |
| Native on-NAS Docker run / OSM import / API benchmark | **NOT RUN** |

The templates remain **PoC-only** and limited to local NAS loopback; avoid exposing the endpoint through LAN forwarding, reverse proxies, DDNS or Cloudflare Tunnel until service authentication and TLS are designed. Taiwan legal vehicle rules and Google Places license gating remain unchanged.
