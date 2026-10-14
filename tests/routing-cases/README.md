# Taiwan vehicle benchmark input schema / Stage 0

`schema.json` is the input-case contract. `taiwan_vehicle_benchmarks.json` is **intentionally empty** pending Gemini's >=15 real, sourced examples. This is NOT a complete Stage 0 benchmark deliverable.

Input uses `mustAllowSegments` for legally **permitted** segments, not necessarily traversed ones. `mustTraverseSegments` is a separate route geometry assertion. `mustForbidSegments` are hard failures. `legalExpectation=UNKNOWN` is appropriate when official evidence has not been verified. No fake traffic/ETA results.

Future run results should be stored outside expected fixtures (suggest `results/YYYY-MM-DD/*.json`) and record `mapDataVersion`, `routingEngineVersion`, `ruleSetVersion`, `profileVersion`, `routeGeometryHash`, `observedAtEpochMillis`, official sources and outcome.

Validate draft JSON via `python scripts/validate_benchmarks.py --allow-draft`. Enforce Stage 0 >=15 only via `python scripts/validate_benchmarks.py` when Gemini dataset is ready. `jsonschema` Python module required in local environment.
