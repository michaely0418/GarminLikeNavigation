# HOTFIX-001 — Windows PowerShell Gradle Wrapper bootstrap

**Affected build:** Stage 0 source v0.0.1  
**Hotfix:** v0.0.1-r1  
**Date:** 2026-10-14

## Root cause

Windows PowerShell 5.1 can return an HTTP response with `.Content` as `System.Byte[]` when the Gradle `.sha256` endpoint uses a binary content type. The original script incorrectly called `.Content.Trim()` as if it were a string and failed before downloading the Wrapper JAR.

## Changes

- `scripts/bootstrap-gradle-wrapper.ps1`: download the checksum to a temporary file using `-OutFile`, read it as text, validate that a 64-digit SHA-256 exists, download the official Gradle 8.13 wrapper JAR, and verify `Get-FileHash` before installation. Cleanup uses `finally`.
- `gradlew.bat`: remove an accidental control character in the missing-Wrapper error help line; no functional change to normal startup.
- No changes to Android/Kotlin/Gradle plugin versions or shared interface contracts.

## Validation

The script has been inspected and the extracted project has been re-packaged. **It has not been executed on Windows PowerShell or used to build an Android APK in this environment.** The Windows user should run the steps below and share any subsequent error output.

## Apply (existing extraction)

Copy the standalone patched `bootstrap-gradle-wrapper.ps1` into `scripts\`, overwriting the old file, and run:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\bootstrap-gradle-wrapper.ps1
.\gradlew.bat --version
.\gradlew.bat :core:model:test :app:assembleDebug
```

**Expected first command:** reports a verified `gradle-wrapper.jar`.  
**Expected after build:** `app\build\outputs\apk\debug\app-debug.apk`.
