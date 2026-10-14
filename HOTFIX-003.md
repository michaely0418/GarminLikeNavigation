# HOTFIX-003 — Missing Java 17 toolchain on Windows

**Affected version:** v0.0.1-r2 (Android Studio JBR may run Gradle using Java 21, while every project Kotlin module uses `jvmToolchain(17)`).

## Symptom

`Cannot find a Java installation ... matching: {languageVersion=17}`

## Cause

Gradle starts successfully but the requested *compiler/test toolchain* Java 17 is not installed. `JAVA_HOME` pointing at Java 21 does not fulfill `jvmToolchain(17)`.

## Fix

`settings.gradle.kts` adds the officially supported Gradle Settings plugin:

```kotlin
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
```

Gradle can obtain a JDK 17 via Foojay automatically on the first build if network and repository policy allow it. This does not change the source/target compatibility, which remains Java 17. It also does not change the preexisting Java used to run Gradle.

## Validate

```powershell
cd F:\GarminLikeNavigation\GarminLikeNavigation
.\gradlew.bat --stop
.\gradlew.bat -q javaToolchains
.\gradlew.bat :core:model:test :app:assembleDebug
```

This is a **config-only change**. Toolchain download, Gradle tasks and APK are **not** validated in the user's Windows 11 machine. If the network blocks the Foojay endpoint, install JDK 17 locally and re-run. Use `gradlew -q javaToolchains` to verify detection.

Official docs: https://docs.gradle.org/8.13/userguide/toolchains.html and https://github.com/gradle/foojay-toolchains
