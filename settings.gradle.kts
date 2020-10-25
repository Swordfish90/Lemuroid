include(
    ":retrograde-util",
    ":retrograde-app-shared",
    ":lemuroid-touchinput",
    ":lemuroid-app",
    ":lemuroid-metadata-libretro-db",
    ":lemuroid-app-ext-free",
    ":lemuroid-app-ext-play",
    ":lemuroid_core_gambatte",
    ":lemuroid_core_desmume",
    ":lemuroid_core_fbneo",
    ":lemuroid_core_fceumm",
    ":lemuroid_core_genesis_plus_gx",
    ":lemuroid_core_mgba",
    ":lemuroid_core_mupen64plus_next",
    ":lemuroid_core_pcsx_rearmed",
    ":lemuroid_core_ppsspp",
    ":lemuroid_core_snes9x",
    ":lemuroid_core_stella"
)

project(":lemuroid_core_gambatte").projectDir = File("lemuroid-cores/lemuroid_core_gambatte")
project(":lemuroid_core_desmume").projectDir = File("lemuroid-cores/lemuroid_core_desmume")
project(":lemuroid_core_fbneo").projectDir = File("lemuroid-cores/lemuroid_core_fbneo")
project(":lemuroid_core_fceumm").projectDir = File("lemuroid-cores/lemuroid_core_fceumm")
project(":lemuroid_core_genesis_plus_gx").projectDir = File("lemuroid-cores/lemuroid_core_genesis_plus_gx")
project(":lemuroid_core_mgba").projectDir = File("lemuroid-cores/lemuroid_core_mgba")
project(":lemuroid_core_mupen64plus_next").projectDir = File("lemuroid-cores/lemuroid_core_mupen64plus_next")
project(":lemuroid_core_pcsx_rearmed").projectDir = File("lemuroid-cores/lemuroid_core_pcsx_rearmed")
project(":lemuroid_core_ppsspp").projectDir = File("lemuroid-cores/lemuroid_core_ppsspp")
project(":lemuroid_core_snes9x").projectDir = File("lemuroid-cores/lemuroid_core_snes9x")
project(":lemuroid_core_stella").projectDir = File("lemuroid-cores/lemuroid_core_stella")
