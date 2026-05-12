@file:Suppress("ktlint")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

include(
    ":retrograde-util",
    ":retrograde-app-shared",
    ":lemuroid-touchinput",
    ":lemuroid-app",
    ":lemuroid-metadata-libretro-db",
    ":lemuroid-app-ext-free",
    ":bundled-cores",
    ":bundled-cores-pro",
    ":baselineprofile"
)

project(":bundled-cores").projectDir = File("lemuroid-cores/bundled-cores")
project(":bundled-cores-pro").projectDir = File("lemuroid-cores/bundled-cores-pro")
