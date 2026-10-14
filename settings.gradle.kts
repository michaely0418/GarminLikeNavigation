pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
// HOTFIX-003: Let Gradle automatically provision missing JDK 17 toolchains.
// This is a Settings plugin, not an Android app plugin.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "GarminLikeNavigation"
include(":app", ":core:model", ":core:interfaces", ":core:navigation", ":core:routing", ":core:safety", ":core:search", ":core:voice", ":ui:map", ":ui:hud", ":car:app")
