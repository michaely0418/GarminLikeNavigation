# Architecture — v1.0 / Stage 0

## Module graph

```text
:app (single APK / wiring)
 ├── :core:model          (JVM, common types / units)
 ├── :core:interfaces     (JVM, 6 contracts; depends on model + Flow)
 ├── :core:routing        (JVM, routing adapter / legality / Garmin-like cost)
 ├── :core:navigation     (JVM, headless navigation state)
 ├── :core:search         (JVM, provider adapters; license metadata)
 ├── :core:safety         (JVM, mockable independent engine; Gemini)
 ├── :core:voice          (JVM, OpenAI/Gemini adapters)
 ├── :ui:map              (Android library, map rendering; ChatGPT)
 ├── :ui:hud              (Android library, phone HUD/Compose; Gemini)
 └── :car:app             (Android library; Stage 8 CarAppService, same APK)
```

Dependency direction: Android UI/Car/app may reference interfaces; core never depends on app/Android UI. Headless routing/navigation/safety can be unit tested. `:core:safety` only depends on the `:core:interfaces` API surface (which exports shared models transitively).

## Happy path after implementation

```text
ISearchProvider (location discovery + permissions metadata)
          | PlaceCandidate (not automatically cacheable)
          v
IRoutingProvider (open road graph, vehicle hard access filter first)
          | NavRoute + legality + explanation
          v
INavigationCore --(LocationSample)--> state + voice guidance
          |                   \
          v                    ISafetyEngine (direction + along-route check)
        :ui:map / :ui:hud / :car:app
          ^
  IAiVoiceAssistant --(authorized tool request)--> Navigation Core
```

## Hard invariants

- Forbidden access cannot be permitted by choosing lower penalty. UNKNOWN is not ALLOWED.
- Navigation must refuse a route whose `LegalityReport.hasViolation` is true; unknown must be surfaced and conservatively handled.
- M09 uses `LocationSample` + optional `NavRoute`, never needs M08 implementation to begin tests.
- `PlaceCandidate` carries source attribution and data-use restrictions; no implicit permanent Google-place coordinate storage.
- Voice has no unilateral authority to choose unsafe routes or change destinations.
- Assistant API failures cannot stop core guidance or alerts.
- Android Auto supports approved host templates. `:ui:hud` Compose views are for phone only.
- No precise GPS or personal history is uploaded without explicit requirement and privacy review.

## Config and future adapters

Stage 0 scaffolding deliberately has no network providers. Future services must be explicit interfaces, isolated configs, and mockable data. Real-world benchmarks record map version / engine version / rule version and official evidence.

## Stage boundaries

- Stage 0: skeleton + contracts + CI baseline + Gemini prerequisites.
- Stage 1: map, location + HUD.
- Stage 2–3: Docker GraphHopper/Valhalla PoC + three vehicle legality.
- Stage 4: search providers, Google UI Kit compliance PoC.
- Stage 5–6: guidance and safety.
- Stage 7: optional AI voice.
- Stage 8: Android Auto host + DHU/real-car tests.
- Stage 9: QA/release readiness.
- O1: **actual device offline route re-planning** is separate.
