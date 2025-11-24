plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()

        maven {
            name = "TarsosDSP repository"
            url = uri("https://mvn.0110.be/releases")
        }
    }
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
    }
}
