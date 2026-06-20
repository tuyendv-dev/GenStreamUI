package network.ermis.genstreamui.domain.model

/**
 * Một trailer của game (phần tử mảng "trailers" trong response /games).
 * [video] là URL phát chính (HLS m3u8); [adaptive]/[microtrailer] là các nguồn thay thế.
 */
data class GameTrailer(
    val name: String = "",
    val video: String = "",
    val thumbnail: String = "",
    val category: Int = 0,
    val adaptive: List<TrailerStream> = emptyList(),
    val microtrailer: List<TrailerStream> = emptyList()
)

/**
 * Một nguồn video của trailer. "adaptive" dùng [encoding] (dash_av1/dash_h264/hls_h264),
 * "microtrailer" dùng [type] (video/webm, video/mp4).
 */
data class TrailerStream(
    val url: String = "",
    val encoding: String = "",
    val type: String = ""
)
