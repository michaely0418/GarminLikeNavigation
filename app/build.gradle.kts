plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "app.garminlike.nav"
    compileSdk = 35
    defaultConfig {
        minSdk = 26
        applicationId = "app.garminlike.nav"
        targetSdk = 35
        versionCode = 2
        versionName = "0.1.0-stage1"
    }
    buildFeatures { compose = true }
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
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)
}
