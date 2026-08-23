import com.android.build.api.dsl.ManagedVirtualDevice

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt.gradle)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.aboutlibraries)
}

kotlin {
    jvmToolchain(21)
}

android {
    namespace = "page.ooooo.geoshare"
    compileSdk = 37

    defaultConfig {
        applicationId = "page.ooooo.geoshare"
        minSdk = 25
        // noinspection EditedTargetSdkVersion
        targetSdk = 37
        versionCode = 47
        versionName = "6.7.0"

        androidResources {
            @Suppress("UnstableApiUsage")
            localeFilters += listOf(
                "ar",
                "bg",
                "cs",
                "de",
                "en",
                "es",
                "fr",
                "pt-rBR",
                "pt-rPT",
                "ru",
                "tr",
                "uk",
                "zh-rCN",
                "zh-rTW",
            )
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments += mapOf(
            // Clear app state between tests
            "clearPackageData" to "true",
            // Include only normal tests, not screenshots
            "package" to "page.ooooo.geoshare.tests",
        )
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
    }
    flavorDimensions += "tier"
    productFlavors {
        create("free") {
            isDefault = true
            dimension = "tier"
        }
        create("pro") {
            dimension = "tier"
            applicationIdSuffix = ".pro"
        }
        create("demo") {
            dimension = "tier"
            applicationIdSuffix = ".demo"
        }
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    lint {
        disable += "MissingTranslation" // Translation is crowdsourced, so this isn't viable
    }
    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        @Suppress("UnstableApiUsage")
        managedDevices {
            localDevices {
                create("mediumPhoneApi37") {
                    device = "Medium Phone"
                    apiLevel = 37
                    systemImageSource = "google"
                    testedAbi = "x86_64" // Set to suppress warn
                    pageAlignment = ManagedVirtualDevice.PageAlignment.FORCE_4KB_PAGES // Set to suppress warn
                }
            }
        }
    }
}

room {
    // Enable room auto-migrations.
    schemaDirectory("$projectDir/schemas")
}

aboutLibraries {
    collect {
        configPath = file("./config")
    }
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)

    implementation(libs.aboutlibraries.compose.m3)
    implementation(libs.accompanist.drawableplainter)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.testing)
    implementation(libs.hilt.android)
    implementation(libs.jts.core)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.contentnegotiation)
    implementation(libs.ktor.client.mock)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.openlocationcode)
    ksp(libs.androidx.room.compiler)
    ksp(libs.hilt.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)

    "proImplementation"(libs.android.billingclient.billing)
    "proImplementation"(libs.android.billingclient.billing.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.kotlin)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.uiautomator)
    androidTestImplementation(libs.fastlane.screengrab)
    androidTestUtil(libs.androidx.test.orchestrator)
}

tasks.register<Copy>("copyScreenshots") {
    group = "verification"
    description = "Copies screenshots from instrument test outputs to docs/screenshots"

    @Suppress("UnstableApiUsage")
    val deviceNames = android.testOptions.managedDevices.localDevices.names

    for (flavor in android.productFlavors.names) {
        for (device in deviceNames) {
            val path = "outputs/managed_device_android_test_additional_output/debug/flavors/$flavor/$device"
            from(layout.buildDirectory.dir(path))
        }
    }
    into("$rootDir/docs/screenshots")
    include("**/*.webp")
}
