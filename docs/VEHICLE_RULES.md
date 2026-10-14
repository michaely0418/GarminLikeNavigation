# Taiwan Vehicle Access Rules & OpenStreetMap Tagging Specification — Stage 0

**Document Version:** 1.0.0  
**Effective Date:** 2026-10-14  
**Primary Author / Reviewer:** Gemini (Stage 0 Owner) / ChatGPT (Reviewer)  
**Status:** **G2 ACTIVE BASELINE (Ground Truth Rules Established / Rule Engine Pending Stage 2)**

---

## 1. Executive Summary & G2 Hard Invariants

In Taiwan road navigation, access permissions for vehicles are strictly stratified by law. Unlike passenger cars, motorized two-wheelers face intricate segment-level and lane-level legal restrictions.

### Architectural Invariants:
1. **Hard Rejection vs Cost Penalty:** When a road segment is legally forbidden (`RoadAccessStatus.FORBIDDEN`) for the active vehicle profile, the routing engine **MUST NOT** assign a high cost/weight penalty to it. It **MUST be completely pruned** from the routing graph (hard reject).
2. **Conservative Handling of UNKNOWN:** If the statutory eligibility of a tunnel, ramp, or segment is unverified in our dataset (`RoadAccessStatus.UNKNOWN`), the system must conservatively flag a warning (`LegalityReport.hasUnknownAccess = true`) and default to safe exclusion when traversing high-risk facilities (e.g. freeways/expressways).
3. **Statutory Signage Overrides OSM Road Class:** Generic OpenStreetMap tags (such as `highway=trunk` or `highway=primary`) do not accurately guarantee access for heavy motorcycles or scooters in Taiwan. Official government gazettes and roadside regulatory signage strictly supersede OSM classification.

---

## 2. Taiwan Statutory Framework

The rules defined in this specification are grounded in the following official laws and regulatory notices:

1. **道路交通管理處罰條例 (Road Traffic Management and Penalty Act):**
   * **第 33 條:** Regulations and penalties for freeways (高速公路) and expressways (快速公路).
   * **第 92 條第 2 項:** Grants large heavy motorcycles (大型重型機車, displacement > 250cc) the right to operate on expressways (省道快速公路) and designated pilot freeway sections, subject to Ministry of Transportation and Communications (MOTC) announcement.
   * **第 92 條第 6 項:** Authorizes MOTC to determine specific freeway sections and timelines for heavy motorcycle operation. (As of 2026, only National Freeway 3A has been opened).
2. **道路交通安全規則 (Road Traffic Safety Rules):**
   * **第 99 條:** Lane usage and turn rules for ordinary motorcycles (普通重型/輕型機車, <= 250cc). Governs motorcycle ban lanes (禁行機車道), inner fast lanes, and mandatory two-stage left turns (兩段式左轉).
   * **第 99-1 條:** Requires large heavy motorcycles (> 250cc) to adhere to rules and penalties applicable to small passenger vehicles (`CAR`), except where special provisions apply.
3. **高速公路及快速公路交通管制規則 (Freeway and Expressway Traffic Control Regulations):**
   * **第 19 條:** Strictly prohibits pedestrians, mopeds, and motorcycles with displacement under 250cc (`SCOOTER`) from entering freeways and expressways.
4. **Official Administrative Bulletins (MOTC & Highway Bureau):**
   * 交通部 101 年 7 月 10 日交路字第 10100234031 號公告 (Opening National Freeway 3A to large heavy motorcycles).
   * 交通部公路局各區養護工程分局行車管制公告 (Designation of specific tunnels such as Baguashan, Suhua Improvement tunnels, Caopu Moriyoshi tunnel).

---

## 3. Vehicle Classification & General Access Baseline

