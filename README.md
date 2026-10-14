# Garmin-like Navigation — Stage 0 scaffold

**Status:** Approved to begin Stage 0 by the product owner on 2026-10-14. **This ZIP is a local scaffold and documentation handoff; NOT a verified Android APK or a completed Stage 0.**

> Google-compatible destination search (only after license Gate G1) + self-controlled Garmin-like routing + CAR / SCOOTER / HEAVY_MOTORCYCLE + Taiwan safety alerts + optional OpenAI/Gemini voice + Android Auto.
>
> No Garmin intellectual property, firmware, maps, trademark endorsement or proprietary data is included.

## What is actually included

- Multi-module Android/Kotlin Gradle project (`:app`, `:core:*`, `:ui:*`, `:car:app`).
- Six Stage 0 Kotlin interfaces and shared type contracts, including access legality and data retention metadata.
- A minimal launch Activity (text-only **not a navigation app**), placeholder modules, and basic Kotlin unit tests.
- GitHub Actions CI blueprint, Git/PR/Issue templates, architecture, ADR, interface guide, benchmark schema.
- Gemini task handoff for Safety, Search, HUD, vehicle legal references, 15 benchmark cases.

**Not yet implemented:** actual MapLibre/Google map, Google Places integration, route calculation, Android Auto CarAppService, GPS/TBT, AI voices, safety database, production offline routing. Android Auto module is a nonregistered placeholder by design.

## Quick start (Windows 11 + Android Studio)

1. Install Android Studio, Android SDK Platform API 35 and Build Tools 35.0.0. Gradle requires the *Java 17 toolchain* specified by `jvmToolchain(17)`. HOTFIX-003 enables automatic JDK 17 provisioning via the Foojay toolchain resolver; network access is needed on first use. Android Studio JBR may run Gradle under Java 21, separately.
2. Extract the ZIP into a **short ASCII path**, e.g. `C:\Dev\GarminLikeNavigation` (the extracted root contains `settings.gradle.kts`).
3. Open **PowerShell** in the extracted project root, then execute:
   ```powershell
   powershell -ExecutionPolicy Bypass -File scripts\bootstrap-gradle-wrapper.ps1
   .\gradlew.bat :core:model:test :app:assembleDebug
   ```
   The bootstrap retrieves the official Gradle **8.13** Wrapper JAR and verifies the SHA-256 from the official Gradle server. Requires network access. It is deliberately **not shipped** as an unverified binary.
4. Android Studio > Open > select extracted project root > Gradle Sync. Choose the JDK configured in Android Studio. Run `app` on an emulator or a developer-mode phone. The screen displays only `Stage 0`.
5. Result **after successful build**: `app/build/outputs/apk/debug/app-debug.apk`. No APK is included in this delivery.

Build stack **proposed (not compiled here)**: AGP 8.13.2, Gradle 8.13, Kotlin 2.2.20, Java 17, compile/target SDK 35, minimum SDK 26, car-app library 1.7.0. Version bumps are reviewed via ADR.

## GitHub import (remote repository not created)

Open a terminal in extracted project root:
```powershell
git init -b main
git add .
git commit -m "chore: bootstrap Stage 0 navigation architecture"
git remote add origin https://github.com/YOUR_ACCOUNT/GarminLikeNavigation.git
git push -u origin main
```
Create the **empty private repository first** in GitHub. Do not push API secrets. Create `develop` after initial commit and require PR/CI checks before merges. See `docs/GIT_WORKFLOW.md`.

## Deliverables & handoff

- `docs/ARCHITECTURE.md`, `docs/ADR-001-routing-deployment.md`, `docs/INTERFACE_CONTRACTS.md`, `docs/PRODUCT_SPEC.md`.
- `docs/UNIT_CONVENTIONS.md`, `docs/API_POLICY.md`, `docs/VEHICLE_RULES.md`, `docs/HUD_WIREFRAME.md`.
- `.ai/*`, `GEMINI_HANDOFF.md`, `tests/routing-cases/*`, `.github/workflows/android-ci.yml`.
- `docs/STAGE0_CHECKLIST.md` has truthfully marked completion / pending gates.

No API keys are required at Stage 0. Google Cloud account, Play Console and vehicle testing are later gated activities.
