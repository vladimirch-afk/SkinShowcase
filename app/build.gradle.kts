plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "ru.kotlix.skinshowcase"
    compileSdk = Config.compileSdk

    defaultConfig {
        applicationId = "ru.kotlix.skinshowcase"
        minSdk = Config.minSdk
        targetSdk = Config.targetSdk
        versionCode = Config.versionCode
        versionName = Config.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    buildTypes {
        debug {
            buildConfigField("String", "MESSAGING_DEBUG_TOKEN", "\"\"")
            buildConfigField("boolean", "USE_MOCK_SERVER", "true")
            buildConfigField(
                "String",
                "APPMETRICA_API_KEY",
                "\"${project.findProperty("APPMETRICA_API_KEY") ?: ""}\""
            )
        }
        release {
            buildConfigField("boolean", "USE_MOCK_SERVER", "false")
            buildConfigField("String", "MESSAGING_DEBUG_TOKEN", "\"\"")
            buildConfigField(
                "String",
                "APPMETRICA_API_KEY",
                "\"${project.findProperty("APPMETRICA_API_KEY") ?: ""}\""
            )
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":designsystem"))
    implementation(project(":message"))
    implementation(project(":mock"))
    implementation(project(":onboarding"))

    implementation(libs.androidx.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.animation) // slideIntoContainer, slideOutOfContainer
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.coil.compose)
    implementation(libs.appmetrica.analytics)
    implementation(libs.appmetrica.push)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.firebase.messaging)
    implementation(libs.play.services.base)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}