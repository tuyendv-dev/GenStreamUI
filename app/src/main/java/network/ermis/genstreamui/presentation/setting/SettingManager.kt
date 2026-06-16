package network.ermis.genstreamui.presentation.setting

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Vibrator
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import com.limelight.R
import com.limelight.binding.video.MediaCodecHelper
import com.limelight.preferences.GlPreferences
import com.limelight.preferences.PreferenceConfiguration

object SettingManager {

    fun getPrefs(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun loadSettings(context: Context): List<SettingCategory> {
        val prefs = getPrefs(context)
        val categories = mutableListOf<SettingCategory>()
        val res = context.resources

        // Basic
        categories.add(SettingCategory(
            id = "category_basic_settings",
            title = res.getString(R.string.category_basic_settings),
            items = mutableListOf(
                SettingItem(
                    id = "list_resolution",
                    title = res.getString(R.string.title_resolution_list),
                    description = res.getString(R.string.summary_resolution_list),
                    type = SettingType.LIST,
                    entries = res.getStringArray(R.array.resolution_names),
                    entryValues = res.getStringArray(R.array.resolution_values),
                    value = prefs.getString("list_resolution", PreferenceConfiguration.DEFAULT_RESOLUTION)
                ),
                SettingItem(
                    id = "list_fps",
                    title = res.getString(R.string.title_fps_list),
                    description = res.getString(R.string.summary_fps_list),
                    type = SettingType.LIST,
                    entries = res.getStringArray(R.array.fps_names),
                    entryValues = res.getStringArray(R.array.fps_values),
                    value = prefs.getString("list_fps", PreferenceConfiguration.DEFAULT_FPS)
                ),
                SettingItem(
                    id = "seekbar_bitrate_kbps",
                    title = res.getString(R.string.title_seekbar_bitrate),
                    description = res.getString(R.string.summary_seekbar_bitrate),
                    type = SettingType.SEEKBAR,
                    min = 500, max = 150000, step = 500, suffix = res.getString(R.string.suffix_seekbar_bitrate_mbps),
                    value = prefs.getInt("seekbar_bitrate_kbps", PreferenceConfiguration.getDefaultBitrate(context)).toString()
                ),
                SettingItem(
                    id = "frame_pacing",
                    title = res.getString(R.string.title_frame_pacing),
                    description = res.getString(R.string.summary_frame_pacing),
                    type = SettingType.LIST,
                    entries = res.getStringArray(R.array.video_frame_pacing_names),
                    entryValues = res.getStringArray(R.array.video_frame_pacing_values),
                    value = prefs.getString("frame_pacing", "latency")
                ),
                SettingItem(
                    id = "checkbox_stretch_video",
                    title = res.getString(R.string.title_checkbox_stretch_video),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_stretch_video", false)
                )
            )
        ))

        // Audio
        categories.add(SettingCategory(
            id = "category_audio_settings",
            title = res.getString(R.string.category_audio_settings),
            items = mutableListOf(
                SettingItem(
                    id = "list_audio_config",
                    title = res.getString(R.string.title_audio_config_list),
                    description = res.getString(R.string.summary_audio_config_list),
                    type = SettingType.LIST,
                    entries = res.getStringArray(R.array.audio_config_names),
                    entryValues = res.getStringArray(R.array.audio_config_values),
                    value = prefs.getString("list_audio_config", "2")
                ),
                SettingItem(
                    id = "checkbox_enable_audiofx",
                    title = res.getString(R.string.title_checkbox_enable_audiofx),
                    description = res.getString(R.string.summary_checkbox_enable_audiofx),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_enable_audiofx", false)
                )
            )
        ))

        // Gamepad
        categories.add(SettingCategory(
            id = "category_gamepad_settings",
            title = res.getString(R.string.category_gamepad_settings),
            items = mutableListOf(
                SettingItem(
                    id = "seekbar_deadzone",
                    title = res.getString(R.string.title_seekbar_deadzone),
                    description = res.getString(R.string.summary_seekbar_deadzone),
                    type = SettingType.SEEKBAR,
                    min = 0, max = 20, step = 1, suffix = res.getString(R.string.suffix_seekbar_deadzone),
                    value = prefs.getInt("seekbar_deadzone", 7).toString()
                ),
                SettingItem(
                    id = "checkbox_multi_controller",
                    title = res.getString(R.string.title_checkbox_multi_controller),
                    description = res.getString(R.string.summary_checkbox_multi_controller),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_multi_controller", true)
                ),
                SettingItem(
                    id = "checkbox_usb_driver",
                    title = res.getString(R.string.title_checkbox_xb1_driver),
                    description = res.getString(R.string.summary_checkbox_xb1_driver),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_usb_driver", true)
                ),
                SettingItem(
                    id = "checkbox_usb_bind_all",
                    title = res.getString(R.string.title_checkbox_usb_bind_all),
                    description = res.getString(R.string.summary_checkbox_usb_bind_all),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_usb_bind_all", false),
                    dependency = "checkbox_usb_driver"
                ),
                SettingItem(
                    id = "checkbox_mouse_emulation",
                    title = res.getString(R.string.title_checkbox_mouse_emulation),
                    description = res.getString(R.string.summary_checkbox_mouse_emulation),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_mouse_emulation", true)
                ),
                SettingItem(
                    id = "analog_scrolling",
                    title = res.getString(R.string.title_analog_scrolling),
                    description = res.getString(R.string.summary_analog_scrolling),
                    type = SettingType.LIST,
                    entries = res.getStringArray(R.array.analog_scrolling_names),
                    entryValues = res.getStringArray(R.array.analog_scrolling_values),
                    value = prefs.getString("analog_scrolling", "right"),
                    dependency = "checkbox_mouse_emulation"
                ),
                SettingItem(
                    id = "checkbox_vibrate_fallback",
                    title = res.getString(R.string.title_checkbox_vibrate_fallback),
                    description = res.getString(R.string.summary_checkbox_vibrate_fallback),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_vibrate_fallback", false)
                ),
                SettingItem(
                    id = "seekbar_vibrate_fallback_strength",
                    title = res.getString(R.string.title_seekbar_vibrate_fallback_strength),
                    description = res.getString(R.string.summary_seekbar_vibrate_fallback_strength),
                    type = SettingType.SEEKBAR,
                    min = 0, max = 200, step = 1, suffix = res.getString(R.string.suffix_seekbar_vibrate_fallback_strength),
                    value = prefs.getInt("seekbar_vibrate_fallback_strength", 100).toString(),
                    dependency = "checkbox_vibrate_fallback"
                ),
                SettingItem(
                    id = "checkbox_flip_face_buttons",
                    title = res.getString(R.string.title_checkbox_flip_face_buttons),
                    description = res.getString(R.string.summary_checkbox_flip_face_buttons),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_flip_face_buttons", false)
                ),
                SettingItem(
                    id = "checkbox_gamepad_touchpad_as_mouse",
                    title = res.getString(R.string.title_checkbox_gamepad_touchpad_as_mouse),
                    description = res.getString(R.string.summary_checkbox_gamepad_touchpad_as_mouse),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_gamepad_touchpad_as_mouse", false)
                ),
                SettingItem(
                    id = "checkbox_gamepad_motion_sensors",
                    title = res.getString(R.string.title_checkbox_gamepad_motion_sensors),
                    description = res.getString(R.string.summary_checkbox_gamepad_motion_sensors),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_gamepad_motion_sensors", true)
                ),
                SettingItem(
                    id = "checkbox_gamepad_motion_fallback",
                    title = res.getString(R.string.title_checkbox_gamepad_motion_fallback),
                    description = res.getString(R.string.summary_checkbox_gamepad_motion_fallback),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_gamepad_motion_fallback", false)
                )
            )
        ))

        // Input
        categories.add(SettingCategory(
            id = "category_input_settings",
            title = res.getString(R.string.category_input_settings),
            items = mutableListOf(
                SettingItem(
                    id = "checkbox_touchscreen_trackpad",
                    title = res.getString(R.string.title_checkbox_touchscreen_trackpad),
                    description = res.getString(R.string.summary_checkbox_touchscreen_trackpad),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_touchscreen_trackpad", true)
                ),
                SettingItem(
                    id = "checkbox_mouse_nav_buttons",
                    title = res.getString(R.string.title_checkbox_mouse_nav_buttons),
                    description = res.getString(R.string.summary_checkbox_mouse_nav_buttons),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_mouse_nav_buttons", false)
                ),
                SettingItem(
                    id = "checkbox_absolute_mouse_mode",
                    title = res.getString(R.string.title_checkbox_absolute_mouse_mode),
                    description = res.getString(R.string.summary_checkbox_absolute_mouse_mode),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_absolute_mouse_mode", false)
                )
            )
        ))

        // On-screen control
        categories.add(SettingCategory(
            id = "category_onscreen_controls",
            title = res.getString(R.string.category_on_screen_controls_settings),
            items = mutableListOf(
                SettingItem(
                    id = "checkbox_show_onscreen_controls",
                    title = res.getString(R.string.title_checkbox_show_onscreen_controls),
                    description = res.getString(R.string.summary_checkbox_show_onscreen_controls),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_show_onscreen_controls", false)
                ),
                SettingItem(
                    id = "checkbox_vibrate_osc",
                    title = res.getString(R.string.title_checkbox_vibrate_osc),
                    description = res.getString(R.string.summary_checkbox_vibrate_osc),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_vibrate_osc", true),
                    dependency = "checkbox_show_onscreen_controls"
                ),
                SettingItem(
                    id = "checkbox_only_show_L3R3",
                    title = res.getString(R.string.title_only_l3r3),
                    description = res.getString(R.string.summary_only_l3r3),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_only_show_L3R3", false),
                    dependency = "checkbox_show_onscreen_controls"
                ),
                SettingItem(
                    id = "checkbox_show_guide_button",
                    title = res.getString(R.string.title_show_guide_button),
                    description = res.getString(R.string.summary_show_guide_button),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_show_guide_button", true),
                    dependency = "checkbox_show_onscreen_controls"
                ),
                SettingItem(
                    id = "seekbar_osc_opacity",
                    title = res.getString(R.string.dialog_title_osc_opacity),
                    description = res.getString(R.string.summary_osc_opacity),
                    type = SettingType.SEEKBAR,
                    min = 0, max = 100, step = 1, suffix = res.getString(R.string.suffix_osc_opacity),
                    value = prefs.getInt("seekbar_osc_opacity", 90).toString(),
                    dependency = "checkbox_show_onscreen_controls"
                )
            )
        ))

        // Host
        categories.add(SettingCategory(
            id = "category_host_settings",
            title = res.getString(R.string.category_host_settings),
            items = mutableListOf(
                SettingItem(
                    id = "checkbox_enable_sops",
                    title = res.getString(R.string.title_checkbox_enable_sops),
                    description = res.getString(R.string.summary_checkbox_enable_sops),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_enable_sops", true)
                ),
                SettingItem(
                    id = "checkbox_host_audio",
                    title = res.getString(R.string.title_checkbox_host_audio),
                    description = res.getString(R.string.summary_checkbox_host_audio),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_host_audio", false)
                )
            )
        ))

        // UI
        categories.add(SettingCategory(
            id = "category_ui_settings",
            title = res.getString(R.string.category_ui_settings),
            items = mutableListOf(
                SettingItem(
                    id = "checkbox_enable_pip",
                    title = res.getString(R.string.title_checkbox_enable_pip),
                    description = res.getString(R.string.summary_checkbox_enable_pip),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_enable_pip", false)
                ),
                SettingItem(
                    id = "list_languages",
                    title = res.getString(R.string.title_language_list),
                    description = res.getString(R.string.summary_language_list),
                    type = SettingType.LIST,
                    entries = res.getStringArray(R.array.language_names),
                    entryValues = res.getStringArray(R.array.language_values),
                    value = prefs.getString("list_languages", PreferenceConfiguration.DEFAULT_LANGUAGE)
                ),
                SettingItem(
                    id = "checkbox_small_icon_mode",
                    title = res.getString(R.string.title_checkbox_small_icon_mode),
                    description = res.getString(R.string.summary_checkbox_small_icon_mode),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_small_icon_mode", PreferenceConfiguration.getDefaultSmallMode(context))
                )
            )
        ))

        // Advanced
        categories.add(SettingCategory(
            id = "category_advanced_settings",
            title = res.getString(R.string.category_advanced_settings),
            items = mutableListOf(
                SettingItem(
                    id = "checkbox_unlock_fps",
                    title = res.getString(R.string.title_unlock_fps),
                    description = res.getString(R.string.summary_unlock_fps),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_unlock_fps", false)
                ),
                SettingItem(
                    id = "checkbox_reduce_refresh_rate",
                    title = res.getString(R.string.title_checkbox_reduce_refresh_rate),
                    description = res.getString(R.string.summary_checkbox_reduce_refresh_rate),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_reduce_refresh_rate", false)
                ),
                SettingItem(
                    id = "checkbox_disable_warnings",
                    title = res.getString(R.string.title_checkbox_disable_warnings),
                    description = res.getString(R.string.summary_checkbox_disable_warnings),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_disable_warnings", false)
                ),
                SettingItem(
                    id = "video_format",
                    title = res.getString(R.string.title_video_format),
                    description = res.getString(R.string.summary_video_format),
                    type = SettingType.LIST,
                    entries = res.getStringArray(R.array.video_format_names),
                    entryValues = res.getStringArray(R.array.video_format_values),
                    value = prefs.getString("video_format", "auto")
                ),
                SettingItem(
                    id = "checkbox_enable_hdr",
                    title = res.getString(R.string.title_enable_hdr),
                    description = res.getString(R.string.summary_enable_hdr),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_enable_hdr", false)
                ),
                SettingItem(
                    id = "checkbox_full_range",
                    title = res.getString(R.string.title_full_range),
                    description = res.getString(R.string.summary_full_range),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_full_range", false)
                ),
                SettingItem(
                    id = "checkbox_enable_perf_overlay",
                    title = res.getString(R.string.title_enable_perf_overlay),
                    description = res.getString(R.string.summary_enable_perf_overlay),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_enable_perf_overlay", false)
                ),
                SettingItem(
                    id = "checkbox_enable_post_stream_toast",
                    title = res.getString(R.string.title_enable_post_stream_toast),
                    description = res.getString(R.string.summary_enable_post_stream_toast),
                    type = SettingType.TOGGLE,
                    isEnabled = prefs.getBoolean("checkbox_enable_post_stream_toast", false)
                )
            )
        ))

        // Apply dynamic visibility logic
        applyDynamicLogic(context, categories)

        return categories
    }

    private fun applyDynamicLogic(context: Context, categories: MutableList<SettingCategory>) {
        val packageManager = context.packageManager
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay

        val oscCategory = categories.find { it.id == "category_onscreen_controls" }
        val inputCategory = categories.find { it.id == "category_input_settings" }
        val gamepadCategory = categories.find { it.id == "category_gamepad_settings" }
        val uiCategory = categories.find { it.id == "category_ui_settings" }
        val advancedCategory = categories.find { it.id == "category_advanced_settings" }
        val basicCategory = categories.find { it.id == "category_basic_settings" }

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)) {
            categories.remove(oscCategory)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || packageManager.hasSystemFeature("com.nvidia.feature.shield")) {
            inputCategory?.items?.removeAll { it.id == "checkbox_absolute_mouse_mode" }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            gamepadCategory?.items?.removeAll { it.id == "checkbox_gamepad_motion_sensors" }
        }

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER) &&
            !packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE)) {
            gamepadCategory?.items?.removeAll { it.id == "checkbox_gamepad_motion_fallback" }
        }

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)) {
            gamepadCategory?.items?.removeAll { it.id == "checkbox_usb_bind_all" || it.id == "checkbox_usb_driver" }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            !packageManager.hasSystemFeature("android.software.picture_in_picture") ||
            packageManager.hasSystemFeature("com.amazon.software.fireos")) {
            uiCategory?.items?.removeAll { it.id == "checkbox_enable_pip" }
        }

        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (!vibrator.hasVibrator()) {
            gamepadCategory?.items?.removeAll { it.id == "checkbox_vibrate_fallback" || it.id == "seekbar_vibrate_fallback_strength" }
            oscCategory?.items?.removeAll { it.id == "checkbox_vibrate_osc" }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !vibrator.hasAmplitudeControl()) {
            gamepadCategory?.items?.removeAll { it.id == "seekbar_vibrate_fallback_strength" }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            advancedCategory?.items?.removeAll { it.id == "checkbox_enable_hdr" }
        } else {
            val hdrCaps = display.hdrCapabilities
            val foundHdr10 = hdrCaps?.supportedHdrTypes?.contains(Display.HdrCapabilities.HDR_TYPE_HDR10) == true
            if (!foundHdr10) {
                advancedCategory?.items?.removeAll { it.id == "checkbox_enable_hdr" }
            } else if (PreferenceConfiguration.isShieldAtvFirmwareWithBrokenHdr()) {
                val hdrItem = advancedCategory?.items?.find { it.id == "checkbox_enable_hdr" }
                if (hdrItem != null) {
                    hdrItem.isEnabled = false
                }
            }
        }

        var maxSupportedFps = display.refreshRate
        var maxSupportedResW = 0
        var hasInsets = false
        val newResEntries = mutableListOf<String>()
        val newResValues = mutableListOf<String>()

        val addNativeRes = { w: Int, h: Int, insets: Boolean, portrait: Boolean ->
            val namePrefix = if (insets) context.getString(R.string.resolution_prefix_native_fullscreen) else context.getString(R.string.resolution_prefix_native)
            var name = namePrefix
            if (PreferenceConfiguration.isSquarishScreen(w, h)) {
                name += " " + context.getString(if (portrait) R.string.resolution_prefix_native_portrait else R.string.resolution_prefix_native_landscape)
            }
            name += " (${w}x${h})"
            val value = "${w}x${h}"
            if (!newResValues.contains(value)) {
                newResEntries.add(name)
                newResValues.add(value)
            }
        }

        val addNativeResBoth = { w: Int, h: Int, insets: Boolean ->
            if (PreferenceConfiguration.isSquarishScreen(w, h)) {
                addNativeRes(h, w, insets, true)
            }
            addNativeRes(w, h, insets, false)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Ignore cutout logic for simplicity, could be added later
            }

            for (candidate in display.supportedModes) {
                val w = Math.max(candidate.physicalWidth, candidate.physicalHeight)
                val h = Math.min(candidate.physicalWidth, candidate.physicalHeight)
                if (!packageManager.hasSystemFeature(PackageManager.FEATURE_TELEVISION) || (w > 3840 || h > 2160)) {
                    addNativeResBoth(w, h, hasInsets)
                }
                if ((w >= 3840 || h >= 2160) && maxSupportedResW < 3840) maxSupportedResW = 3840
                else if ((w >= 2560 || h >= 1440) && maxSupportedResW < 2560) maxSupportedResW = 2560
                else if ((w >= 1920 || h >= 1080) && maxSupportedResW < 1920) maxSupportedResW = 1920

                if (candidate.refreshRate > maxSupportedFps) {
                    maxSupportedFps = candidate.refreshRate
                }
            }

            MediaCodecHelper.initialize(context, GlPreferences.readPreferences(context).glRenderer)
            val avcDecoder = MediaCodecHelper.findProbableSafeDecoder("video/avc", -1)
            val hevcDecoder = MediaCodecHelper.findProbableSafeDecoder("video/hevc", -1)
            
            if (avcDecoder != null) {
                val avcWidthRange = avcDecoder.getCapabilitiesForType("video/avc").videoCapabilities.supportedWidths
                if (avcWidthRange.contains(1280)) {
                    if (avcWidthRange.contains(3840) && maxSupportedResW < 3840) maxSupportedResW = 3840
                    else if (avcWidthRange.contains(1920) && maxSupportedResW < 1920) maxSupportedResW = 1920
                    else if (maxSupportedResW < 1280) maxSupportedResW = 1280
                }
            }
            if (hevcDecoder != null) {
                val hevcWidthRange = hevcDecoder.getCapabilitiesForType("video/hevc").videoCapabilities.supportedWidths
                if (hevcWidthRange.contains(1280)) {
                    if (hevcWidthRange.contains(3840) && maxSupportedResW < 3840) maxSupportedResW = 3840
                    else if (hevcWidthRange.contains(1920) && maxSupportedResW < 1920) maxSupportedResW = 1920
                    else if (maxSupportedResW < 1280) maxSupportedResW = 1280
                }
            }
        } else {
            val metrics = DisplayMetrics()
            display.getRealMetrics(metrics)
            val w = Math.max(metrics.widthPixels, metrics.heightPixels)
            val h = Math.min(metrics.widthPixels, metrics.heightPixels)
            addNativeResBoth(w, h, false)
        }

        val resItem = basicCategory?.items?.find { it.id == "list_resolution" }
        if (resItem != null) {
            val originalEntries = resItem.entries?.toMutableList() ?: mutableListOf()
            val originalValues = resItem.entryValues?.toMutableList() ?: mutableListOf()
            
            if (maxSupportedResW != 0) {
                if (maxSupportedResW < 3840) {
                    val idx = originalValues.indexOf(PreferenceConfiguration.RES_4K)
                    if (idx >= 0) { originalEntries.removeAt(idx); originalValues.removeAt(idx) }
                }
                if (maxSupportedResW < 2560) {
                    val idx = originalValues.indexOf(PreferenceConfiguration.RES_1440P)
                    if (idx >= 0) { originalEntries.removeAt(idx); originalValues.removeAt(idx) }
                }
                if (maxSupportedResW < 1920) {
                    val idx = originalValues.indexOf(PreferenceConfiguration.RES_1080P)
                    if (idx >= 0) { originalEntries.removeAt(idx); originalValues.removeAt(idx) }
                }
            }

            originalEntries.addAll(newResEntries)
            originalValues.addAll(newResValues)

            resItem.entries = originalEntries.toTypedArray()
            resItem.entryValues = originalValues.toTypedArray()

            if (!originalValues.contains(resItem.value)) {
                val prefs = getPrefs(context)
                val fallback = if (maxSupportedResW >= 1440) PreferenceConfiguration.RES_1440P
                               else if (maxSupportedResW >= 1080) PreferenceConfiguration.RES_1080P
                               else PreferenceConfiguration.RES_720P
                resItem.value = fallback
                prefs.edit().putString("list_resolution", fallback).apply()
                PreferenceConfiguration.getDefaultBitrate(fallback, prefs.getString("list_fps", PreferenceConfiguration.DEFAULT_FPS)).let {
                    prefs.edit().putInt("seekbar_bitrate_kbps", it).apply()
                }
            }
        }

        val fpsItem = basicCategory?.items?.find { it.id == "list_fps" }
        if (fpsItem != null) {
            val originalEntries = fpsItem.entries?.toMutableList() ?: mutableListOf()
            val originalValues = fpsItem.entryValues?.toMutableList() ?: mutableListOf()

            if (!PreferenceConfiguration.readPreferences(context).unlockFps) {
                if (maxSupportedFps < 118) {
                    val idx = originalValues.indexOf("120")
                    if (idx >= 0) { originalEntries.removeAt(idx); originalValues.removeAt(idx) }
                }
                if (maxSupportedFps < 88) {
                    val idx = originalValues.indexOf("90")
                    if (idx >= 0) { originalEntries.removeAt(idx); originalValues.removeAt(idx) }
                }
            }
            
            val frameRateRounded = Math.round(maxSupportedFps)
            if (frameRateRounded != 0) {
                val fpsValue = frameRateRounded.toString()
                if (!originalValues.contains(fpsValue)) {
                    val fpsName = context.getString(R.string.resolution_prefix_native) + " (" + fpsValue + " " + context.getString(R.string.fps_suffix_fps) + ")"
                    originalEntries.add(fpsName)
                    originalValues.add(fpsValue)
                }
            }

            fpsItem.entries = originalEntries.toTypedArray()
            fpsItem.entryValues = originalValues.toTypedArray()

            if (!originalValues.contains(fpsItem.value)) {
                val prefs = getPrefs(context)
                val fallback = if (maxSupportedFps >= 88) "90" else "60"
                fpsItem.value = fallback
                prefs.edit().putString("list_fps", fallback).apply()
                PreferenceConfiguration.getDefaultBitrate(prefs.getString("list_resolution", PreferenceConfiguration.DEFAULT_RESOLUTION), fallback).let {
                    prefs.edit().putInt("seekbar_bitrate_kbps", it).apply()
                }
            }
        }
    }

    fun saveSetting(context: Context, item: SettingItem, newValue: Any) {
        val prefs = getPrefs(context)
        val editor = prefs.edit()
        when (newValue) {
            is Boolean -> {
                item.isEnabled = newValue
                editor.putBoolean(item.id, newValue)
            }
            is String -> {
                item.value = newValue
                editor.putString(item.id, newValue)
            }
            is Int -> {
                item.value = newValue.toString()
                editor.putInt(item.id, newValue)
            }
        }
        editor.apply()
    }
}
