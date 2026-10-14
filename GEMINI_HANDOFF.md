# Stage 0 — instructions to Gemini (正式交接)

## Context and approval
Product owner **formally approved** v1.0 split and Stage 0 initiation on **2026-10-14**. Q1–Q4 are reflected in the code and docs. Local starter ZIP exists, **no GitHub remote and no successful Android build yet**.

## Gemini Stage 0 owner deliverables (do not rewrite ChatGPT's scaffold wholesale)

1. **M04/G1** — update `docs/API_POLICY.md`: current official Google Places/Places UI Kit terms, actual Google search→coordinate→self routing→Google/OSM map data-flow PoC analysis. Attribution, export/cache/retention and BYOK restrictions must be explicitly traced. If uncertain keep Google provider disabled. Supply authoritative dated sources. Public OSMF Nominatim is not unlimited autocomplete.
2. **M07/G2** — fill `docs/VEHICLE_RULES.md`: official Taiwan road-regulation rules with specific route/ramp/tunnel/section/direction, date, official links, road-map tagging caveats for `CAR`, `SCOOTER`, `HEAVY_MOTORCYCLE`. Law/road-sign evidence > class-level assumptions. UNKNOWN remains conservative.
3. **M13/Q2** — add at least **15 geographically grounded, reviewable** cases to `tests/routing-cases/taiwan_vehicle_benchmarks.json`, conforming to `tests/routing-cases/schema.json`. Do **not** fabricate sources or assert a road legal if unverified; use `UNKNOWN` or explicit verification status. Include violations and unknown cases. Never modify expected results to hide failure.
4. **M03** — complete `docs/HUD_WIREFRAME.md` with phone UI design and cross-boundary Android Auto restrictions; Compose implementation is a Stage 1 follow-up.
5. Independently review `core/model` and six Kotlin interfaces, dependency graph, Gradle config, security, and CI. Provide issue list with severity and recommended patches, not an unsolicited architecture rewrite.

## Exact constraints
- Application ID `app.garminlike.nav` provisional (may change BEFORE Play release).
- `:app`, `:core:model`, `:core:interfaces`, `:core:navigation`, `:core:routing`, `:core:safety`, `:core:search`, `:core:voice`, `:ui:map`, `:ui:hud`, `:car:app`.
- Units: WGS84 degrees; meters Double; m/s Double; bearing `[0,360)`; UTC epochMillis Long; duration seconds Long.
- Model plus interfaces is the single DTO source; proposal/ADR required for a breaking change.
- `ISafetyEngine` directly mocked from `LocationSample` and `NavRoute?`; actual live camera alert requires direction/route matching and provenance.
- Known forbidden road segments are hard reject, not weight penalty. Heavy motorcycle legal classification not inferred solely from road code.
- Four offline capabilities are separate; O1 offline re-route not yet implemented.
- App API credentials cannot be committed; ChatGPT / Gemini subscriptions do not imply free API access.

## Required reply format
Provide one review issue table: `severity`, `file`, `finding`, `proposal`, `evidence`; followed by updated file list, official policy/source list + dates, schema validation evidence, unit/CI results (actually run vs not run), blockers G1–G6, and a clear `APPROVE / REQUEST CHANGES` recommendation.

Use `.ai/DELIVERY_TEMPLATE.md` and `.ai/REVIEW_LOG.md` for handoff. **Do not treat source of truth as Gemini chat alone**; commits/PRs/actual test logs are authoritative once GitHub exists.
