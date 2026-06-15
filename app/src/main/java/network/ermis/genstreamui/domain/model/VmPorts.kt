package network.ermis.genstreamui.domain.model

/**
 * Dẫn xuất toàn bộ port của VM từ một **base port** duy nhất do backend trả về
 * (`connection-token.vm_endpoint.port`). VM nằm sau relay offset-forwarding nên mọi cổng dịch vụ
 * đều suy ra từ base theo offset cố định (xem genstream-custom-auth.md §7).
 *
 * Offset gốc tính theo cổng Moonlight chuẩn với DEFAULT_HTTP_PORT=47989, DEFAULT_HTTPS_PORT=47984.
 */
data class VmPorts(val base: Int) {
    /** HTTP GameStream — chính là port backend trả về. */
    val http: Int get() = base

    /** HTTPS (serverinfo/launch/resume/cancel, mTLS) = base − 5 (47984 − 47989). */
    val https: Int get() = base - 5

    /** token-auth (/api/auth/token) = base + 2. */
    val tokenAuth: Int get() = base + 2

    /** genstream-agent (/launch, /close) = base + 3. */
    val agent: Int get() = base + 3

    /** RTSP = base + 21 (48010 − 47989). */
    val rtsp: Int get() = base + 21

    /** Video UDP = base + 9 (47998 − 47989). */
    val video: Int get() = base + 9

    /** Control UDP = base + 10 (47999 − 47989). */
    val control: Int get() = base + 10

    /** Audio UDP = base + 11 (48000 − 47989). */
    val audio: Int get() = base + 11
}
