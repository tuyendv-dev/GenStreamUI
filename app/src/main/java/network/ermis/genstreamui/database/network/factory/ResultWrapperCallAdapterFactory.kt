package network.ermis.genstreamui.database.network.factory

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * CallAdapter.Factory nhận diện các method trả về Call<ResultWrapper<T>> và gắn adapter bọc.
 * Port từ GenPlayAndroid.
 */
class ResultWrapperCallAdapterFactory : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) {
            return null
        }

        val callType = getParameterUpperBound(0, returnType as ParameterizedType)
        if (getRawType(callType) != ResultWrapper::class.java) {
            return null
        }

        val resultType = getParameterUpperBound(0, callType as ParameterizedType)
        return ResultWrapperCallAdapter<Any>(resultType)
    }
}
