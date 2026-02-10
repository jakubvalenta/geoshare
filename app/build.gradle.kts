plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt.gradle)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(21)
}

android {
    namespace = "page.ooooo.geoshare"
    compileSdk = 36
    compileSdkMinor = 1

    defaultConfig {
        applicationId = "page.ooooo.geoshare"
        minSdk = 25
        // noinspection EditedTargetSdkVersion
        targetSdk = 36
        versionCode = 36
        versionName = "5.16.0"

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
                "it",
                "pl",
                "pt-rBR",
                "pt-rPT",
                "ru",
                "uk",
                "zh-rCN",
                "zh-rTW",
            )
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // The following argument makes the Android Test Orchestrator run its "pm clear" command after each test
        // invocation. This command ensures that the app's state is completely cleared between tests.
        testInstrumentationRunnerArguments += mapOf("clearPackageData" to "true")
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
        create("paid") {
            dimension = "tier"
            versionNameSuffix = "-paid"
        }
        create("demo") {
            dimension = "tier"
            versionNameSuffix = "-demo"
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
    }
}

dependencies {

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

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
    implementation(libs.hilt.android)
    implementation(libs.jts.core)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.mock)
    ksp(libs.hilt.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)

    "paidImplementation"(libs.android.billingclient.billing)
    "paidImplementation"(libs.android.billingclient.billing.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.kotlin)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.uiautomator)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestUtil(libs.androidx.test.orchestrator)
}
