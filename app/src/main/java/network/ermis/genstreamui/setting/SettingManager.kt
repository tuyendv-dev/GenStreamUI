package network.ermis.genstreamui.setting

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SettingManager {

    private const val PREFS_NAME = "GenStreamSettings"
    private const val KEY_SETTINGS = "settings_json_v3"
    private const val KEY_LANGUAGE_SETTING = "setting_language"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getLanguage(context: Context): String {
        return getPrefs(context).getString(KEY_LANGUAGE_SETTING, "English") ?: "English"
    }

    fun setLanguage(context: Context, language: String) {
        getPrefs(context).edit().putString(KEY_LANGUAGE_SETTING, language).apply()
    }

    fun loadSettings(context: Context): List<SettingCategory> {
        val json = getPrefs(context).getString(KEY_SETTINGS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<SettingCategory>>() {}.type
                val categories: List<SettingCategory> = Gson().fromJson(json, type)
                
                // Ensure dynamic values are updated
                val currentLanguage = getLanguage(context)
                categories.find { it.id == "interface" }?.items?.find { it.id == "language" }?.value = currentLanguage
                
                return categories
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return getDefaultSettings(context)
    }

    fun saveSettings(context: Context, categories: List<SettingCategory>) {
        val json = Gson().toJson(categories)
        getPrefs(context).edit().putString(KEY_SETTINGS, json).apply()
    }

    private fun getDefaultSettings(context: Context): List<SettingCategory> {
        val currentLanguage = getLanguage(context)
        return listOf(
            SettingCategory(
                id = "basic",
                title = "Basic",
                items = listOf(
                    SettingItem("video_res", "Video Resolution", "Increase image sharpness. Lower for weaker devices and slower networks.", SettingType.ARROW),
                    SettingItem("video_fps", "Video Frame Rate", "Increase smoother motion. Lower for weaker devices.", SettingType.ARROW),
                    SettingItem("video_bitrate", "Video Bitrate", "Increase image quality. Lower to reduce network lag.", SettingType.ARROW),
                    SettingItem("video_pacing", "Video Frame Pacing", "Balance between smoothness and latency.", SettingType.ARROW),
                    SettingItem("stretch_video", "Stretch Video to Fullscreen", "", SettingType.TOGGLE, isEnabled = true)
                )
            ),
            SettingCategory(
                id = "audio",
                title = "Audio",
                items = listOf(
                    SettingItem("surround_sound", "Surround Sound Setup", "Enable 5.1 or 7.1 surround sound for supported speaker systems.", SettingType.ARROW),
                    SettingItem("system_eq", "System Equalizer Support", "Allow system audio effects while streaming, may increase audio latency.", SettingType.TOGGLE, isEnabled = true)
                )
            ),
            SettingCategory(
                id = "gamepad",
                title = "Gamepad",
                items = listOf(
                    SettingItem("deadzone", "Adjust Analog Stick Deadzone", "Note: Some games may enforce a larger deadzone than what Moonlight is configured to use.", SettingType.ARROW),
                    SettingItem("auto_detect", "Automatically Detect Controller Presence", "If disabled, a controller will always be considered connected.", SettingType.TOGGLE, isEnabled = true),
                    SettingItem("xbox_driver", "Xbox USB Controller Driver", "Enable a built-in USB driver for devices that don't support Xbox controllers.", SettingType.TOGGLE, isEnabled = true),
                    SettingItem("override_driver", "Override Controller Driver Support", "Use Moonlight's USB driver for all supported controllers, including Xbox controllers if supported.", SettingType.TOGGLE, isEnabled = true),
                    SettingItem("mouse_emu", "Controller Mouse Emulation", "Holding the Start button switches the controller into mouse mode.", SettingType.TOGGLE, isEnabled = true),
                    SettingItem("analog_scroll", "Use an Analog Stick to Scroll", "Choose an analog stick for scrolling while in mouse emulation mode.", SettingType.ARROW),
                    SettingItem("emu_rumble", "Emulate Rumble with Device Vibration", "Use your device's vibration motor to simulate rumble if your controller doesn't support it.", SettingType.TOGGLE, isEnabled = true),
                    SettingItem("adjust_rumble", "Adjust Emulated Rumble Intensity", "Increase or decrease the vibration strength on your device.", SettingType.TOGGLE, isEnabled = true),
                    SettingItem("reverse_face_btn", "Reverse Face Button Layout", "Swap the A/B and X/Y button layout for physical and on-screen controllers.", SettingType.TOGGLE, isEnabled = true),
                    SettingItem("always_control_mouse", "Always Control Mouse with Touchpad", "Force the gamepad touchpad to control the host mouse, even when emulating a gamepad with touchpad support.", SettingType.TOGGLE, isEnabled = true),
                    SettingItem("allow_motion_sensor", "Allow Use of Gamepad Motion Sensors", "Allow supported hosts to request motion sensor data while emulating a motion-enabled controller. Disabling this may slightly reduce power and network usage if motion sensors aren't being used in-game.", SettingType.TOGGLE, isEnabled = true),
                    SettingItem("emu_motion_sensor", "Emulate Gamepad Motion Sensors", "Use your device's built-in motion sensors if the connected controller or Android version doesn't support them.\nNote: Enabling this option may cause the host to detect your controller as a PlayStation controller.", SettingType.TOGGLE, isEnabled = true)
                )
            ),
            SettingCategory(
                id = "input",
                title = "Input",
                items = listOf(
                    SettingItem("touch_trackpad", "Use Touchscreen as Trackpad", "When enabled, the touchscreen behaves like a trackpad. When disabled, the touchscreen directly controls the mouse cursor.", SettingType.TOGGLE, isEnabled = true),
                    SettingItem("mouse_back_forward", "Enable Mouse Back and Forward Buttons", "Enabling this option may break right-click behavior on some buggy devices.", SettingType.TOGGLE, isEnabled = true),
                    SettingItem("remote_desktop_mouse", "Remote Desktop Mouse Mode", "Makes mouse acceleration feel more natural for remote desktop use, but it may not work properly with many games.", SettingType.TOGGLE, isEnabled = true)
                )
            ),
            SettingCategory(
                id = "onscreen",
                title = "On-screen control",
                items = listOf(
                    SettingItem("show_onscreen", "Show On-screen Controls", "Display virtual touch controls on the screen.", SettingType.TOGGLE, isEnabled = true),
                    SettingItem("enable_vibration", "Enable Vibration", "Use device vibration to simulate rumble for on-screen controls.", SettingType.TOGGLE, isEnabled = true),
                    SettingItem("show_l3_r3", "Show Only L3 and R3", "Hide all virtual buttons except L3 and R3.", SettingType.TOGGLE, isEnabled = true),
                    SettingItem("adjust_transparency", "Adjust Transparency", "Make the on-screen controls more transparent.", SettingType.ARROW),
                    SettingItem("reset_layout", "Reset Saved On-screen Layout", "Restore all on-screen controls to their default size and position.", SettingType.TOGGLE, isEnabled = true)
                )
            ),
            SettingCategory(
                id = "host",
                title = "Host",
                items = listOf(
                    SettingItem("opt_game_settings", "Optimize Game Settings", "Allow GeForce Experience to optimize game settings for streaming.", SettingType.TOGGLE, isEnabled = true),
                    SettingItem("play_audio_pc", "Play Audio on PC", "Play audio from both the PC and this device.", SettingType.TOGGLE, isEnabled = true)
                )
            ),
            SettingCategory(
                id = "interface",
                title = "Interface",
                items = listOf(
                    SettingItem("pip_mode", "Enable Picture-in-Picture Mode", "Allow the stream to be viewed (but not controlled) while multitasking.", SettingType.TOGGLE, isEnabled = true),
                    SettingItem("language", "Language", "Language used for Moonlight.", SettingType.ARROW, value = currentLanguage),
                    SettingItem("small_tiles", "Use Small Tiles", "Smaller tiles in the app grid allow more apps to fit on the screen.", SettingType.TOGGLE, isEnabled = true)
                )
            ),
            SettingCategory(
                id = "advanced",
                title = "Advanced",
                items = listOf(
                    SettingItem("unlock_fps", "Unlock All Frame Rates", "Streaming at 90 or 120 FPS may reduce latency on high-end devices, but can cause lag or instability on unsupported devices.", SettingType.TOGGLE, isEnabled = true),
                    SettingItem("refresh_rate_reduce", "Allow Refresh Rate Reduction", "Lower display refresh rates can save battery at the cost of additional video latency.", SettingType.TOGGLE, isEnabled = true),
                    SettingItem("disable_warning", "Disable Warning Notifications", "Disable on-screen connection warning notifications while streaming.", SettingType.TOGGLE, isEnabled = true),
                    SettingItem("override_hevc", "Override HEVC Settings", "HEVC reduces video bandwidth usage but requires newer hardware support.", SettingType.ARROW),
                    SettingItem("enable_hdr", "Enable HDR (Experimental)", "Enable HDR streaming when both the game and PC GPU support it.\nHDR requires an NVIDIA GTX 1000 series GPU or newer.", SettingType.TOGGLE, isEnabled = true),
                    SettingItem("force_full_range", "Force Full Range Video (Experimental)", "May cause loss of detail in bright and dark areas if your device doesn't properly support full-range video.", SettingType.TOGGLE, isEnabled = true),
                    SettingItem("show_perf_stats", "Show Performance Stats During Streaming", "Display real-time stream performance information while streaming.", SettingType.TOGGLE, isEnabled = true),
                    SettingItem("show_latency", "Show Latency Summary After Streaming", "Display a latency statistics summary after the stream ends.", SettingType.TOGGLE, isEnabled = true)
                )
            )
        )
    }
}
