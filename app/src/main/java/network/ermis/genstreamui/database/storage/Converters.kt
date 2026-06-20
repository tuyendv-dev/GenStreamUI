package network.ermis.genstreamui.database.storage

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import network.ermis.genstreamui.domain.model.GameTrailer

/** TypeConverters cho kiểu phức (List<String>, List<GameTrailer> <-> JSON). Port pattern từ GenPlayAndroid. */
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String = gson.toJson(value ?: emptyList<String>())

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return runCatching { gson.fromJson<List<String>>(value, type) }.getOrDefault(emptyList())
    }

    @TypeConverter
    fun fromTrailerList(value: List<GameTrailer>?): String = gson.toJson(value ?: emptyList<GameTrailer>())

    @TypeConverter
    fun toTrailerList(value: String?): List<GameTrailer> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<GameTrailer>>() {}.type
        return runCatching { gson.fromJson<List<GameTrailer>>(value, type) }.getOrDefault(emptyList())
    }
}
