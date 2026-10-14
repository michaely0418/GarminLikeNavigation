# Stage 0 Hotfix-002 — Gradle Wrapper 404 correction (2026-10-14)

## Root cause
`services.gradle.org/distributions/gradle-8.13-wrapper.jar` returns 404 because Gradle does not publish the Wrapper JAR as a standalone distribution at that endpoint. Only checksum/signature metadata exists there. HOTFIX-001 resolved PowerShell's byte[] `.Trim()` failure but retained the invalid download URL.

## Fix
- Download `gradle/wrapper/gradle-wrapper.jar` from the Gradle project's official GitHub repository, pinned to tag `v8.13.0`.
- Verify against Gradle's published 8.13 Wrapper JAR SHA-256: `81a82aaea5abcc8ff68b3dfcb58b3c3c429378efd98e7433460610fecd7ae45f`.
- PowerShell 5.1 compatibility: TLS 1.2, `Invoke-WebRequest -OutFile`, `Get-FileHash`; do not rely on response `.Content` types.
- Do not replace an existing JAR until a successful checksum verification; remove temporary downloads on failure.
- Linux/macOS bootstrap script also updated.

References:
- https://docs.gradle.org/current/userguide/gradle_wrapper.html
- https://gradle.org/release-checksums/
- https://github.com/gradle/gradle/blob/v8.13.0/gradle/wrapper/gradle-wrapper.jar

## Windows steps
1. Copy `bootstrap-gradle-wrapper-r2.ps1` into the project's `scripts` folder, rename to `bootstrap-gradle-wrapper.ps1`.
2. In project root run:
   ```powershell
   powershell -ExecutionPolicy Bypass -File .\scripts\bootstrap-gradle-wrapper.ps1
   .\gradlew.bat --version
   .\gradlew.bat :core:model:test :app:assembleDebug
   ```

## Verification status
- Confirmed that official GitHub repository tag `v8.13.0` contains the Wrapper JAR and confirmed SHA-256 value on Gradle checksum reference.
- PowerShell script is statically inspected, but cannot be executed under Windows PowerShell in the current Linux environment.
- Network access to GitHub binaries is not available from this container; final download/checksum and Android SDK build require user's Windows environment.