| Vehicle Code (`VehicleType`) | Legal Definition (Taiwan Regulation) | License Plate Color | National Freeways (國道) | Provincial Expressways (快速公路 台61~台88) | Urban Elevated Arterials (市區高架) | Fast-Traffic Inner Lanes (禁行機車道) |
|---|---|---|---|---|---|---|
| **`CAR`** | 自用小客車 / 小型車 | White (Black text) | **ALLOWED** | **ALLOWED** | **ALLOWED** | **ALLOWED** (Excludes Bus-Only lanes) |
| **`HEAVY_MOTORCYCLE`** | 大型重型機車 (Displacement > 250cc) | Yellow (250–549cc) / Red (>=550cc) | **FORBIDDEN** (Exception: **Freeway 3A ONLY**) | **ALLOWED** (Exception: **Baguashan Tunnel FORBIDDEN**) | **ALLOWED** (Per municipal regulations) | **ALLOWED** (Treated as `CAR` per Rule 99-1) |
| **`SCOOTER`** | 普通重型/輕型機車 (Displacement <= 250cc) | White (<=250cc) / Green (<=50cc) | **FORBIDDEN** (Strict statutory ban) | **FORBIDDEN** (Strict statutory ban) | **FORBIDDEN** (Except dedicated motorcycle ramps) | **FORBIDDEN** (Must use slow/outer lane or 2-stage turn) |

---

## 4. Segment-Level Ground Truth Matrix

The following table records critical Taiwan road segments that feature non-uniform access restrictions across the three vehicle classes.

