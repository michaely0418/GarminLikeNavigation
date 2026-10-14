# Taiwan Vehicle Access Rules & OpenStreetMap Tagging Specification — Stage 0

**Document Version:** 1.1.0  
**Effective Date:** 2026-10-14  
**Primary Author / Reviewer:** Gemini (Stage 0 Owner) / ChatGPT (Reviewer)  
**Status:** **G2 IN PROGRESS (Specification Baseline & Curated Benchmark Draft; Engine Verification Pending Stage 2)**

---

## 1. Executive Summary & G2 Hard Invariants

In Taiwan road navigation, access permissions for vehicles are strictly stratified by law. Unlike passenger cars, motorized two-wheelers face intricate segment-level and lane-level statutory restrictions.

### Architectural Invariants:
1. **Hard Rejection vs Cost Penalty:** When a road segment is legally forbidden (`RoadAccessStatus.FORBIDDEN`) for the active vehicle profile, the routing engine **MUST NOT** assign a high cost/weight penalty to it. It **MUST be completely pruned** from the routing graph (hard reject).
2. **Conservative Handling of UNKNOWN:** If the statutory eligibility of a tunnel, ramp, or segment is unverified or marked `RECHECK_REQUIRED` in our dataset (`RoadAccessStatus.UNKNOWN`), the system must conservatively flag a warning (`LegalityReport.hasUnknownAccess = true`) and default to safe exclusion when traversing high-risk facilities (e.g. freeways/expressways).
3. **Statutory Signage Overrides OSM Road Class:** Generic OpenStreetMap tags (such as `highway=trunk` or `highway=primary`) do not accurately guarantee access for heavy motorcycles or scooters in Taiwan. Official government gazettes and roadside regulatory signage strictly supersede OSM classification.
4. **Verification Integrity:** Passing schema checks or curating specification rules does **not** equal engine-tested or road-tested certification. Legal references are audited and differentiated by verification status (`VERIFIED` vs `RECHECK_REQUIRED`).

---

## 2. Taiwan Statutory Framework

The rules defined in this specification are grounded in the following official laws and regulatory notices:

