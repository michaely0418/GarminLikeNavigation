# Stage 1 — Map & GPS Prototype (v0.1.0)

Related issue: https://github.com/michaely0418/GarminLikeNavigation/issues/4

## Scope
ChatGPT implements the Android phone demo: MapLibre 12.3.1, OpenFreeMap network tiles, Android foreground LocationManager, 3 vehicle choices, zoom, recenter, day/night styles, preference persistence.
Gemini separately owns the :ui:hud Compose HUD feature branch. This PR does not modify :ui:hud, Android Auto or six core interfaces.

## Provider / License
- Day: https://tiles.openfreemap.org/styles/liberty
- Night: https://tiles.openfreemap.org/styles/dark
- OpenFreeMap mobile guidance: https://openfreemap.org/quick_start/
- Provider attribution: https://github.com/hyperknot/openfreemap
- MapLibre SDK: https://maplibre.org/maplibre-native/android/
- MapLibre attribution controls are not disabled. The controls are outside the map viewport.
- The map requires internet. No tile bulk download, no Google Places API or key used.
- Re-evaluate tile licensing, acceptable use, attribution, and SLA before public release.

## Permissions and location handling
- INTERNET / ACCESS_NETWORK_STATE for HTTPS map; approximate/fine location only while foreground.
- No background location permission, location upload service or analytics integration added.
- Tile provider can infer approximate map area and IP address from tile HTTP requests.
- Permission denied, device location disabled and no fix yet are shown as states.

## Testing — 本次要測試項目
1. Launch with Internet and confirm Taiwanese map instead of Stage 0 placeholder.
2. Allow precise GPS; current-location marker and coordinate/accuracy displayed.
3. Allow approximate only; if network location available, no crash and location works.
4. Deny location; map remains functional and retry location button responds.
5. Turn location off/on and resume activity; verify recovery.
6. Map drag, pinch, zoom buttons and recenter button.
7. Switch CAR / SCOOTER / HEAVY_MOTORCYCLE and confirm settings after relaunch.
8. Toggle day/night map style, relaunch, and confirm persistence.
9. Rotate portrait/landscape; ensure no crash and UI is usable.
10. Disable data network; tiles may fail, app must not crash.
11. Verify attribution is visible and no foreground overlay hides it.
12. Real GitHub CI passes Kotlin tests, Debug APK build and Artifact upload.

## Build (PowerShell)
git fetch origin
git switch feature/stage1-map-location
git pull --ff-only origin feature/stage1-map-location
.\gradlew.bat :core:model:test :core:navigation:test :app:assembleDebug
Debug APK: app/build/outputs/apk/debug/app-debug.apk

## Known limitations
- No route engine, turn-by-turn, real speed camera alerts, Android Auto, Gemini/OpenAI or full offline maps yet.
- MapLibre Marker API used for demo (deprecated), to be replaced with style layers.
- Stage 0 G1 Google Places license remains BLOCKED; G2 Taiwan legal segments are IN PROGRESS.
- Vehicle choice is UI state only; it does not yet calculate legal routes.
- APK compilation passing does not prove physical device GNSS or map networking works.
