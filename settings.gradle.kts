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
    ":ominoy-touchinput",
    ":ominoy-app",
    ":ominoy-metadata-libretro-db",
    ":ominoy-app-ext-free",
    ":bundled-cores",
    ":baselineprofile",
    ":libretrodroid"
)

project(":bundled-cores").projectDir = File("ominoy-cores/bundled-cores")
project(":libretrodroid").projectDir = File("third_party/libretrodroid/libretrodroid")
