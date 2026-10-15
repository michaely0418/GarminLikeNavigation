# Stage 2 Routing PoC Design — Proposal (No Legal Routing Claim)

Status: Draft / isolated development branch, 2026-10-15. Related Issue #11.

## Architecture decision
- `:app` and `:ui:map` remain Stage 1 preview unchanged.
- `routing-poc/` contains a localhost-only experimental Valhalla service; no router API calls are added to Android app.
- Compare Valhalla vs GraphHopper using the SAME pinned Geofabrik Taiwan OSM PBF, vehicle rules version, test cases and host resources. Engine choice remains open.
- Adopt versioned PBF SHA256 + image digest + graph build config, before making any comparative speed or route claims.

## Comparative test dimensions

| Dimension | Required evidence | Default status |
|---|---|---|
| Import | timestamp, CPU/RAM, output graph size, duration | NOT RUN |
| API | HTTP response/error, encoded shape, distance, time, latency | NOT RUN |
| Reproducibility | PBF hash, image digest, engine version, config | NOT RUN |
| CAR | smoke route on fixed public test coordinates | NOT RUN |
| SCOOTER | segment + lane-level lawful mapping; verified hard rejection | BLOCKED pending G2 |
| HEAVY_MOTORCYCLE | legally versioned freeway/expressway/tunnel access | BLOCKED pending G2 |
| UNKNOWN | high-risk segment fails closed with explicit reason | NOT IMPLEMENTED |
| Benchmark | 18 schema cases with geometry/hash + legal evidence | SCHEMA-ONLY |
| Security | localhost port + no secrets committed + ODbL attribution | NOT RUN |

## Hard safety invariants
1. A routing engine's built-in costing profile is NOT a Taiwanese road-access law verification.
2. Any `FORBIDDEN` edge MUST be excluded. `UNKNOWN` on high-risk road MUST be excluded or return no certified route.
3. If the available graph lacks direction-specific official evidence, do not silently reinterpret a segment as ALLOWED.
4. Destination search uses no Google Places data until G1 legal review grants it.
5. The PoC will not be accessible to mobile devices by default: `127.0.0.1` binding. Any further access requires authenticated HTTPS, network controls, and approval.

## Proposed next decisions (Product Owner)
- Host: Windows Docker Desktop / Linux Docker VM / Synology (need supported architecture and RAM).
- Trial area: Taiwan PBF as provided by Geofabrik, or a smaller cropped PBF if hardware limits require.
- After first Valhalla smoke test and source review, prepare a matching GraphHopper test environment on the same PBF for fair comparison.

References:
- https://github.com/valhalla/valhalla
- https://github.com/nilsnolde/docker-valhalla
- https://github.com/graphhopper/graphhopper/blob/master/docs/core/deploy.md
- https://download.geofabrik.de/asia/taiwan.html
- https://github.com/michaely0418/GarminLikeNavigation/issues/11

**Stage 2 engine selection and legal routing still pending. No PR merge until Issue #6 rulesets validated and Product Owner acceptance.**
