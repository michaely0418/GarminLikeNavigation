# API Policy and Data-Use Governance — Stage 0

**Document Version:** 1.0.0  
**Effective Date:** 2026-10-14  
**Primary Author / Reviewer:** Gemini (Stage 0 Owner) / ChatGPT (Reviewer)  
**Status:** **G1 BLOCKED (Fail-Closed) / G4 ACTIVE / G6 ACTIVE**

---

## 1. Executive Summary & G1 / G4 / G6 Policy Gates

This document establishes the mandatory data licensing, attribution, caching, and credential governance policies for external APIs integrated into **GarminLikeNavigation**. 

| Gate | Scope | Status | Enforcement Invariant |
|---|---|---|---|
| **G1** | **Google Places & Map Data Flow** | **BLOCKED (Fail-Closed)** | Default provider configuration sets `google_places_enabled = false`. Google Places API coordinates **MUST NOT** be extracted and fed into a non-Google map (MapLibre/OSM) or independent routing engine (GraphHopper/Valhalla) within the same application context until an official, legally vetted compliance path is validated. |
| **G4** | **API Secrets & Cost Custody** | **ACTIVE** | No hardcoded API keys in repository, CI, or compiled APK. User-supplied keys (BYOK) must be stored in encrypted Android Keystore. Monthly billing caps and alert telemetry are mandatory before any live network requests. |
| **G6** | **Search Capacity & Fair Use** | **ACTIVE** | Public OSMF Nominatim is **strictly restricted** to non-production, manual test calls (<= 1 req/sec). Autocomplete-as-you-type on public Nominatim is hard-forbidden. Production search requires self-hosted (Photon/Pelias) or licensed commercial open-data backends. |

---

## 2. Google Maps Platform Service Terms Analysis