| Segment Ref / Name | Route & Chainage | Direction | `CAR` | `HEAVY_MOTORCYCLE` | `SCOOTER` | Authority & Legal Basis | Official Source & Verified Date | OSM Tagging Caveats |
|---|---|---|---|---|---|---|---|---|
| **`TW-SEG-01`**<br>國道三號甲線 (國3甲主線) | 台北端 (0k+000) 至 深坑端 (5k+600) | 双向 (Both) | **ALLOWED** | **ALLOWED** | **FORBIDDEN** | 交通部公告<br>處罰條例第92條 | 交通部交路字第10100234031號<br>(Verified 2026-10-14) | `highway=motorway`, `motorcycle=no`, `motorcycle:conditional=yes @ (engine_capacity > 250)`. Missing conditional tag causes engines to ban heavy moto. |
| **`TW-SEG-02`**<br>國道一號主線 (汐五高架/主線) | 汐止至五股段 (11k 至 33k) | 双向 (Both) | **ALLOWED** | **FORBIDDEN** | **FORBIDDEN** | 交通部高公局<br>處罰條例第33, 92條 | 高公局國道通行管制公告<br>(Verified 2026-10-14) | `highway=motorway`, `motorroad=yes`. Heavy moto forbidden despite 92-2 enabling clause. |
| **`TW-SEG-03`**<br>國道五號雪山隧道 | 坪林交流道 (15k) 至 頭城交流道 (30k) | 双向 (Both) | **ALLOWED** | **FORBIDDEN** | **FORBIDDEN** | 交通部高公局<br>高快速交管第19條 | 高公局雪山隧道管制規則<br>(Verified 2026-10-14) | `highway=motorway`, `tunnel=yes`. Special long tunnel ban. |
| **`TW-SEG-04`**<br>台76線八卦山隧道 | 林厝IC (26k+500) 至 中興系統 (31k+400) | 双向 (Both) | **ALLOWED** | **FORBIDDEN** | **FORBIDDEN** | 交通部公路局<br>公路法第33條 | 公路局中區養護工程分局公告<br>(Verified 2026-10-14) | `highway=trunk`, `motorroad=yes`. Tagged as express trunk, but heavy moto is explicitly banned. Hard failure if inferred from road class. |
| **`TW-SEG-05`**<br>台64線八里新店快速公路 | 八里端 (0k) 至 新店端 (28k) | 双向 (Both) | **ALLOWED** | **ALLOWED** | **FORBIDDEN** | 交通部公路局<br>處罰條例第92條 | 公路局快速公路行車管制表<br>(Verified 2026-10-14) | `highway=trunk`, `motorroad=yes`. |
| **`TW-SEG-06`**<br>台北市民大道高架道路 | 環河北路匝道 至 舊宗路出入口 | 双向 (Both) | **ALLOWED** | **ALLOWED** | **FORBIDDEN** | 台北市政府交通局<br>北市高架管理規定 | 北市交工字第09632831200號<br>(Verified 2026-10-14) | `highway=trunk` or `primary`, `motorcycle:conditional=yes @ (engine_capacity > 250)`. |
| **`TW-SEG-07`**<br>台北大橋機車專用道 | 台北延平北路引道 至 三重重新路端 | 西向 (Westbound) | **FORBIDDEN** | **FORBIDDEN** | **ALLOWED** | 台北市/新北市交局<br>道安規則第99條 | 台北市聯絡橋梁交通管制規範<br>(Verified 2026-10-14) | `highway=cycleway` or `highway=path`, `motorcycle=yes`, `mofa=yes`. Cars/Heavy motos physically barred. |
| **`TW-SEG-08`**<br>台北大橋主線快車道 | 台北民權西路端 至 三重重新路主線 | 双向 (Both) | **ALLOWED** | **ALLOWED** | **FORBIDDEN** | 台北市/新北市交局<br>道安規則第99條 | 台北市聯絡橋梁交通管制規範<br>(Verified 2026-10-14) | `highway=primary`, `motorcycle=no`. Scooter forbidden from entering main bridge. |
| **`TW-SEG-09`**<br>台9線蘇花改東澳隧道群 | 蘇澳端 (104k) 至 東澳端 (113k) | 双向 (Both) | **ALLOWED** | **ALLOWED** | **FORBIDDEN** | 交通部公路局<br>東區養護工程分局 | 公路局 111-08-30 蘇花改常態開放公告<br>(Verified 2026-10-14) | `highway=trunk`, `tunnel=yes`. Heavy moto permanently opened 2022-08; scooter banned (must use 台9丁). |
| **`TW-SEG-10`**<br>台9線蘇花改仁水隧道 | 155k+800 至 158k+750 | 双向 (Both) | **ALLOWED** | **ALLOWED** | **ALLOWED** | 交通部公路局<br>東區養護工程分局 | 公路局 109-01-06 仁水隧道通車公告<br>(Verified 2026-10-14) | Outer lane tagged `highway=primary`, allows ordinary scooter (`motorcycle=yes`). Only Suhua Improvement tunnel allowing scooters. |
| **`TW-SEG-11`**<br>台9線南迴改草埔森永隧道 | 安朔 (437k+600) 至 草埔 (442k+200) | 双向 (Both) | **ALLOWED** | **ALLOWED** | **FORBIDDEN** | 交通部公路局<br>南區養護工程分局 | 公路局 110-04-30 常態開放公告<br>(Verified 2026-10-14) | `highway=trunk`, `tunnel=yes`. Heavy moto opened 2021-04; ordinary scooter banned (must use 台9戊). |
| **`TW-SEG-12`**<br>台61線西濱快速公路 | 全線主線高架段 | 双向 (Both) | **ALLOWED** | **ALLOWED** | **FORBIDDEN** | 交通部公路局<br>處罰條例第92條 | 公路局快速公路管制總表<br>(Verified 2026-10-14) | `highway=trunk`, `motorroad=yes`. Note: certain construction bypasses require localized speed drops. |
| **`TW-SEG-13`**<br>新北環河快速道路主線 | 三重龍門路 至 新店安祥路 | 双向 (Both) | **ALLOWED** | **ALLOWED** | **FORBIDDEN** | 新北市政府交通局<br>新北高架道路管制規約 | 新北市府交安字第1020194812號<br>(Verified 2026-10-14) | `highway=trunk`, `motorcycle=no`. Scooters must use the grade-separated parallel scooter viaducts. |
| **`TW-SEG-14`**<br>台北市新生高架道路 | 濟南路引道 至 通河街圓山引道 | 双向 (Both) | **ALLOWED** | **ALLOWED** | **FORBIDDEN** | 台北市政府交通局<br>市區高架管制規約 | 北市交工字第09632831200號<br>(Verified 2026-10-14) | `highway=primary`, `motorroad=yes`. Heavy moto allowed, ordinary scooter banned. |
| **`TW-SEG-15`**<br>台74線中彰快速公路 | 快官 (0k) 至 霧峰 (37k) | 双向 (Both) | **ALLOWED** | **ALLOWED** | **FORBIDDEN** | 交通部公路局<br>處罰條例第92條 | 公路局中區養護工程分局公告<br>(Verified 2026-10-14) | `highway=trunk`, `motorroad=yes`. Ring expressway around Taichung. |
| **`TW-SEG-16`**<br>台北車站忠孝西路一段 | 重慶南路口 至 中山南北路口 | 双向 (Both) | **ALLOWED** | **ALLOWED** | **FORBIDDEN** (23:00~06:00 開放) | 台北市政府交通局<br>道路管制公告 | 北市交工字第1113067820號<br>(Verified 2026-10-14) | Time-conditional access: `motorcycle:conditional=no @ (06:00-23:00)`. Daytime scooter access banned since 1978. |

