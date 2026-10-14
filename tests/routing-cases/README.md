# Taiwan Vehicle Benchmark Dataset Specification — Stage 0

**Document Version:** 1.1.0  
**Status:** **SCHEMA VALIDATED / SPECIFICATION DRAFT (Human-Curated Legal Ground Truth)**

---

## 1. Overview & Verification Lifecycle

`taiwan_vehicle_benchmarks.json` contains curated test cases conforming to `schema.json`. To prevent misleading claims about test maturity, all benchmarks in this project pass through four explicit verification stages:

| Level | Status Identifier | Definition & Criteria | Stage 0 Current State |
|---|---|---|---|
| **L1** | `schema_validated` | The JSON file strictly passes Draft 2020-12 JSON Schema validation via `validate_benchmarks.py` with unique case IDs and >=15 test cases. | **PASS (18 cases verified)** |
| **L2** | `source_verified` | Every rule assertion is linked to an authoritative, dated official gazette, government bulletin, or statute article. (Cases linked to dynamic news pages are marked `RECHECK_REQUIRED`). | **IN PROGRESS (Key gazettes verified; specific local rules pending Stage 2 recheck)** |
| **L3** | `routing_executed` | The headless routing engine (GraphHopper/Valhalla) calculates an actual path geometry, verifies that forbidden segments are pruned, and records execution metrics. | **NOT RUN (Routing engine implementation scheduled for Stage 2/3)** |
| **L4** | `road_tested` | On-vehicle physical GPS road testing on actual Taiwan road corridors under production driving conditions. | **NOT RUN (Scheduled for Stage 8/9 QA)** |

---

## 2. Segment Identification & Draft / Manual Status

In Stage 0, no compiled OpenStreetMap routing graph exists. Consequently:
* Segment identifiers such as `TW-SEG-xx` and `sourceType: ROAD_NAME_HINT` or `OFFICIAL_SEGMENT_ID` serve as **Human-Curated Specification Drafts (draft/manual status)**.
* They define the statutory ground truth requirement (e.g. "Freeway 3A must be allowed for heavy motorcycles, but Baguashan Tunnel 26.5k-32.6k must be forbidden").
* **Stage 2 Transition Requirement:** When the Docker GraphHopper / Valhalla engine is stood up in Stage 2, these abstract segment references MUST be explicitly resolved to:
  1. Concrete `OSM_WAY_ID` sets extracted from the Taiwan `.osm.pbf` extract.
  2. Bounding polygon corridors (`GEOMETRY_CORRIDOR`) with directional azimuth filters.

---

## 3. Benchmark Case Expectations

* `mustAllowSegments`: Segments that the vehicle is legally entitled to enter. (Does NOT assert that the fastest route MUST traverse them).
* `mustForbidSegments`: Segments that the vehicle is legally prohibited from entering. Any route containing these segments is a hard safety failure.
* `mustTraverseSegments`: Segments that the route MUST include to satisfy specific mandatory routing requirements (e.g. dedicated scooter viaduct on Taipei Bridge).
* `legalExpectation`:
  * `ALLOWED`: The destination can be legally reached without violating road regulations.
  * `FORBIDDEN`: No statutory route exists, or the primary queried corridor is illegal and must be completely pruned.
  * `UNKNOWN`: Regulatory status is unverified; engine must fail closed or emit user warning.

---

## 4. Running Validation

```powershell
python scripts/validate_benchmarks.py
```
> Note: Passing `validate_benchmarks.py` validates **JSON Schema and case count (L1 `schema_validated`)**, not runtime route execution (L3) or physical road verification (L4).
