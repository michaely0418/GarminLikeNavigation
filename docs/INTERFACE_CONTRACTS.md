# Six interface contracts / Stage 0 v1.0

Source of truth: `core/interfaces/src/main/kotlin/app/garminlike/nav/core/interfaces/` and common DTOs in `:core:model`.

| Contract | Main consumer | Responsibility |
|---|---|---|
| `VehicleProfile` | M06/M07 | VehicleType + legally evaluated access + soft preferences; FORBIDDEN hard stop |
| `ISearchProvider` | M04 | Provider search; PlaceCandidate source IDs, attribution and retention/coordinate use rights |
| `IRoutingProvider` | M06 | Origin/destination + vehicle/weights => candidate NavRoutes with legality + cost explanation |
| `INavigationCore` | M08 | StateFlow NavState, location updates, start/pause/resume/stop, voice events |
| `ISafetyEngine` | M09 | location+route => sourced, directional, route-correlated safety alerts |
| `IAiVoiceAssistant` | M10/M11 | Voice session, permitted tools, tool authorization and approval state |

## Non-negotiable semantics

1. `VehicleProfile.evaluateRoadAccess()` must return `UNKNOWN` when no verified segment-level entitlement exists. Penalizing a forbidden segment is **not** an acceptable substitute for excluding it.
2. A destination's search `coordinate` can be absent, and the provider's retention + usage policy may restrict routing/storage. Integrations must gate downstream use; a DTO does not legalize it.
3. `NavRoute.legality` distinguishes forbidden and unknown. Production Navigation Core must refuse known forbidden segments and signal unknown appropriately.
4. Distances stored meters, speeds `m/s`, bearing `[0,360)`, Unix UTC millis, durations seconds; see `UNIT_CONVENTIONS.md`.
5. `ISafetyEngine` does not issue real alerts using proximity only; mock fixtures must exercise opposing carriageways and along-route distance.
6. Tool requests from AI are proposals; any route-changing action passes through Navigation Core and may require user confirmation. No server access credentials in Kotlin sources.

## Version changes
This baseline is a draft freeze for cross-team work, not an immutable ABI. Breaking contract changes require `.ai/DECISIONS.md`, an ADR and two-side review. All modules compile from one source of model definitions.
