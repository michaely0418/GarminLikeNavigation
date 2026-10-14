# Current task

- Stage: Stage 1 — Mobile Map, Location & Compose HUD Prototype
- Status: 
  - ChatGPT: MapLibre, GPS, and Vehicle Mode State in PR #7 (`feature/stage1-map-location`, DRAFT/OPEN). Gemini reviewed and calibrated coverage to head `baea9ea` (APPROVE).
  - Gemini: Phone Compose HUD, 3 vehicle types, adaptive Portrait/Landscape, DEMO/NO ROUTE state, unit tests in PR #8 (`feature/stage1-hud`). Round 1 (H1–H5) and Round 2 (R2-H1, R2-H3) review fixes completed and verified on CI #38568576149.
- Integration branch: `develop`
- Stage 1 feature branches: `feature/stage1-map-location` (ChatGPT) and `feature/stage1-hud` (Gemini)
- Issue tracking: [Issue #4 — MapLibre map, GPS location and CAR/SCOOTER/HEAVY_MOTORCYCLE selector](https://github.com/michaely0418/GarminLikeNavigation/issues/4)
- Owners: ChatGPT — core app/map/location; Gemini — phone HUD/UX/review; Product Owner — device acceptance
- Stage 1 Gemini Deliverables: `:ui:hud` Jetpack Compose HUD, `docs/STAGE1_HUD.md` (v1.2.0), 18 JVM unit tests (`:ui:hud:testDebugUnitTest`), DEMO/NO ROUTE handling
- G1: BLOCKED (Google Places vs non-Google map); G2: IN PROGRESS (Taiwan segment legality / GIS mapping)
- Not proven: routing execution, legal route coverage, field road tests, full offline rerouting, Android Auto, physical device UI layout (NOT RUN)
- Next decision: Await ChatGPT Round 2 acceptance and coordinate Stage 1 integration with Product Owner. Do NOT self-merge.
- Avoid: writing to `main` directly, co-editing ChatGPT's `MainActivity.kt` or `ui/map`, claiming live road legality from mock data


