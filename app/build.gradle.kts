plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "app.garminlike.nav"
    compileSdk = 35
    defaultConfig {
        minSdk = 26
        applicationId = "app.garminlike.nav"
        targetSdk = 35
        versionCode = 1
        versionName = "0.0.1-stage0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin { jvmToolchain(17) }

dependencies {
    implementation(project(":core:interfaces"))
    implementation(project(":core:model"))
    implementation(project(":core:navigation"))
    implementation(project(":core:routing"))
    implementation(project(":core:safety"))
    implementation(project(":core:search"))
    implementation(project(":core:voice"))
    implementation(project(":ui:map"))
    implementation(project(":ui:hud"))
    implementation(project(":car:app"))
}
