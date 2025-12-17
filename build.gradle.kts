plugins {
    // Biarkan alias AGP (Android Gradle Plugin) apa adanya
    alias(libs.plugins.android.application) apply false

    // KRUSIAL: Ganti alias dengan ID dan versi spesifik yang stabil
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false

    id("com.google.gms.google-services") version "4.4.3" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.7.7" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // PERHATIAN: Hapus classpath untuk google-services.
        // Dengan KGP versi baru, `id("com.google.gms.google-services")`
        // di atas sudah cukup, dan duplikasi di buildscript dapat menyebabkan masalah.
        // classpath("com.google.gms:google-services:4.4.3") // HAPUS BARIS INI
        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.2") // optional
    }
}