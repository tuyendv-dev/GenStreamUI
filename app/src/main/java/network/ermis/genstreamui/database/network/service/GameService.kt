package network.ermis.genstreamui.database.network.service

import network.ermis.genstreamui.database.cache.SharedPrefCommon
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.dto.res.ResDiscovery
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * Retrofit service cho game. Song song với [AuthService] / [UserService].
 */
interface GameService {

    @GET("/games/discovery")
    suspend fun getDiscovery(
        @Header("Accept") accept: String = "application/json",
        @Header("Accept-Language") language: String = SharedPrefCommon.languageCode.ifEmpty { "en" }
    ): ResultWrapper<ResDiscovery>

    /** Duyệt game theo store (vd: steam). GET /games/browse?store=steam — cùng schema với discovery. */
    @GET("/games/browse")
    suspend fun browse(
        @Query("store") store: String?,
        @Header("Accept") accept: String = "application/json",
        @Header("Accept-Language") language: String = SharedPrefCommon.languageCode.ifEmpty { "en" }
    ): ResultWrapper<ResDiscovery>
}
