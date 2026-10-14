# Current task

- Stage: 0 closeout / Stage 1 planning
- Status: Stage 0 scaffold & Gemini specs ACCEPTED; Stage 1 implementation **NOT STARTED**
- Integration branch: `develop`
- Closeout branch: `chore/stage0-closeout`
- Stage 0 tracking: [Issue #1](https://github.com/michaely0418/GarminLikeNavigation/issues/1), [PR #3](https://github.com/michaely0418/GarminLikeNavigation/pull/3) (MERGED)
- Next task: [Issue #4 — MapLibre map, GPS location and CAR/SCOOTER/HEAVY_MOTORCYCLE selector](https://github.com/michaely0418/GarminLikeNavigation/issues/4)
- Owners: ChatGPT — core app/map/location; Gemini — phone HUD/UX/review; Product Owner — device acceptance
- Stage 1 feature branches (to create when starting implementation): `feature/stage1-map-location` and `feature/stage1-hud`
- Stage 0 acceptance: 11 modules + 6 interfaces, CI tests/assembleDebug & artifact, 18 schema-valid drafts and policy/HUD specs, R1–R10 review
- G1: BLOCKED (Google Places vs non-Google map); G2: IN PROGRESS (Taiwan segment legality / GIS mapping)
- Not proven: routing execution, legal route coverage, field road tests, full offline rerouting, Android Auto, production Google search compliance
- Next decision: finish Stage 0 closeout PR and then obtain implementation go-ahead for Issue #4
- Avoid: writing to `main` directly, co-editing shared modules without coordinating, claiming legal validity from JSON validation, committing API credentials
