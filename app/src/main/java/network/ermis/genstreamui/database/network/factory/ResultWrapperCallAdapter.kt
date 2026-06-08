package network.ermis.genstreamui.database.network.factory

import com.google.gson.Gson
import okio.Timeout
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.lang.reflect.Type

/**
 * Bọc mọi onResponse/onFailure của Retrofit thành ResultWrapper:
 * 2xx có body -> Success, lỗi HTTP -> GenericError (parse code/message từ errorBody),
 * IOException/exception -> GenericError. Port từ GenPlayAndroid.
 */
class ResultWrapperCallAdapter<R>(
    private val responseType: Type
) : CallAdapter<R, Call<ResultWrapper<R>>> {

    override fun responseType() = responseType

    override fun adapt(call: Call<R>): Call<ResultWrapper<R>> {
        return object : Call<ResultWrapper<R>> {
            override fun execute(): Response<ResultWrapper<R>> {
                throw UnsupportedOperationException("ResultWrapperCallAdapter does not support execute")
            }

            override fun enqueue(callback: Callback<ResultWrapper<R>>) {
                call.enqueue(object : Callback<R> {
                    override fun onResponse(call: Call<R>, response: Response<R>) {
                        val result = if (response.isSuccessful) {
                            val body = response.body()
                            if (body != null) {
                                ResultWrapper.Success(body)
                            } else {
                                ResultWrapper.GenericError(response.code(), "Empty body")
                            }
                        } else {
                            val errorBodyString = response.errorBody()?.string()
                            val error = errorBodyString?.let {
                                runCatching {
                                    Gson().fromJson(it, ResultWrapper.GenericError::class.java)
                                }.getOrNull()
                            }
                            ResultWrapper.GenericError(
                                code = error?.code ?: response.code(),
                                message = error?.message
                            )
                        }

                        callback.onResponse(
                            this@ResultWrapperCallAdapter.adapt(call),
                            Response.success(result)
                        )
                    }

                    override fun onFailure(call: Call<R>, t: Throwable) {
                        val result = if (t is IOException) {
                            ResultWrapper.GenericError(null, t.message)
                        } else {
                            ResultWrapper.GenericError(null, t.message)
                        }
                        callback.onResponse(
                            this@ResultWrapperCallAdapter.adapt(call),
                            Response.success(result)
                        )
                    }
                })
            }

            override fun isExecuted() = call.isExecuted
            override fun cancel() = call.cancel()
            override fun isCanceled() = call.isCanceled
            override fun clone(): Call<ResultWrapper<R>> = this
            override fun request() = call.request()
            override fun timeout(): Timeout = call.timeout()
        }
    }
}
