// settings.gradle.kts

pluginManagement {
    repositories {
        google() // Wajib untuk menemukan Google Services dan Android Plugins
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    // Mode ini memaksa dependensi ditemukan di repositori yang dideklarasikan di sini.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google() // Wajib untuk Places SDK, Maps, dan Firebase
        mavenCentral()
    }
}

rootProject.name = "map_umkm"
include(":app")