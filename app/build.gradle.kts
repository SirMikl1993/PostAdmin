plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.postadmin"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.postadmin"
        minSdk = 29
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
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
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.litert.support.api)
    implementation(libs.androidx.room.ktx)
    implementation (libs.material3)
    testImplementation(libs.junit)
    implementation (libs.androidx.material.icons.extended)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

// Firebase Firestore
    implementation (libs.google.firebase.firestore.ktx)
// Firebase Storage
    implementation (libs.google.firebase.storage.ktx)
// Jetpack Compose and Kotlin dependencies
    implementation (libs.ui)
    implementation (libs.androidx.foundation)
    implementation (libs.androidx.material)
    implementation (libs.androidx.lifecycle.runtime.ktx.v231)
    implementation(libs.coil.compose.v132)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.auth)
}