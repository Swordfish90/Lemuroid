/*
 *     Copyright (C) 2019  Filippo Scognamiglio
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

#include <jni.h>

#include <EGL/egl.h>

#include <string>
#include <utility>
#include <vector>
#include <unordered_set>
#include <sstream>

#include "libretrodroid.h"
#include "utils/libretrodroidexception.h"
#include "log.h"
#include "core.h"
#include "audio.h"
#include "video.h"
#include "renderers/renderer.h"
#include "fpssync.h"
#include "input.h"
#include "rumble.h"
#include "shadermanager.h"
#include "utils/javautils.h"
#include "environment.h"
#include "renderers/es3/framebufferrenderer.h"
#include "renderers/es2/imagerendereres2.h"
#include "renderers/es3/imagerendereres3.h"
#include "utils/utils.h"
#include "utils/rect.h"
#include "errorcodes.h"
#include "vfs/vfs.h"
#include "rc_consoles.h"
#include <atomic>
#include <deque>
#include <unordered_map>

namespace {
JavaVM* g_raJavaVm = nullptr;
std::atomic<uint64_t> g_raNextRequestId{1};
struct RaPendingRequest {
    rc_client_server_callback_t callback;
    void* callbackData;
};
struct RaServerResponse {
    uint64_t requestId;
    int statusCode;
    std::string body;
};
std::mutex g_raResponseMutex;
std::unordered_map<uint64_t, RaPendingRequest> g_raPendingRequests;
std::deque<RaServerResponse> g_raResponses;

JNIEnv* raGetEnv(bool attach) {
    if (!g_raJavaVm) return nullptr;
    JNIEnv* env = nullptr;
    if (g_raJavaVm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) == JNI_OK) return env;
    if (!attach || g_raJavaVm->AttachCurrentThread(&env, nullptr) != JNI_OK) return nullptr;
    return env;
}

void raNotifyAndroid(int type, const char* title, const char* detail, int points = 0, const char* badgeUrl = "",
                     const char* gameHash = "", uint32_t achievementId = 0, int64_t unlockTimeMillis = 0) {
    JNIEnv* env = raGetEnv(true);
    if (!env) return;
    jclass bridge = env->FindClass("com/swordfish/libretrodroid/RetroAchievementsNativeBridge");
    if (!bridge) { env->ExceptionClear(); return; }
    jmethodID method = env->GetStaticMethodID(bridge, "onNativeEvent", "(ILjava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;JJ)V");
    if (method) {
        jstring jTitle = env->NewStringUTF(title ? title : "");
        jstring jDetail = env->NewStringUTF(detail ? detail : "");
        jstring jBadgeUrl = env->NewStringUTF(badgeUrl ? badgeUrl : "");
        jstring jGameHash = env->NewStringUTF(gameHash ? gameHash : "");
        env->CallStaticVoidMethod(bridge, method, type, jTitle, jDetail, points, jBadgeUrl, jGameHash,
                                  static_cast<jlong>(achievementId), static_cast<jlong>(unlockTimeMillis));
        env->DeleteLocalRef(jTitle);
        env->DeleteLocalRef(jDetail);
        env->DeleteLocalRef(jBadgeUrl);
        env->DeleteLocalRef(jGameHash);
    }
    if (env->ExceptionCheck()) env->ExceptionClear();
    env->DeleteLocalRef(bridge);
}

void raServerCall(const rc_api_request_t* request, rc_client_server_callback_t callback,
                  void* callbackData, rc_client_t*) {
    const uint64_t requestId = g_raNextRequestId.fetch_add(1);
    {
        std::lock_guard<std::mutex> lock(g_raResponseMutex);
        g_raPendingRequests.emplace(requestId, RaPendingRequest{callback, callbackData});
    }
    JNIEnv* env = raGetEnv(true);
    if (!env) return;
    jclass bridge = env->FindClass("com/swordfish/libretrodroid/RetroAchievementsNativeBridge");
    if (!bridge) { env->ExceptionClear(); return; }
    jmethodID method = env->GetStaticMethodID(bridge, "request", "(JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    if (method) {
        jstring url = env->NewStringUTF(request->url ? request->url : "");
        jstring post = request->post_data ? env->NewStringUTF(request->post_data) : nullptr;
        jstring contentType = request->content_type ? env->NewStringUTF(request->content_type) : nullptr;
        env->CallStaticVoidMethod(bridge, method, static_cast<jlong>(requestId), url, post, contentType);
        env->DeleteLocalRef(url);
        if (post) env->DeleteLocalRef(post);
        if (contentType) env->DeleteLocalRef(contentType);
    }
    if (env->ExceptionCheck()) env->ExceptionClear();
    env->DeleteLocalRef(bridge);
}

void raEventHandler(const rc_client_event_t* event, rc_client_t* client) {
    if (!event) return;
    const char* title = "";
    const char* detail = "";
    const char* badgeUrl = "";
    int points = 0;
    char badgeUrlBuffer[512] = {};
    if (event->achievement) {
        title = event->achievement->title;
        detail = event->achievement->description;
        points = static_cast<int>(event->achievement->points);
        badgeUrl = event->achievement->badge_url;
        if (!badgeUrl || !badgeUrl[0]) {
            if (rc_client_achievement_get_image_url(event->achievement, RC_CLIENT_ACHIEVEMENT_STATE_UNLOCKED,
                                                    badgeUrlBuffer, sizeof(badgeUrlBuffer)) == RC_OK) {
                badgeUrl = badgeUrlBuffer;
            }
        }
    } else if (event->server_error) {
        detail = event->server_error->error_message;
    }
    const rc_client_game_t* game = client ? rc_client_get_game_info(client) : nullptr;
    const char* gameHash = game && game->hash ? game->hash : "";
    const uint32_t achievementId = event->achievement ? event->achievement->id : 0;
    const int64_t unlockTimeMillis = event->achievement ? static_cast<int64_t>(event->achievement->unlock_time) * 1000 : 0;
    raNotifyAndroid(static_cast<int>(event->type), title, detail, points, badgeUrl,
                    gameHash, achievementId, unlockTimeMillis);
}

void raGameLoadCallback(int result, const char* errorMessage, rc_client_t* client, void*) {
    if (result == RC_OK) {
        const rc_client_game_t* game = rc_client_get_game_info(client);
        const std::string detail = game ? std::to_string(game->id) + "|" + (game->title ? game->title : "Game recognized") : "0|Game recognized";
        raNotifyAndroid(102, "", detail.c_str());
    } else {
        if (result == RC_NOT_FOUND) raNotifyAndroid(104, "", "No achievement set found for this ROM");
        else raNotifyAndroid(103, "", errorMessage ? errorMessage : "Could not load achievement set");
    }
}

void raLoginCallback(int result, const char* errorMessage, rc_client_t* client, void*) {
    if (result != RC_OK) {
        raNotifyAndroid(101, "", errorMessage ? errorMessage : "RetroAchievements login failed");
        return;
    }
    raNotifyAndroid(100, "", "Connected to RetroAchievements");
    auto& app = libretrodroid::LibretroDroid::getInstance();
    const std::string hash = app.raGameHashForLoad();
    if (hash.empty()) {
        raNotifyAndroid(103, "", "ROM hash is unavailable");
        return;
    }
    rc_client_begin_load_game(client, hash.c_str(), raGameLoadCallback, nullptr);
}

void processRaResponses() {
    std::deque<RaServerResponse> ready;
    {
        std::lock_guard<std::mutex> lock(g_raResponseMutex);
        ready.swap(g_raResponses);
    }
    for (const auto& response : ready) {
        RaPendingRequest pending{};
        {
            std::lock_guard<std::mutex> lock(g_raResponseMutex);
            auto found = g_raPendingRequests.find(response.requestId);
            if (found == g_raPendingRequests.end()) continue;
            pending = found->second;
            g_raPendingRequests.erase(found);
        }
        rc_api_server_response_t result{};
        result.http_status_code = response.statusCode;
        result.body = response.body.data();
        result.body_length = response.body.size();
        pending.callback(&result, pending.callbackData);
    }
}
}

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
    g_raJavaVm = vm;
    return JNI_VERSION_1_6;
}

namespace libretrodroid {

static void callback_get_core_memory_info(uint32_t id, rc_libretro_core_memory_info_t* info) {
    LibretroDroid::getInstance().getCoreMemoryInfo(id, info);
}

uint32_t LibretroDroid::callback_ra_read_memory(
    uint32_t address,
    uint8_t* buffer,
    uint32_t num_bytes,
    rc_client_t*
) {
    auto& instance = LibretroDroid::getInstance();
    return rc_libretro_memory_read(&instance.raMemoryRegions, address, buffer, num_bytes);
}

uintptr_t LibretroDroid::callback_get_current_framebuffer() {
    return LibretroDroid::getInstance().handleGetCurrentFrameBuffer();
}

void LibretroDroid::callback_hw_video_refresh(
    const void *data,
    unsigned width,
    unsigned height,
    size_t pitch
) {
    LOGD("hw video refresh callback called %i %i", width, height);
    LibretroDroid::getInstance().handleVideoRefresh(data, width, height, pitch);
}

void LibretroDroid::callback_audio_sample(int16_t left, int16_t right) {
    LOGE("callback audio sample (left, right) has been called");
}

size_t LibretroDroid::callback_set_audio_sample_batch(const int16_t *data, size_t frames) {
    return LibretroDroid::getInstance().handleAudioCallback(data, frames);
}

void LibretroDroid::callback_retro_set_input_poll() {
    // Do nothing in here...
}

int16_t LibretroDroid::callback_set_input_state(
    unsigned int port,
    unsigned int device,
    unsigned int index,
    unsigned int id
) {
    return LibretroDroid::getInstance().handleSetInputState(port, device, index, id);
}

void LibretroDroid::updateAudioSampleRateMultiplier() {
    if (audio) {
        audio->setPlaybackSpeed(frameSpeed);
    }
}

// TODO... Do we really need this?
void LibretroDroid::resetGlobalVariables() {
    core = nullptr;
    audio = nullptr;
    video = nullptr;
    fpsSync = nullptr;
    input = nullptr;
    rumble = nullptr;
}

int LibretroDroid::availableDisks() {
    return Environment::getInstance().getRetroDiskControlCallback() != nullptr
           ? Environment::getInstance().getRetroDiskControlCallback()->get_num_images()
           : 0;
}

int LibretroDroid::currentDisk() {
    return Environment::getInstance().getRetroDiskControlCallback() != nullptr
           ? Environment::getInstance().getRetroDiskControlCallback()->get_image_index()
           : 0;
}

void LibretroDroid::changeDisk(unsigned int index) {
    if (Environment::getInstance().getRetroDiskControlCallback() == nullptr) {
        LOGE("Cannot swap disk. This platform does not support it.");
        return;
    }

    if (index < 0 || index >= Environment::getInstance().getRetroDiskControlCallback()->get_num_images()) {
        LOGE("Requested image index is not valid.");
        return;
    }

    if (Environment::getInstance().getRetroDiskControlCallback()->get_image_index() != index) {
        Environment::getInstance().getRetroDiskControlCallback()->set_eject_state(true);
        Environment::getInstance().getRetroDiskControlCallback()->set_image_index((unsigned) index);
        Environment::getInstance().getRetroDiskControlCallback()->set_eject_state(false);
    }
}

void LibretroDroid::updateVariable(const Variable& variable) {
    Environment::getInstance().updateVariable(variable.key, variable.value);
}

std::vector<Variable> LibretroDroid::getVariables() {
    return Environment::getInstance().getVariables();
}

std::vector<std::vector<struct Controller>> LibretroDroid::getControllers() {
    return Environment::getInstance().getControllers();
}

void LibretroDroid::setControllerType(unsigned int port, unsigned int type) {
    core->retro_set_controller_port_device(port, type);
}

bool LibretroDroid::unserializeState(int8_t *data, size_t size) {
    std::lock_guard<std::mutex> lock(coreLock);

    return core->retro_unserialize(data, size);
}

JNIEXPORT jboolean JNICALL LibretroDroid::unserializeSRAM(int8_t* data, size_t size) {
    std::lock_guard<std::mutex> lock(coreLock);

    size_t sramSize = core->retro_get_memory_size(RETRO_MEMORY_SAVE_RAM);
    void *sramState = core->retro_get_memory_data(RETRO_MEMORY_SAVE_RAM);

    if (sramState == nullptr) {
        LOGE("Cannot load SRAM: nullptr in retro_get_memory_data");
        return false;
    }

    if (size > sramSize) {
        LOGE("Cannot load SRAM: size mismatch");
        return false;
    }

    memcpy(sramState, data, size);

    return true;
}

std::pair<int8_t*, size_t> LibretroDroid::serializeSRAM() {
    std::lock_guard<std::mutex> lock(coreLock);

    size_t size = core->retro_get_memory_size(RETRO_MEMORY_SAVE_RAM);
    auto* data = new int8_t[size];
    memcpy(data, (int8_t*) core->retro_get_memory_data(RETRO_MEMORY_SAVE_RAM), size);

    return std::pair(data, size);
}

void LibretroDroid::onSurfaceChanged(unsigned int width, unsigned int height) {
    LOGD("Performing libretrodroid onSurfaceChanged");
    video->updateScreenSize(width, height);
}

void LibretroDroid::onSurfaceCreated() {
    LOGD("Performing libretrodroid onSurfaceCreated");

    struct retro_system_av_info system_av_info {};
    core->retro_get_system_av_info(&system_av_info);

    video = nullptr;

    Video::RenderingOptions renderingOptions {
        Environment::getInstance().isUseHwAcceleration(),
        system_av_info.geometry.base_width,
        system_av_info.geometry.base_height,
        Environment::getInstance().isUseDepth(),
        Environment::getInstance().isUseStencil(),
        openglESVersion,
        Environment::getInstance().getPixelFormat()
    };

    auto newVideo = new Video(
        renderingOptions,
        fragmentShaderConfig,
        Environment::getInstance().isBottomLeftOrigin(),
        Environment::getInstance().getScreenRotation(),
        skipDuplicateFrames,
        immersiveModeEnabled,
        viewportRect,
        immersiveModeConfig,
        viewportAlignment
    );

    video = std::unique_ptr<Video>(newVideo);

    if (Environment::getInstance().getHwContextReset() != nullptr) {
        Environment::getInstance().getHwContextReset()();
    }
}

void LibretroDroid::onMotionEvent(
    unsigned int port,
    unsigned int source,
    float xAxis,
    float yAxis
) {
    LOGD("Received motion event: %d %.2f, %.2f", source, xAxis, yAxis);
    if (input) {
        input->onMotionEvent(port, source, xAxis, yAxis);
    }
}

void LibretroDroid::onTouchEvent(float xAxis, float yAxis) {
    LOGD("Received touch event: %.2f, %.2f", xAxis, yAxis);
    if (input && video) {
        auto [x, y] = video->getLayout().getRelativePosition(xAxis, yAxis);
        input->onMotionEvent(0, Input::MOTION_SOURCE_POINTER, x, y);
    }
}

void LibretroDroid::onKeyEvent(unsigned int port, int action, int keyCode) {
    LOGD("Received key event with action (%d) and keycode (%d)", action, keyCode);
    if (input) {
        input->onKeyEvent(port, action, keyCode);
    }
}

void LibretroDroid::create(
    unsigned int GLESVersion,
    const std::string& soFilePath,
    const std::string& systemDir,
    const std::string& savesDir,
    std::vector<Variable> variables,
    const ShaderManager::Config& shaderConfig,
    float refreshRate,
    bool lowLatencyAudio,
    bool enableVirtualFileSystem,
    bool enableMicrophone,
    bool duplicateFrames,
    std::optional<ImmersiveMode::Config> immersiveModeConfig,
    const std::string& language,
    uint32_t retroAchievementsConsoleId
) {
    LOGD("Performing libretrodroid create");

    resetGlobalVariables();
    raConsoleId = retroAchievementsConsoleId;
    destroyRaClient();
    rc_libretro_memory_destroy(&raMemoryRegions);
    raMemoryMapInitialized = false;

    Environment::getInstance().initialize(systemDir, savesDir, &callback_get_current_framebuffer);
    Environment::getInstance().setLanguage(language);
    Environment::getInstance().setEnableVirtualFileSystem(enableVirtualFileSystem);
    Environment::getInstance().setEnableMicrophone(enableMicrophone);

    openglESVersion = GLESVersion;
    screenRefreshRate = refreshRate;
    skipDuplicateFrames = duplicateFrames;
    immersiveModeEnabled = GLESVersion >= 3 && immersiveModeConfig.has_value();
    this->immersiveModeConfig = immersiveModeConfig.value_or(ImmersiveMode::Config{});
    audioEnabled = true;
    frameSpeed = 1;

    core = std::make_unique<Core>(soFilePath);

    core->retro_set_video_refresh(&callback_hw_video_refresh);
    core->retro_set_environment(&Environment::callback_environment);
    core->retro_set_audio_sample(&callback_audio_sample);
    core->retro_set_audio_sample_batch(&callback_set_audio_sample_batch);
    core->retro_set_input_poll(&callback_retro_set_input_poll);
    core->retro_set_input_state(&callback_set_input_state);

    std::for_each(variables.begin(), variables.end(), [&](const Variable& v) {
        updateVariable(v);
    });

    core->retro_init();

    preferLowLatencyAudio = lowLatencyAudio;

    // HW accelerated cores are only supported on opengles 3.
    if (Environment::getInstance().isUseHwAcceleration() && openglESVersion < 3) {
        throw LibretroDroidError("OpenGL ES 3 is required for this Core", ERROR_GL_NOT_COMPATIBLE);
    }

    fragmentShaderConfig = shaderConfig;

    rumble = std::make_unique<Rumble>();
}

void LibretroDroid::loadGameFromPath(const std::string& gamePath) {
    LOGD("Performing libretrodroid loadGameFromPath");
    struct retro_system_info system_info {};
    core->retro_get_system_info(&system_info);

    struct retro_game_info game_info {};
    game_info.path = Utils::cloneToCString(gamePath);
    game_info.meta = nullptr;

    if (system_info.need_fullpath) {
        game_info.data = nullptr;
        game_info.size = 0;
    } else {
        struct Utils::ReadResult file = Utils::readFileAsBytes(gamePath);
        game_info.data = file.data;
        game_info.size = file.size;
    }

    bool result = core->retro_load_game(&game_info);
    if (!result) {
        LOGE("Cannot load game. Leaving.");
        throw std::runtime_error("Cannot load game");
    }

    afterGameLoad();
}

void LibretroDroid::loadGameFromBytes(const int8_t *data, size_t size) {
    LOGD("Performing libretrodroid loadGameFromBytes");

    struct retro_system_info system_info {};
    core->retro_get_system_info(&system_info);

    struct retro_game_info game_info {};
    game_info.path = nullptr;
    game_info.meta = nullptr;

    if (system_info.need_fullpath) {
        game_info.data = nullptr;
        game_info.size = 0;
    } else {
        game_info.data = data;
        game_info.size = size;
    }

    bool result = core->retro_load_game(&game_info);
    if (!result) {
        LOGE("Cannot load game. Leaving.");
        throw std::runtime_error("Cannot load game");
    }

    afterGameLoad();
}

void LibretroDroid::loadGameFromVirtualFiles(std::vector<VFSFile> virtualFiles) {
    LOGD("Performing libretrodroid loadGameFromVirtualFiles");
    struct retro_system_info system_info {};
    core->retro_get_system_info(&system_info);

    if (virtualFiles.empty()) {
        LOGE("Calling loadGameFromVirtualFiles without any file.");
        throw std::runtime_error("Calling loadGameFromVirtualFiles without any file.");
    }

    std::string firstFilePath = virtualFiles[0].getFileName();
    int firstFileFD = virtualFiles[0].getFD();

    bool loadUsingVFS = system_info.need_fullpath || virtualFiles.size() > 1;

    struct retro_game_info game_info {};
    game_info.path = Utils::cloneToCString(firstFilePath);
    game_info.meta = nullptr;

    if (loadUsingVFS) {
        VFS::getInstance().initialize(std::move(virtualFiles));
    }

    if (loadUsingVFS) {
        game_info.data = nullptr;
        game_info.size = 0;
    } else {
        struct Utils::ReadResult file = Utils::readFileAsBytes(firstFileFD);
        game_info.data = file.data;
        game_info.size = file.size;
    }

    bool result = core->retro_load_game(&game_info);
    if (!result) {
        LOGE("Cannot load game. Leaving.");
        throw std::runtime_error("Cannot load game");
    }

    afterGameLoad();
}

void LibretroDroid::destroy() {
    std::lock_guard<std::mutex> lock(coreLock);

    LOGD("Performing libretrodroid destroy");

    if (Environment::getInstance().getHwContextDestroy() != nullptr) {
        Environment::getInstance().getHwContextDestroy()();
    }

    destroyRaClient();
    raUsername.clear();
    raToken.clear();
    raGameHash.clear();
    rc_libretro_memory_destroy(&raMemoryRegions);
    raMemoryMapInitialized = false;
    core->retro_unload_game();
    core->retro_deinit();

    video = nullptr;
    core = nullptr;
    rumble = nullptr;
    fpsSync = nullptr;
    audio = nullptr;

    Environment::getInstance().deinitialize();
    VFS::getInstance().deinitialize();
}

void LibretroDroid::resume() {
    LOGD("Performing libretrodroid resume");

    input = std::make_unique<Input>();

    fpsSync->reset();
    audio->start();
    refreshAspectRatio();
}

void LibretroDroid::pause() {
    LOGD("Performing libretrodroid pause");
    audio->stop();

    input = nullptr;
}

void LibretroDroid::step() {
    std::lock_guard<std::mutex> lock(coreLock);

    processRaResponses();

    LOGD("Stepping into retro_run()");

    unsigned frames = 1;
    if (fpsSync) {
        unsigned requestedFrames = fpsSync->advanceFrames();

        // If the application runs too slow it's better to just skip those frames.
        frames = std::min(requestedFrames, 2u);
    }

    for (size_t i = 0; i < frames * frameSpeed; i++)
        core->retro_run();

    if (raConsoleId != 0 && !raMemoryMapRetriedAfterFirstFrame) {
        initializeRaMemoryMap("post_first_frame");
        raMemoryMapRetriedAfterFirstFrame = true;
    }

    if (raClient != nullptr && raMemoryMapInitialized) {
        for (size_t i = 0; i < frames * frameSpeed; ++i) {
            // Softcore is explicit until all integrity restrictions are implemented and tested.
            rc_client_do_frame(raClient);
        }
    }

    processRaResponses();

    if (video && !video->rendersInVideoCallback()) {
        video->renderFrame();
    }

    if (fpsSync) {
        fpsSync->wait();
    }

    if (rumble && rumbleEnabled) {
        rumble->fetchFromEnvironment();
    }

    // Some games override the core geometry at runtime. These fields get updated in retro_run().
    if (video && Environment::getInstance().isGameGeometryUpdated()) {
        Environment::getInstance().clearGameGeometryUpdated();

        video->updateRendererSize(
            Environment::getInstance().getGameGeometryWidth(),
            Environment::getInstance().getGameGeometryHeight()
        );

        dirtyVideo = true;
    }

    if (video && Environment::getInstance().isScreenRotationUpdated()) {
        Environment::getInstance().clearScreenRotationUpdated();

        video->updateRotation(Environment::getInstance().getScreenRotation());
    }
}

float LibretroDroid::getAspectRatio() {
    float gameAspectRatio = Environment::getInstance().retrieveGameSpecificAspectRatio();
    return gameAspectRatio > 0 ? gameAspectRatio : defaultAspectRatio;
}

void LibretroDroid::refreshAspectRatio() {
    video->updateAspectRatio(getAspectRatio());
}

void LibretroDroid::setRumbleEnabled(bool enabled) {
    rumbleEnabled = enabled;
}

bool LibretroDroid::isRumbleEnabled() const {
    return rumbleEnabled;
}

void LibretroDroid::setFrameSpeed(unsigned int speed) {
    frameSpeed = speed;
    updateAudioSampleRateMultiplier();
}

void LibretroDroid::setAudioEnabled(bool enabled) {
    audioEnabled = enabled;
}

void LibretroDroid::setShaderConfig(ShaderManager::Config shaderConfig) {
    fragmentShaderConfig = std::move(shaderConfig);
    if (video) {
        video->updateShaderType(fragmentShaderConfig);
    }
}

void LibretroDroid::handleVideoRefresh(
    const void *data,
    unsigned int width,
    unsigned int height,
    size_t pitch
) {
    if (video) {
        video->onNewFrame(data, width, height, pitch);

        if (video->rendersInVideoCallback()) {
            video->renderFrame();
        }
    }
}

size_t LibretroDroid::handleAudioCallback(const int16_t *data, size_t frames) {
    if (audio && audioEnabled) {
        audio->write(data, frames);
    }
    return frames;
}

int16_t LibretroDroid::handleSetInputState(
    unsigned int port,
    unsigned int device,
    unsigned int index,
    unsigned int id
) {
    if (input) {
        return input->getInputState(port, device, index, id);
    }
    return 0;
}

uintptr_t LibretroDroid::handleGetCurrentFrameBuffer() {
    if (video) {
        return video->getCurrentFramebuffer();
    }
    return 0;
}

void LibretroDroid::reset() {
    std::lock_guard<std::mutex> lock(coreLock);

    core->retro_reset();
}

std::pair<int8_t*, size_t> LibretroDroid::serializeState() {
    std::lock_guard<std::mutex> lock(coreLock);

    size_t size = core->retro_serialize_size();
    auto data = new int8_t[size];

    core->retro_serialize(data, size);

    return std::pair(data, size);
}

void LibretroDroid::resetCheat() {
    std::lock_guard<std::mutex> lock(coreLock);

    core->retro_cheat_reset();
}

void LibretroDroid::setCheat(unsigned index, bool enabled, const std::string& code) {
    std::lock_guard<std::mutex> lock(coreLock);

    core->retro_cheat_set(index, enabled, Utils::cloneToCString(code));
}

bool LibretroDroid::requiresVideoRefresh() const {
    return dirtyVideo;
}

void LibretroDroid::clearRequiresVideoRefresh() {
    dirtyVideo = false;
}

void LibretroDroid::afterGameLoad() {
    raMemoryMapRetriedAfterFirstFrame = false;
    initializeRaMemoryMap("post_load");

    struct retro_system_av_info system_av_info {};
    core->retro_get_system_av_info(&system_av_info);

    fpsSync = std::make_unique<FPSSync>(system_av_info.timing.fps, screenRefreshRate);

    double inputSampleRate = system_av_info.timing.sample_rate * fpsSync->getTimeStretchFactor();

    audio = std::make_unique<Audio>(
        (int32_t) std::lround(inputSampleRate),
        system_av_info.timing.fps,
        preferLowLatencyAudio
    );

    updateAudioSampleRateMultiplier();

    defaultAspectRatio = findDefaultAspectRatio(system_av_info);
}

void LibretroDroid::initializeRaMemoryMap(const char* phase) {
    rc_libretro_memory_destroy(&raMemoryRegions);
    raMemoryMapInitialized = false;
    if (raConsoleId == 0) return;

    const auto* memoryMap = Environment::getInstance().getRetroMemoryMap();
    raMemoryMapInitialized = rc_libretro_memory_init(
        &raMemoryRegions,
        memoryMap,
        &callback_get_core_memory_info,
        raConsoleId
    ) != 0;
    if (raMemoryMapInitialized) initializeRaClient();

    size_t mappedBytes = 0;
    for (uint32_t i = 0; i < raMemoryRegions.count; ++i) {
        if (raMemoryRegions.data[i] != nullptr) mappedBytes += raMemoryRegions.size[i];
    }
    LOGD("RA_MEMORY_MAP phase=%s console=%u initialized=%d descriptors=%u regions=%u mapped_bytes=%zu total_bytes=%zu",
         phase,
         raConsoleId,
         raMemoryMapInitialized ? 1 : 0,
         memoryMap == nullptr ? 0 : memoryMap->num_descriptors,
         raMemoryRegions.count,
         mappedBytes,
         raMemoryRegions.total_size);
    if (!raMemoryMapInitialized && std::string(phase) == "post_first_frame" && !raUsername.empty()) {
        raNotifyAndroid(103, "", "This core did not expose a usable RetroAchievements memory map.");
    }
}

void LibretroDroid::initializeRaClient() {
    if (raClient != nullptr || raConsoleId == 0) return;
    raClient = rc_client_create(&LibretroDroid::callback_ra_read_memory, raServerCall);
    if (raClient != nullptr) {
        // Hardcore is intentionally unavailable until integrity restrictions are complete.
        rc_client_set_hardcore_enabled(raClient, 0);
        rc_client_set_unofficial_enabled(raClient, 0);
        rc_client_set_event_handler(raClient, raEventHandler);
        if (!raUsername.empty() && !raToken.empty() && !raGameHash.empty()) {
            rc_client_begin_login_with_token(raClient, raUsername.c_str(), raToken.c_str(), raLoginCallback, nullptr);
            raLoginStarted = true;
        }
    }
}

void LibretroDroid::configureRetroAchievements(const std::string& username, const std::string& token, const std::string& hash) {
    if (raClient != nullptr && raLoginStarted) {
        rc_client_unload_game(raClient);
        rc_client_logout(raClient);
        raLoginStarted = false;
    }
    raUsername = username;
    raToken = token;
    raGameHash = hash;
    if (raConsoleId != 0 && !raMemoryMapInitialized && raMemoryMapRetriedAfterFirstFrame) {
        raNotifyAndroid(103, "", "This core did not expose a usable RetroAchievements memory map.");
        return;
    }
    if (raClient != nullptr && !raLoginStarted && !raUsername.empty() && !raToken.empty() && !raGameHash.empty()) {
        rc_client_begin_login_with_token(raClient, raUsername.c_str(), raToken.c_str(), raLoginCallback, nullptr);
        raLoginStarted = true;
    }
}

void LibretroDroid::completeRaServerRequest(uint64_t requestId, int statusCode, const std::string& body) {
    std::lock_guard<std::mutex> lock(g_raResponseMutex);
    if (g_raPendingRequests.find(requestId) != g_raPendingRequests.end()) {
        g_raResponses.push_back(RaServerResponse{requestId, statusCode, body});
    }
}

void LibretroDroid::destroyRaClient() {
    {
        std::lock_guard<std::mutex> lock(g_raResponseMutex);
        g_raPendingRequests.clear();
        g_raResponses.clear();
    }
    raLoginStarted = false;
    if (raClient != nullptr) {
        rc_client_logout(raClient);
        rc_client_destroy(raClient);
        raClient = nullptr;
    }
}

std::string LibretroDroid::getRaMemoryDiagnostics() {
    std::lock_guard<std::mutex> lock(coreLock);
    const auto* memoryMap = Environment::getInstance().getRetroMemoryMap();
    size_t mappedBytes = 0;
    for (uint32_t i = 0; i < raMemoryRegions.count; ++i) {
        if (raMemoryRegions.data[i] != nullptr) mappedBytes += raMemoryRegions.size[i];
    }
    uint32_t sampleCount = 0;
    uint32_t mappedSampleCount = 0;
    std::ostringstream rangeChecks;
    const auto* expectedRegions = rc_console_memory_regions(raConsoleId);
    if (expectedRegions != nullptr) {
        for (uint32_t i = 0; i < expectedRegions->num_regions; ++i) {
            const auto& region = expectedRegions->region[i];
            if (region.type == RC_MEMORY_TYPE_UNUSED || region.type == RC_MEMORY_TYPE_READONLY) continue;
            const uint32_t addresses[] = {
                region.start_address,
                region.start_address + (region.end_address - region.start_address) / 2,
                region.end_address,
            };
            uint32_t mappedInRegion = 0;
            for (const auto address : addresses) {
                uint8_t value = 0;
                ++sampleCount;
                if (rc_libretro_memory_read(&raMemoryRegions, address, &value, 1) == 1) {
                    ++mappedInRegion;
                    ++mappedSampleCount;
                }
            }
            rangeChecks << ",range" << i << "=0x" << std::hex << region.start_address
                        << "-0x" << region.end_address << std::dec << ":" << mappedInRegion << "/3";
        }
    }
    std::ostringstream result;
    result << "console_id=" << raConsoleId
           << ",initialized=" << (raMemoryMapInitialized ? 1 : 0)
           << ",descriptors=" << (memoryMap == nullptr ? 0 : memoryMap->num_descriptors)
           << ",regions=" << raMemoryRegions.count
           << ",mapped_bytes=" << mappedBytes
           << ",address_space_bytes=" << raMemoryRegions.total_size
           << ",address_samples=" << mappedSampleCount << "/" << sampleCount
           << rangeChecks.str();
    if (memoryMap != nullptr && memoryMap->descriptors != nullptr) {
        for (uint32_t i = 0; i < memoryMap->num_descriptors; ++i) {
            const auto& descriptor = memoryMap->descriptors[i];
            result << ",descriptor" << i << "={ptr=" << (descriptor.ptr != nullptr ? 1 : 0)
                   << ";start=0x" << std::hex << descriptor.start
                   << ";len=0x" << descriptor.len
                   << ";select=0x" << descriptor.select
                   << ";disconnect=0x" << descriptor.disconnect
                   << ";offset=0x" << descriptor.offset
                   << ";flags=0x" << descriptor.flags << std::dec << "}";
        }
    }
    return result.str();
}

void LibretroDroid::getCoreMemoryInfo(uint32_t id, rc_libretro_core_memory_info_t* info) {
    if (info == nullptr) return;
    if (core == nullptr) {
        info->data = nullptr;
        info->size = 0;
        return;
    }
    info->data = static_cast<uint8_t*>(core->retro_get_memory_data(id));
    info->size = core->retro_get_memory_size(id);
}

float LibretroDroid::findDefaultAspectRatio(const retro_system_av_info& system_av_info) {
    float result = system_av_info.geometry.aspect_ratio;
    if (result < 0) {
        result =
            (float) system_av_info.geometry.base_width / (float) system_av_info.geometry.base_height;
    }
    return result;
}

void LibretroDroid::handleRumbleUpdates(const std::function<void(int, float, float)> &handler) {
    if (rumble && rumbleEnabled) {
        rumble->handleRumbleUpdates(handler);
    }
}

void LibretroDroid::setViewport(Rect viewportRect) {
    this->viewportRect = viewportRect;

    if (video != nullptr) {
        video->updateViewportSize(viewportRect);
    }
}

void LibretroDroid::setViewportAlignment(unsigned int viewportAlignment) {
    this-> viewportAlignment = viewportAlignment;

    if (video) {
        this->video->updateViewportAlignment(viewportAlignment);
    }
}

} //namespace libretrodroid
