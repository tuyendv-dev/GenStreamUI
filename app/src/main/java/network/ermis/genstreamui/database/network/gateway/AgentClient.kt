package network.ermis.genstreamui.database.network.gateway

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import network.ermis.genstreamui.BuildConfig
import network.ermis.genstreamui.domain.model.dto.req.ReqAgentCommand
import network.ermis.genstreamui.domain.model.dto.res.ResAgentClose
import network.ermis.genstreamui.domain.model.dto.res.ResAgentLaunch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Client gọi **genstream-agent** trên VM — `http://host:base+3/launch|/close` (HTTP thường, không TLS),
 * Bearer **agent token** (genstream-custom-auth.md §6 Stage 3a / §9). Single-shot, trả [Outcome] thô.
 */
@Singleton
class AgentClient @Inject constructor() {

    /** @param httpStatus mã HTTP, **0** nếu unreachable/IO error. */
    data class Outcome<T>(val httpStatus: Int, val body: T?)

    private val gson = Gson()
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient.Builder()
        .apply {
            // Log HTTP cho agent /launch /close (chỉ DEBUG).
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                )
            }
        }
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun launch(url: String, bearerToken: String, req: ReqAgentCommand): Outcome<ResAgentLaunch> =
        post(url, bearerToken, req, ResAgentLaunch::class.java)

    suspend fun close(url: String, bearerToken: String, req: ReqAgentCommand): Outcome<ResAgentClose> =
        post(url, bearerToken, req, ResAgentClose::class.java)

    private suspend fun <T> post(
        url: String,
        bearerToken: String,
        req: ReqAgentCommand,
        clazz: Class<T>
    ): Outcome<T> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $bearerToken")
            .post(gson.toJson(req).toRequestBody(jsonMedia))
            .build()
        try {
            client.newCall(request).execute().use { resp ->
                val parsed = resp.body?.string()?.let {
                    runCatching { gson.fromJson(it, clazz) }.getOrNull()
                }
                Outcome(resp.code, parsed)
            }
        } catch (e: IOException) {
            Outcome(httpStatus = 0, body = null)
        }
    }
}
