# API policy — Gemini M04 Stage 0 WORKING TEMPLATE

**NOT YET REVIEWED / G1 BLOCKED.** Owner: Gemini; reviewer: ChatGPT. Complete source quotes, official terms, affected code paths and actual PoC before Google provider is enabled.

| Provider | Endpoint / SDK | Attribution | Data-use allowed with OSM | Cache/retention | API key custody | Source and verified date | Result |
|---|---|---|---|---|---|---|---|
| Google Places UI Kit | pending PoC | pending | NOT ASSUMED | pending | pending | https://cloud.google.com/maps-platform/terms/maps-service-terms | BLOCKED |
| Non-Google search | pending selection | pending | license dependent | pending | TBD | TBD | NOT CONFIGURED |
| OSMF public Nominatim | public test only | ODbL / attribution review | policy-limited | per policy | no user key | https://operations.osmfoundation.org/policies/nominatim/ | NOT A PUBLIC PRODUCTION PROVIDER |

Specific questions: Does a returned coordinate qualify as downloadable/exportable Google content? May it be passed to independent Routing Provider? Where is it displayed? How long persisted? Attribution and terms? Restrictions for public app / Android Auto / BYOK? Verify official answers; when uncertain, G1 fails closed.

**Never put production API secrets in this repository.**
