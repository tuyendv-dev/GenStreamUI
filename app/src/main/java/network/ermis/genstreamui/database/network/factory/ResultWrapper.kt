package network.ermis.genstreamui.database.network.factory

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Envelope an toàn kiểu cho mọi response HTTP. Port từ GenPlayAndroid.
 * Mọi service method trả về ResultWrapper<T>; client pattern-match Success / GenericError / NetworkError.
 */
@Keep
sealed class ResultWrapper<out T> {
    data class Success<out T>(val value: T) : ResultWrapper<T>()

    data class GenericError(
        @SerializedName("code")
        val code: Int? = null,
        @SerializedName("message")
        val message: String? = null
    ) : ResultWrapper<Nothing>()

    data object NetworkError : ResultWrapper<Nothing>()
}
