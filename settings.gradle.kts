@file:Suppress("ktlint")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

include(
    ":retrograde-util",
    ":retrograde-app-shared",
    ":lemuroid-touchinput",
    ":lemuroid-app",
    ":lemuroid-metadata-libretro-db",
    ":lemuroid-app-ext-free",
    ":lemuroid-app-ext-play",
    ":bundled-cores",
    ":baselineprofile"
)

project(":bundled-cores").projectDir = File("lemuroid-cores/bundled-cores")

// NOTE: The per-core Play Dynamic Feature Modules (lemuroid_core_*) have been removed.
// Cores are now downloaded at runtime directly from the RetroArch nightly buildbot:
//   https://buildbot.libretro.com/nightly/android/latest/{ABI}/{coreName}_libretro_android.so.zip
// This applies to both the free and Play (store) build variants.
