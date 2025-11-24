plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "it.progmob.esame1"
    compileSdk = 36   // SDK stabile e supportato

    defaultConfig {
        applicationId = "it.progmob.esame1"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
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

    // Disattivato perché NON usi Compose
    buildFeatures {
        compose = false
    }
}

dependencies {

    // AndroidX e UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // Firebase
    implementation(libs.firebase.auth)
  //  implementation("com.google.firebase:firebase-database:20.3.0")  non ho capito perchè non va bene
// Firebase BOM,risolve gestisce automaticamente versioni compatibili
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

// Firebase Auth
    implementation("com.google.firebase:firebase-auth")

// Firebase Realtime Database
    implementation("com.google.firebase:firebase-database")


    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
