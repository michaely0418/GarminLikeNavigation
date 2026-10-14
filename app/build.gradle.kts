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
        // CI uses the monotonically increasing GitHub Actions run number.
        // Local Android Studio builds intentionally keep their independent debug code.
        versionCode = providers.gradleProperty("ciVersionCode").orNull?.toIntOrNull() ?: 2
        versionName = "0.1.0-stage1"
    }

    // Never commit a signing private key. A dedicated NON-PRODUCTION test key
    // is injected by GitHub Actions from repository secrets when configured.
    signingConfigs {
        val keystorePath = providers.environmentVariable("CI_TEST_KEYSTORE_FILE").orNull
        val password = providers.environmentVariable("CI_TEST_KEYSTORE_PASSWORD").orNull
        if (!keystorePath.isNullOrBlank() && !password.isNullOrBlank()) {
            create("stableCiDebug") {
                storeFile = file(keystorePath)
                storePassword = password
                keyAlias = "navtest"
                keyPassword = password
                storeType = "pkcs12"
            }
        }
    }
    buildTypes {
        getByName("debug") {
            if (signingConfigs.names.contains("stableCiDebug")) {
                signingConfig = signingConfigs.getByName("stableCiDebug")
            }
        }
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
