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
- [ ] M04 `API_POLICY.md` official-data-flow and UI Kit compliance checklist (G1).
- [ ] M07 `VEHICLE_RULES.md` legally sourced versioned segment records (G2).
- [ ] M13 `tests/routing-cases/taiwan_vehicle_benchmarks.json` **>=15 reviewable cases**, annotated where legality is unverified.
- [ ] M03 `HUD_WIREFRAME.md` actual mockup and functional boundaries.
- [ ] Gemini cross-review of six contracts and stage0 scaffolding.

## Blockers G1–G6
- G1 GOOGLE DATA: BLOCKED until actual terms + PoC verified.
- G2 LEGAL ACCESS: BLOCKED pending segment evidence and benchmark checks.
- G3 ANDROID AUTO: NOT IMPLEMENTED; DHU and trusted installer later.
- G4 SECRET/COST: design policies recorded; runtime implementation later.
- G5 OFFLINE: O1 NOT IMPLEMENTED, do not advertise.
- G6 SEARCH CAPACITY: public Nominatim not used as production backend.

**Stage 0 status: IN PROGRESS — deliverable skeleton produced, not accepted as done.**
