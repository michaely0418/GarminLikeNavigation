# Stage 2 S2-A0 — Synology NAS / Container Manager PoC SOP

**STATUS: CONFIGURATION ONLY / DEPLOYMENT NOT RUN / TAIWAN ROAD LAW NOT VERIFIED.**

Target: Synology DS1621xs+ (Intel Xeon D-1527, linux/amd64). This is a **public technical specification**, not an observation of the owner's installed RAM or DSM version. Verify actual RAM, DSM version, Container Manager availability, and volume free space **on the NAS** before deployment.

References:
- Synology Container Manager > Project: https://kb.synology.com/en-global/DSM/help/ContainerManager/docker_project
- Synology DS1621xs+ hardware: https://global.download.synology.com/download/Document/Hardware/ProductSpec/DiskStation/21-year/DS1621xs%2B/cht/Product_Spec_DS1621xs%2B_cht.pdf
- Official Valhalla: https://github.com/valhalla/valhalla/blob/master/docker/README.md
- Official GraphHopper: https://github.com/graphhopper/graphhopper/blob/master/README.md
- Geofabrik Taiwan: https://download.geofabrik.de/asia/taiwan.html
- Road-law specification: `docs/VEHICLE_RULES.md`; Stage 2 governance: `docs/API_POLICY.md`.

## 1. NAS prerequisites (do not assume they are met)

1. DSM > **控制台 > 資訊中心**: record DSM version, total physical RAM, CPU/architecture. Verify Container Manager is installed and supported for this DSM.
2. DSM > **儲存空間管理員**: check the actual volume containing Docker project folders. **Provision around 50 GB free initially** for two independent graph caches, raw OSM and backups/logs; this is a planning reserve, *not a measured requirement*.
3. **RAM planning (estimate, not an engine hard requirement):**
   - 8 GB installed: do **not** immediately import all of Taiwan or run both engines. Use the same constrained Taipei/New Taipei extract for preliminary exploration, review NAS load and memory first; cap only one engine at a time around 3GB and GraphHopper heap below its cap, accepting possible OOM during graph import.
   - >=16 GB: start one engine at a time, `cpus: 2.0`, `mem_limit: 6g`, GraphHopper `-Xmx4g` / `-Xms1g`. Stop if other NAS services starve or OOM occurs. Actual import may exceed these numbers; measure rather than guess.
   - 32 GB: more margin for experiments; still run sequentially initially to collect comparable data without contention.
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
4. Verify port mapping `127.0.0.1:8002:8002`, data bind mount `./data:/custom_files`, `platform=linux/amd64`, two CPU cores and memory cap 6GB (only on a NAS with sufficient free RAM). **Do not enable Web Station web portal.**
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
4. Compose uses Eclipse Temurin **Java 25 JRE** + official GraphHopper JAR, generic `car` profile, 2 CPU, total container RAM cap 6GB and JVM `-Xmx4g`. Confirm current installed NAS RAM supports this allocation. GraphHopper upstream's sample config binds to localhost by default; using the repository-provided modified config is **necessary inside Docker** to serve port 8989 through its host-loopback bind.
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

### 本次改動項目
- Synology-specific Docker Compose for two engines, source-configured GraphHopper, DSM Project SOP, Windows SSH tunnels, identical PBF and reproducible metrics script.

### 本次要測試項目
- [ ] GitHub CI offline Python unit tests and all Compose configuration syntax checks.
- [ ] User reports real NAS RAM, DSM, Container Manager version and storage capacity.
- [ ] User verifies both engines locally import the same PBF, response parsers work, and metrics are recorded.
- [ ] Gemini cross-reviews templates before merging Draft PR #13.
- [ ] GitHub Issue #6 Rulesets applied and tested before merge.
