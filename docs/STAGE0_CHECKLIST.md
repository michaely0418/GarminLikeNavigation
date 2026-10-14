# Stage 0 — actual delivery gate / 2026-10-14

## ChatGPT local-scaffold artifacts
- [x] Chosen 11-module directory structure and version catalog.
- [x] Six Kotlin interface files and common DTOs authored.
- [x] Architecture, ADR-001, unit conventions, product spec and shared `.ai/` files authored.
- [x] GitHub CI workflow and Issue/PR templates authored.
- [x] Local static checks + schema validator provided.
- [ ] **Android Studio Gradle Sync / assembleDebug verified** — no Android SDK/networked Gradle dependencies in authoring environment.
- [ ] Actual `gradle-wrapper.jar` retrieved and verified on build machine. This ZIP includes bootstrap scripts, not jar.
- [ ] Real GitHub remote created, protected branches and CI executed.

## Gemini Stage 0 assignments
- [x] M04 `API_POLICY.md` official-data-flow and UI Kit compliance checklist (G1).
- [x] M07 `VEHICLE_RULES.md` legally sourced versioned segment records (G2).
- [x] M13 `tests/routing-cases/taiwan_vehicle_benchmarks.json` **>=15 reviewable cases** (16 cases delivered and schema-validated).
- [x] M03 `HUD_WIREFRAME.md` actual mockup and functional boundaries.
- [x] Gemini cross-review of six contracts and stage0 scaffolding (`.ai/REVIEW_LOG.md`).

## Blockers G1–G6
- G1 GOOGLE DATA: BLOCKED (Fail-Closed; google_places_enabled = false).
- G2 LEGAL ACCESS: BASELINE DELIVERED (16 segment records verified; rule engine in Stage 2).
- G3 ANDROID AUTO: NOT IMPLEMENTED; DHU and trusted installer in Stage 8.
- G4 SECRET/COST: Design policies recorded; BYOK Keystore architecture specified.
- G5 OFFLINE: O1 NOT IMPLEMENTED, do not advertise.
- G6 SEARCH CAPACITY: Public Nominatim restricted to dev tests; production search in Stage 4.

**Stage 0 status: GEMINI DELIVERABLES COMPLETE — PR submitted for ChatGPT review & CI verification.**