1. **道路交通管理處罰條例 (Road Traffic Management and Penalty Act):**
   * **第 33 條:** Regulations and penalties for freeways (高速公路) and expressways (快速公路). [全國法規資料庫](https://law.moj.gov.tw/LawClass/LawSingle.aspx?pcode=K0040012&flno=33)
   * **第 92 條第 2 項:** Grants large heavy motorcycles (大型重型機車, displacement > 250cc) the right to operate on expressways (省道快速公路) and designated pilot freeway sections, subject to Ministry of Transportation and Communications (MOTC) announcement. [全國法規資料庫](https://law.moj.gov.tw/LawClass/LawSingle.aspx?pcode=K0040012&flno=92)
   * **第 92 條第 6 項:** Authorizes MOTC to determine specific freeway sections and timelines for heavy motorcycle operation. (As of 2026, only National Freeway 3A has been opened).
2. **道路交通安全規則 (Road Traffic Safety Rules):**
   * **第 99 條:** Lane usage and turn rules for ordinary motorcycles (普通重型/輕型機車, <= 250cc). Governs motorcycle ban lanes (禁行機車道), inner fast lanes, and mandatory two-stage left turns (兩段式左轉). [全國法規資料庫](https://law.moj.gov.tw/LawClass/LawSingle.aspx?pcode=K0040013&flno=99)
   * **第 99-1 條:** Requires large heavy motorcycles (> 250cc) to adhere to rules and penalties applicable to small passenger vehicles (`CAR`), except where special provisions apply. [全國法規資料庫](https://law.moj.gov.tw/LawClass/LawSingle.aspx?pcode=K0040013&flno=99-1)
3. **高速公路及快速公路交通管制規則 (Freeway and Expressway Traffic Control Regulations):**
   * **第 19 條:** Strictly prohibits pedestrians, mopeds, and motorcycles with displacement under 250cc (`SCOOTER`) from entering freeways and expressways. [全國法規資料庫](https://law.moj.gov.tw/LawClass/LawSingle.aspx?pcode=K0040019&flno=19)
4. **Authoritative Administrative Bulletins & Gazette Links:**
   * **國道 3 號甲線開放公告:** 交通部臺灣區國道高速公路局 101 年 6 月 28 日管字第 1016005186 號公告（101 年 7 月 1 日起實施），開放 550cc 以上大型重機行駛國 3 甲台北聯絡道全線。 [交通部法規檢索系統](https://motclaw.motc.gov.tw/webMotcLaw2018/SLaw/Content?soid=4686)
   * **快速公路大型重機行駛路段與限制一覽表:** 交通部公路局公告，詳細明列各快速公路開放與禁行里程。 [公路局公告表](https://www.thb.gov.tw/News_ExpresswaySection.aspx?n=462&sms=13790)
   * **國道禁止大型重機通行說明:** 交通部高速公路局國道行車管制通告。 [高公局官方說明](https://www.freeway.gov.tw/Publish.aspx?cnid=1778)

---

## 3. Vehicle Classification & General Access Baseline

| Vehicle Code (`VehicleType`) | Legal Definition (Taiwan Regulation) | License Plate Color | National Freeways (國道) | Provincial Expressways (快速公路 台61~台88) | Urban Elevated Arterials (市區高架) | Fast-Traffic Inner Lanes (禁行機車道) |
|---|---|---|---|---|---|---|
| **`CAR`** | 自用小客車 / 小型車 | White (Black text) | **ALLOWED** | **ALLOWED** | **ALLOWED** | **ALLOWED** (Excludes Bus-Only lanes) |
| **`HEAVY_MOTORCYCLE`** | 大型重型機車 (Displacement > 250cc) | Yellow (250–549cc) / Red (>=550cc) | **FORBIDDEN** (Exception: **Freeway 3A ONLY**) | **ALLOWED** (Exceptions: **Baguashan Tunnel 26.5k-32.6k**, **Taichung Ring 74 Dali-Wufeng 34k-39.2k FORBIDDEN**) | **ALLOWED** (Per municipal regulations) | **ALLOWED** (Treated as `CAR` per Rule 99-1) |
| **`SCOOTER`** | 普通重型/輕型機車 (Displacement <= 250cc) | White (<=250cc) / Green (<=50cc) | **FORBIDDEN** (Strict statutory ban) | **FORBIDDEN** (Strict statutory ban) | **FORBIDDEN** (Except dedicated motorcycle ramps) | **FORBIDDEN** (Must use slow/outer lane or 2-stage turn) |

---

## 4. Segment-Level Ground Truth Matrix

The following table records specific Taiwan road segments that feature non-uniform access restrictions across the three vehicle classes.

| Segment Ref / Name | Route & Chainage | Direction | `CAR` | `HEAVY_MOTORCYCLE` | `SCOOTER` | Authority & Legal Basis | Official Direct Link | Verification Status | OSM Tagging Caveats |
|---|---|---|---|---|---|---|---|---|---|
| **`TW-SEG-01`**<br>國道三號甲線 (國3甲全線) | 台北端 (0k+000) 至 深坑端 (5k+600) | 双向 (Both) | **ALLOWED** | **ALLOWED** | **FORBIDDEN** | 高公局 101.06.28 管字第1016005186號公告<br>(101.07.01 實施) | [交通部公報詳細連結](https://motclaw.motc.gov.tw/webMotcLaw2018/SLaw/Content?soid=4686) | **VERIFIED** | `highway=motorway`, `ref=國3甲`, `motorcycle:conditional=yes @ (engine_capacity > 250)`. Missing conditional tag causes generic engines to ban heavy moto. |
| **`TW-SEG-02`**<br>國道一號主線 (汐五高架/主線) | 汐止至五股段 (11k 至 33k) | 双向 (Both) | **ALLOWED** | **FORBIDDEN** | **FORBIDDEN** | 高公局管制公告<br>處罰條例第33, 92條 | [高公局國道通告](https://www.freeway.gov.tw/Publish.aspx?cnid=1778)<br>[處罰條例第92條](https://law.moj.gov.tw/LawClass/LawSingle.aspx?pcode=K0040012&flno=92) | **VERIFIED** | `highway=motorway`, `motorroad=yes`. Heavy moto strictly forbidden despite 92-2 enabling clause. |
| **`TW-SEG-03`**<br>國道五號雪山長隧道 | 坪林交流道 (15k) 至 頭城交流道 (30k) | 双向 (Both) | **ALLOWED** | **FORBIDDEN** | **FORBIDDEN** | 高公局長隧道管制公告<br>高快速交管第19條 | [高公局長隧道管制](https://www.freeway.gov.tw/Publish.aspx?cnid=1778)<br>[高快速交管第19條](https://law.moj.gov.tw/LawClass/LawSingle.aspx?pcode=K0040019&flno=19) | **VERIFIED** | `highway=motorway`, `tunnel=yes`. Strict prohibition. |
| **`TW-SEG-04`**<br>台76線八卦山隧道及銜接段 | 林厝IC (26k+500) 至 中興系統 (32k+600) | 双向 (Both) | **ALLOWED** | **FORBIDDEN** | **FORBIDDEN** | 公路局快速公路限制一覽表<br>長隧道危險品與車種管制 | [公路局快速公路公告表](https://www.thb.gov.tw/News_ExpresswaySection.aspx?n=462&sms=13790) | **VERIFIED** | `highway=trunk`, `motorroad=yes`. Prohibited segment is 26.5k to 32.6k (6.1 km total). Engines must not infer access from expressway class. |
| **`TW-SEG-05`**<br>台64線八里新店快速公路 | 八里端 (0k) 至 新店端 (28k) | 双向 (Both) | **ALLOWED** | **ALLOWED** | **FORBIDDEN** | 公路局快速公路行駛路段表<br>處罰條例第92條第2項 | [公路局快速公路公告表](https://www.thb.gov.tw/News_ExpresswaySection.aspx?n=462&sms=13790) | **VERIFIED** | `highway=trunk`, `motorroad=yes`. |
| **`TW-SEG-06`**<br>台北市民大道高架道路 | 環河北路匝道 至 舊宗路出入口 | 双向 (Both) | **ALLOWED** | **ALLOWED** | **FORBIDDEN** | 台北市政府交通局公告<br>北市高架管理規定 | [台北市道路交通管制規定](https://www-ws.gov.taipei/) | **RECHECK_REQUIRED** | `highway=trunk` or `primary`, `motorcycle:conditional=yes @ (engine_capacity > 250)`. Dynamic municipality notice; requires gazette check in Stage 2. |
| **`TW-SEG-07`**<br>台北大橋機車專用道 | 台北延平北路引道 至 三重重新路端 | 西向 (Westbound) | **FORBIDDEN** | **FORBIDDEN** | **ALLOWED** | 台北市/新北市交局聯絡橋梁規定<br>道安規則第99條 | [道安規則第99條](https://law.moj.gov.tw/LawClass/LawSingle.aspx?pcode=K0040013&flno=99) | **RECHECK_REQUIRED** | `highway=cycleway` or `highway=path`, `motorcycle=yes`. Cars/Heavy motos physically barred. |
| **`TW-SEG-08`**<br>台北大橋主線快車道 | 台北民權西路端 至 三重重新路主線 | 双向 (Both) | **ALLOWED** | **ALLOWED** | **FORBIDDEN** | 台北市/新北市交局聯絡橋梁規定<br>道安規則第99條 | [道安規則第99條](https://law.moj.gov.tw/LawClass/LawSingle.aspx?pcode=K0040013&flno=99) | **RECHECK_REQUIRED** | `highway=primary`, `motorcycle=no`. Scooter forbidden from entering main bridge carriageway. |
| **`TW-SEG-09`**<br>台9線蘇花改東澳隧道群 | 蘇澳端 (104k) 至 東澳端 (113k) | 双向 (Both) | **ALLOWED** | **ALLOWED** | **FORBIDDEN** | 交通部公路局東區養護工程分局<br>蘇花改常態化開放公告 | [公路局新聞及公告專區](https://www.thb.gov.tw/) | **RECHECK_REQUIRED** | `highway=trunk`, `tunnel=yes`. Heavy moto permanently opened 2022-08; scooter banned (must use 台9丁). Dynamic portal link pending permanent gazette ID. |
| **`TW-SEG-10`**<br>台9線蘇花改仁水隧道 | 155k+800 至 158k+750 | 双向 (Both) | **ALLOWED** | **ALLOWED** | **ALLOWED** | 交通部公路局東區養護工程分局<br>仁水隧道通車管理公告 | [公路局新聞及公告專區](https://www.thb.gov.tw/) | **RECHECK_REQUIRED** | Outer lane tagged `highway=primary`, allows ordinary scooter (`motorcycle=yes`). Only Suhua Improvement tunnel allowing scooters. |
| **`TW-SEG-11`**<br>台9線南迴改草埔森永隧道 | 安朔 (437k+600) 至 草埔 (442k+200) | 双向 (Both) | **ALLOWED** | **ALLOWED** | **FORBIDDEN** | 交通部公路局南區養護工程分局<br>草埔森永隧道常態開放公告 | [公路局新聞及公告專區](https://www.thb.gov.tw/) | **RECHECK_REQUIRED** | `highway=trunk`, `tunnel=yes`. Heavy moto opened 2021-04; ordinary scooter banned (must use 台9戊). Dynamic portal link pending permanent gazette ID. |
| **`TW-SEG-12`**<br>台61線西濱快速公路 | 八里至新竹、香山至曾文溪橋等通車段 | 双向 (Both) | **ALLOWED** | **ALLOWED** (依通車路段) | **FORBIDDEN** | 公路局快速公路限制一覽表<br>處罰條例第92條 | [公路局快速公路公告表](https://www.thb.gov.tw/News_ExpresswaySection.aspx?n=462&sms=13790) | **VERIFIED** | `highway=trunk`, `motorroad=yes`. **Caveat:** 香山平面交織段、八里台北港端、鳳鼻尾隧道銜接路段非封閉高架，須按現地標誌管制，不得全線泛化。 |
| **`TW-SEG-13`**<br>新北環河快速道路主線 | 三重龍門路 至 新店安祥路 | 双向 (Both) | **ALLOWED** | **ALLOWED** | **FORBIDDEN** | 新北市政府交通局高架管制規約 | [新北市政府公告](https://www.traffic.ntpc.gov.tw/) | **RECHECK_REQUIRED** | `highway=trunk`, `motorcycle=no`. Scooters must use the grade-separated parallel scooter viaducts. |
| **`TW-SEG-14`**<br>台北市新生高架道路 | 濟南路引道 至 通河街圓山引道 | 双向 (Both) | **ALLOWED** | **ALLOWED** | **FORBIDDEN** | 台北市政府交通局高架管制規約 | [台北市道路交通管制規定](https://www-ws.gov.taipei/) | **RECHECK_REQUIRED** | `highway=primary`, `motorroad=yes`. Heavy moto allowed, ordinary scooter banned. |
| **`TW-SEG-15A`**<br>台74線快官至大里二段 | 快官 (0k+000) 至 大里二交流道 (34k+000) | 双向 (Both) | **ALLOWED** | **ALLOWED** | **FORBIDDEN** | 公路局快速公路限制一覽表<br>處罰條例第92條第2項 | [公路局快速公路公告表](https://www.thb.gov.tw/News_ExpresswaySection.aspx?n=462&sms=13790) | **VERIFIED** | `highway=trunk`, `motorroad=yes`. Main urban ring expressway of Taichung. |
| **`TW-SEG-15B`**<br>台74線大里二至霧峰端 | 大里二交流道 (34k+000) 至 霧峰端 (39k+235) | 双向 (Both) | **ALLOWED** | **FORBIDDEN** | **FORBIDDEN** | 公路局快速公路限制一覽表<br>銜接國道3號高架路段管制 | [公路局快速公路公告表](https://www.thb.gov.tw/News_ExpresswaySection.aspx?n=462&sms=13790) | **VERIFIED** | `highway=trunk`. **Strict prohibition:** Heavy motorcycle prohibited between 34k+000 and 39k+235. Engine must prune this segment! |
| **`TW-SEG-16`**<br>台北車站忠孝西路一段 | 重慶南路口 至 中山南北路口 | 双向 (Both) | **ALLOWED** | **ALLOWED** | **FORBIDDEN** (23:00~06:00 開放) | 台北市政府交通局道路管制公告 | [台北市交工處公告](https://www.bote.gov.taipei/) | **RECHECK_REQUIRED** | Time-conditional access: `motorcycle:conditional=no @ (06:00-23:00)`. Daytime scooter access banned since 1978. |

---

## 5. OpenStreetMap Tagging Mapping & Pitfalls

### 5.1 The Motorcycle Semantic Collision
In global OpenStreetMap standards:
* `motorcycle=*` refers to any 2-wheeled motor vehicle capable of > 50 km/h.
* In Taiwan, the law separates 2-wheeled vehicles into **普通機車 (Scooter, <=250cc)** and **大型重機 (Heavy Motorcycle, >250cc)**, which follow completely different statutory regimes.

### 5.2 Mandatory Tag Rules for Taiwan Routing Profiles

| Vehicle Profile | OSM Access Tag Rules Evaluated by Engine |
|---|---|
| **`CAR`** | Standard `motorcar != no` and `access != no`. |
| **`HEAVY_MOTORCYCLE`** | Evaluates: `motorcycle:conditional` containing `engine_capacity > 250` OR (`motorroad == yes` AND segment NOT in `TW-EXCLUDED-TUNNELS` and NOT in `TW-SEG-15B`) OR (`highway == motorway` ONLY IF `ref == "國3甲"`). Banned on all other `highway=motorway`. |
| **`SCOOTER`** | Evaluates: `highway != motorway`, `motorroad != yes`, `motorcycle != no`, and avoids `lanes:backward:motorcycle=no` / `lanes:forward:motorcycle=no` on urban multi-lane ways. |

### 5.3 Segment Pruning Rules (for Stage 2 Routing Engine)
1. **Hard Whitelist (Freeway 3A for Heavy Moto):**
   Way query: `highway=motorway` AND `ref="國3甲"`.
2. **Hard Blacklists (Heavy Moto Restrictions on Expressways):**
   * **Baguashan Tunnel Segment:** Way IDs on Provincial Highway 76 from `26k+500` to `32k+600` MUST be explicitly blacklisted.
   * **Taichung Ring 74 East Section:** Way IDs on Provincial Highway 74 from `34k+000` to `39k+235` (Dali-2 IC to Wufeng end) MUST be explicitly blacklisted.
3. **Scooter Bridge Separation:**
   Bridge relations must map `dedicated_cycleway_scooter` ways as traversable and primary bridge ways as forbidden for `SCOOTER`.

---

## 6. Verification & Legal Source Registry

* All statutory URLs in this document are anchored to:
  * 全国法規資料庫 ([https://law.moj.gov.tw/](https://law.moj.gov.tw/))
  * 交通部法規檢索系統 ([https://motclaw.motc.gov.tw/](https://motclaw.motc.gov.tw/))
  * 交通部高速公路局通報 ([https://www.freeway.gov.tw/](https://www.freeway.gov.tw/))
  * 交通部公路局快速公路行駛限制一覽表 ([https://www.thb.gov.tw/News_ExpresswaySection.aspx?n=462&sms=13790](https://www.thb.gov.tw/News_ExpresswaySection.aspx?n=462&sms=13790))
* Audit date: **2026-10-14**.
* Items labeled `RECHECK_REQUIRED` will undergo secondary verification against municipal official gazettes during Stage 2 before ingestion into the live routing graph.
