@file:Suppress("UNCHECKED_CAST")

package network.ermis.genstreamui.database.cache

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Wrapper SharedPreferences type-safe với property delegate. Khởi tạo trong GlobalApp.onCreate().
 * Port từ GenPlayAndroid (database/cache/SharedPrefCommon.kt).
 */
object SharedPrefCommon {
    private const val PREFERENCES_NAME = "GenStreamUI-Prefs"
    private var sharePref: SharedPreferences? = null

    fun init(context: Context) {
        if (sharePref == null) {
            sharePref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        }
    }

    fun setValue(keyName: String, value: Any?) {
        sharePref?.edit {
            when (value) {
                is Int -> putInt(keyName, value)
                is Float -> putFloat(keyName, value)
                is Long -> putLong(keyName, value)
                is Boolean -> putBoolean(keyName, value)
                is String -> putString(keyName, value)
                is Double -> putString(keyName, value.toString())
            }
        }
    }

    fun <T> getValue(keyName: String, defaultValue: T): T = when (defaultValue) {
        is Int -> (sharePref?.getInt(keyName, defaultValue) ?: defaultValue) as T
        is Long -> (sharePref?.getLong(keyName, defaultValue) ?: defaultValue) as T
        is Float -> (sharePref?.getFloat(keyName, defaultValue) ?: defaultValue) as T
        is Boolean -> (sharePref?.getBoolean(keyName, defaultValue) ?: defaultValue) as T
        is String -> (sharePref?.getString(keyName, defaultValue) ?: defaultValue) as T
        is Double -> (sharePref?.getString(keyName, defaultValue.toString())?.toDouble()
            ?: defaultValue) as T

        else -> defaultValue
    }

    var firstInstall: Boolean
        get() = getValue("firstInstall", true)
        set(value) = setValue("firstInstall", value)

    var languageCode: String
        get() = getValue("languageCode", "")
        set(value) = setValue("languageCode", value)

    var accessToken: String
        get() = getValue("accessToken", "")
        set(value) = setValue("accessToken", value)

    var userName: String
        get() = getValue("userName", "")
        set(value) = setValue("userName", value)

    var userId: Int
        get() = getValue("userId", 0)
        set(value) = setValue("userId", value)

    var userEmail: String
        get() = getValue("userEmail", "")
        set(value) = setValue("userEmail", value)

    var userAvatarUrl: String
        get() = getValue("userAvatarUrl", "")
        set(value) = setValue("userAvatarUrl", value)
}
