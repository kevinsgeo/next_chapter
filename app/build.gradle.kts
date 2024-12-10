// build.gradle.kts (app-level)

import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.cs407.next_chapter"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cs407.next_chapter"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Load secrets.properties
        val properties = Properties()
        val propertiesFile = rootProject.file("secrets.properties")
        if (propertiesFile.exists()) {
            properties.load(propertiesFile.inputStream())
        }

        // Set manifest placeholder for API key
        manifestPlaceholders["MAPS_API_KEY"] = properties.getProperty("MAPS_API_KEY", "")
    }

    buildTypes {
        release {
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.material3.lint)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.androidx.animation.core.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    // When using the BoM, you don't specify versions in Firebase library dependencies

    // Add the dependency for the Firebase SDK for Google Analytics
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")

    // Splash API
    implementation(libs.androidx.core.splashscreen)

    // Google Maps Dependencies
    implementation("com.google.android.gms:play-services-maps:18.1.0") // Google Maps SDK
    implementation("com.google.maps.android:maps-compose:2.11.3") // Maps Compose
    implementation("com.google.accompanist:accompanist-permissions:0.30.1") // For handling permissions

    // Accompanist Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.31.5-beta")

    // CameraX Dependencies
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0-alpha01")
    implementation("androidx.camera:camera-extensions:1.3.0-alpha01")

    // ML Kit Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.0.3")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    // JSON Parsing
    implementation("org.json:json:20210307")

    // *Added Dependency: Material Icons Extended*
    implementation("androidx.compose.material:material-icons-extended:1.4.3")
    //stream api
    val streamChat = "6.7.0"
    implementation("io.getstream:stream-chat-android-offline:$streamChat")
    implementation("io.getstream:stream-chat-android-compose:$streamChat")

    implementation("androidx.compose.material:material-icons-extended:1.6.0-alpha08")
}

apply(plugin = "com.google.gms.google-services")
