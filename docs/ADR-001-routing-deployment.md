# ADR-001 — Routing deployment during prototyping

- **Date:** 2026-10-14
- **Status:** Accepted for prototype by Product Owner's Stage 0 approval; re-evaluate for public release.
- **Decision owner:** ChatGPT (integration), product owner approves changes.

## Context
Need controllable road-class, distance, turn, interchange and legality costs in Taiwan, independently of Google routes. Building mobile offline routing at the beginning is high risk and delays validation.

## Decision
Start with Docker-hosted, self-operated routing API (GraphHopper or Valhalla adapter chosen after comparative PoC). Android clients obtain route geometry and instruction data over authenticated HTTPS on trusted server, and may cache a previously obtained route per license/privacy rules. Avoid claiming it works offline when a new destination needs routing.

## Options
- A: Docker routing backend: easier profile iteration, more testable, requires network for new routes (**selected**).
- B: Native mobile offline router immediately: best offline capability, substantially more engineering and dataset footprint (**deferred to O1**).
- C: Google Navigation: mature but conflicts with self-controlled routing objective (**not selected**).

## Consequences
- Must log map/engine/profile/rules version for tests.
- Server endpoint costs, reliability, HTTPS, API auth, capacity and privacy require independent review.
- Four separate capabilities: offline tiles; cached active route; on-device guidance on cached route; on-device fresh route/re-route.
- O1 requires routing graph + routing runtime on device and legal road tests, not just cache.

## Revisit trigger
Public launch, routing SLA problems, cloud cost, O1 milestone, vehicle legality coverage or data policy changes.