---

## 5. OpenStreetMap Tagging Mapping & Pitfalls

### 5.1 The Motorcycle Semantic Collision
In global OpenStreetMap standards:
* `motorcycle=*` refers to any 2-wheeled motor vehicle capable of > 50 km/h.
* In Taiwan, the law separates 2-wheeled vehicles into **普通機車 (Scooter, <=250cc)** and **大型重機 (Heavy Motorcycle, >250cc)**, which follow completely different legal regimes.

### 5.2 Mandatory Tag Rules for Taiwan Routing Profiles

| Vehicle Profile | OSM Access Tag Rules Evaluated by Engine |
|---|---|
| **`CAR`** | Standard `motorcar != no` and `access != no`. |
| **`HEAVY_MOTORCYCLE`** | Evaluates: `motorcycle:conditional` containing `engine_capacity > 250` OR (`motorroad == yes` AND segment not in `TW-EXCLUDED-TUNNELS`) OR (`highway == motorway` ONLY IF `ref == "國3甲"`). Banned on all other `highway=motorway`. |
| **`SCOOTER`** | Evaluates: `highway != motorway`, `motorroad != yes`, `motorcycle != no`, and avoids `lanes:backward:motorcycle=no` / `lanes:forward:motorcycle=no` on urban multi-lane ways. |

### 5.3 Segment Blacklist & Whitelist Extraction Pipeline (for Stage 2)
1. **Hard Whitelist (Freeway 3A for Heavy Moto):**
   Tag pattern: `highway=motorway` AND `ref="國3甲"`.
2. **Hard Blacklist (Baguashan Tunnel for Heavy Moto):**
   Way IDs corresponding to Provincial Highway 76 chainage 26k+500 to 31k+400 MUST be explicitly tagged `heavy_motorcycle=no` in graph pre-processing.
3. **Scooter Bridge Separation:**
   Bridge relations must map `dedicated_cycleway_scooter` ways as traversable and primary bridge ways as forbidden for `SCOOTER`.

---

## 6. Verification & Legal Source Registry

* All statutory URLs in this document are anchored to the Laws & Regulations Database of the Republic of China ([https://law.moj.gov.tw/](https://law.moj.gov.tw/)), the Freeway Bureau ([https://www.freeway.gov.tw/](https://www.freeway.gov.tw/)), and the Directorate General of Highways ([https://www.thb.gov.tw/](https://www.thb.gov.tw/)).
* Date of verification: **2026-10-14**.
* Recheck policy: Any legislative amendment to Road Traffic Management and Penalty Act Article 92 triggers an immediate revision of this registry.
