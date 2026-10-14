# Product specification | v1.0 baseline

## Positioning
A Taiwan-focused independent navigation App emphasizing understandable, legally valid, direct routes. Google-level destination search is a desired **optional** provider, pending applicable Google Maps Platform terms and actual UI/data-flow PoC. Routing is independent of Google Navigation SDK.

## User-visible objectives
1. Profiles: `CAR`, `SCOOTER`, `HEAVY_MOTORCYCLE`. Mandatory legal access rules precede heuristic preferences.
2. Choice among recommended, fastest, direct, and major-road-leaning routes. No universal assertion that matching Garmin's algorithm is possible.
3. Drive guidance: GPS, matching, voice prompts, safe rerouting and useful guidance, progressively delivered.
4. Taiwan alerts: fixed / section speed / enforcement with source provenance, direction and route correlation. Must work independently of AI.
5. Google search, OSM/alternative search, and map/routing engines decoupled; license compliance gates any public deployment.
6. Android Auto supports safe templates; phone Compose HUD is not reusable as host template wholesale.
7. Optional voice providers OpenAI/Gemini, separately billed APIs, no embedded long-lived API secret.
8. Offline feature levels tracked separately; O1 complete offline rerouting is an independent milestone.

## Non-goals for Stage 0
No production route engine, offline routing, Android Auto host approval, live search, active camera database, voice APIs or release signing. No road-legal guarantee without road-level official evidence. No copying Garmin proprietary assets.

## Acceptance principle
User is Product Owner; an AI code claim is not build proof. Critical access violations and policy problems block merge. See `docs/STAGE0_CHECKLIST.md`.
