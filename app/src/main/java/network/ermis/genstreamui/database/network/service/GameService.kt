package network.ermis.genstreamui.database.network.service

import network.ermis.genstreamui.database.cache.SharedPrefCommon
import network.ermis.genstreamui.database.network.factory.ResultWrapper
import network.ermis.genstreamui.domain.model.dto.res.ResDiscovery
import network.ermis.genstreamui.domain.model.dto.res.ResGameCategories
import network.ermis.genstreamui.domain.model.dto.res.ResGameDetail
import network.ermis.genstreamui.domain.model.dto.res.ResSearchGames
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
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

    /** Chi tiết một game theo id. GET /games/{id}. */
    @GET("/games/{id}")
    suspend fun getGameDetail(
        @Path("id") id: Int,
        @Header("Accept") accept: String = "application/json",
        @Header("Accept-Language") language: String = SharedPrefCommon.languageCode.ifEmpty { "en" }
    ): ResultWrapper<ResGameDetail>

    /**
     * Tìm/lọc game. GET /games?featured=&hot=&recommended=&q=&category=&platform=&page=&per_page=.
     * Tất cả filter đều optional — truyền null để bỏ qua field tương ứng.
     */
    @GET("/games")
    suspend fun searchGames(
        @Query("q") q: String? = null,
        @Query("category") category: String? = null,
        @Query("platform") platform: String? = null,
        @Query("featured") featured: Boolean? = null,
        @Query("hot") hot: Boolean? = null,
        @Query("recommended") recommended: Boolean? = null,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
        @Header("Accept") accept: String = "application/json",
        @Header("Accept-Language") language: String = SharedPrefCommon.languageCode.ifEmpty { "en" }
    ): ResultWrapper<ResSearchGames>

    /** Danh sách category của game. GET /games/categories. */
    @GET("/games/categories")
    suspend fun getGameCategories(
        @Header("Accept") accept: String = "application/json",
        @Header("Accept-Language") language: String = SharedPrefCommon.languageCode.ifEmpty { "en" }
    ): ResultWrapper<ResGameCategories>
}