### 2.1 Authoritative Terms & Citations (Verified 2026-10-14)
* **Google Maps Platform Terms of Service:** [https://cloud.google.com/maps-platform/terms/](https://cloud.google.com/maps-platform/terms/)
* **Google Maps Platform Service-Specific Terms:** [https://cloud.google.com/maps-platform/terms/maps-service-terms](https://cloud.google.com/maps-platform/terms/maps-service-terms)
* **Places API Policies:** [https://developers.google.com/maps/documentation/places/web-service/policies](https://developers.google.com/maps/documentation/places/web-service/policies)

### 2.2 Key Contractual Restrictions

#### A. Prohibition Against Non-Google Maps (Section 3.2.3(b) "No Use with Non-Google Maps")
* **Rule:** Customer will not use Google Maps Core Services with any non-Google map in a Customer Application.
* **Impact on GarminLikeNavigation:** The core architecture uses open-source MapLibre Native with OpenStreetMap vector tiles. If the user searches for a destination using Google Places API (or Google Places UI Kit), extracts `geometry.location (lat, lng)`, calculates an independent route via self-hosted GraphHopper/OSRM, and renders that route on MapLibre tiles, this directly breaches Section 3.2.3(b).
* **Policy Verdict:** **Direct coordinate pass-through from Google Places to MapLibre/GraphHopper is PROHIBITED.** The Google search provider MUST remain disabled in production builds unless rendering on Google Maps SDK or unless an explicit written exemption / commercial license permits decoupled coordinate consumption.

#### B. Prohibition on Permanent Caching / Export (Section 3.2.4(a) "No Pre-fetching, Caching, or Storage")
* **Rule:** Customer will not pre-fetch, index, store, or cache any Google Content, except for temporary caching (up to 30 consecutive calendar days) solely to improve system performance. Storing coordinates permanently in a local offline database or routing graph is strictly prohibited.
* **Permitted Exception:** Google allows storing `place_id` indefinitely, but coordinates and place details must be refreshed periodically.
* **Impact on GarminLikeNavigation:** `PlaceCandidate` in `:core:model` defaults to `RetentionPolicy.NO_CACHE` or `RetentionPolicy.SESSION_ONLY`. Storing Google Place results in bookmarks or history requires storing only `providerPlaceId` and re-fetching or prompting the user.

#### C. Attribution Requirements
* Any UI displaying Google Places data must prominently display the official "Powered by Google" logo or use Google Places UI Kit's native attribution bar. Attribution must be legible and not obscured by custom Compose HUD layers.

---

## 3. Search Provider Feasibility Matrix

| Provider | Integration Type | Attribution | Allowed with OSM Map & GraphHopper? | Cache / Retention Policy | Key Custody / Cost Model | Stage 0 Verification Status |
|---|---|---|---|---|---|---|
| **Google Places UI Kit / Places SDK** | Android Native SDK | Required ("Powered by Google") | **NO (Breaches Terms 3.2.3(b))** | 30-day performance cache only; no coordinate export | BYOK via Android Keystore; Paid per Autocomplete session | **BLOCKED (G1 Fail-Closed)** |
| **Google Places Web API (REST)** | HTTP / Ktor | Required | **NO (Same non-Google map restriction)** | Session only; `place_id` retention permitted | Server-side proxy or BYOK; Pay per request | **BLOCKED (G1 Fail-Closed)** |
| **OSMF Public Nominatim** | HTTP REST | ODbL / OpenStreetMap Contributors | **YES (Fully compatible with OSM)** | Per ODbL (no bulk export restrictions, standard fair use) | No key required; Public community server | **RESTRICTED (G6: Dev/test only, max 1 req/s, NO autocomplete)** |
| **Self-Hosted Photon (OSM-based)** | Self-hosted Docker / Ktor | OpenStreetMap ODbL | **YES (Fully compatible)** | Unlimited local caching permitted | Self-hosted infrastructure cost; No third-party key | **RECOMMENDED TARGET (Stage 4)** |
| **Commercial Open Geocoders (Geoapify / Stadia / LocationIQ)** | HTTP REST | Commercial open attribution | **YES (Explicitly permits third-party map rendering)** | Plan-dependent (typically allows persistent coordinate routing) | BYOK / Project API Key; Monthly free tier + pay-as-you-go | **EVALUATE FOR STAGE 4** |

---

## 4. Architectural Data Flow & Verification PoC

```text
                  +-----------------------------------+
                  |        ISearchProvider            |
                  +-----------------------------------+
                                    |
                    Returns PlaceCandidate
                    [coordinateUsePolicy = PROVIDER_RESTRICTED (if Google)
                     retentionPolicy = SESSION_ONLY]
                                    |
                                    v
                  +-----------------------------------+
                  |     Downstream Compliance Check   |
                  +-----------------------------------+
                     /                             \
     If PROVIDER_RESTRICTED:                 If THIRD_PARTY_USE_VERIFIED:
     - Block OSM Route Overlay               - Forward coordinate to IRoutingProvider
     - Must switch to Google Map             - Render route on MapLibre vector tiles
       OR abort routing                      - Safe for offline trip history
```

### 4.1 Data Transfer Object Enforcement
In `:core:model/SearchModels.kt`:
* `CoordinateUsePolicy.PROVIDER_RESTRICTED`: Imposed on any Google Places source.
* `CoordinateUsePolicy.THIRD_PARTY_USE_VERIFIED`: Granted only to open-license providers (Photon, Geoapify, Nominatim).
* `PlaceCandidate.coordinate`: May be `null` if provider permits metadata display but restricts programmatic coordinate extraction.

---

## 5. Security & BYOK (Bring Your Own Key) Specification (G4)

1. **Zero Secret Ingestion:** The repository `.gitignore` prohibits any `google-services.json`, `*.keystore`, `.env`, or API key configuration files.
2. **BYOK Storage Architecture:**
   * When BYOK is enabled in settings, user-entered API keys are stored in `EncryptedSharedPreferences` backed by `MasterKey` in Android Keystore.
   * Keys are NEVER logged to logcat, crash reporters, or analytics.
   * Memory scrub: Secret strings are converted to `CharArray` and cleared after HTTP request construction where feasible.
3. **Budget Fail-Safe:**
   * An in-app client-side quota counter tracks daily and monthly billable requests.
   * If requests exceed 80% of user-configured threshold, a confirmation prompt is required before subsequent searches.

---

## 6. Public Nominatim Fair-Use Policy (G6)

To respect OpenStreetMap Foundation server resources:
* **Rate Limit:** Client enforces a minimum interval of 1,200ms between consecutive HTTP requests.
* **User-Agent Header:** All requests MUST set a descriptive User-Agent:
  `GarminLikeNavigation/0.1 (Android; +https://github.com/michaely0418/GarminLikeNavigation)`
* **No Search-As-You-Type:** Autocomplete on every keystroke is blocked by a 1,500ms debounce AND requires a minimum of 4 characters, with an explicit "Search" button press recommended.
* **Failure Handling:** HTTP 429 or 503 triggers exponential backoff and informs the user of public server capacity constraints.

---

## 7. Stage 0 Conclusion & Roadmap

1. **Stage 0 Action:** Keep `google_places_enabled = false` and mock search adapters for unit tests.
2. **Stage 4 Action:** Deploy a containerized Photon instance alongside GraphHopper for completely open, contractually safe search and routing in Docker/local environments.
