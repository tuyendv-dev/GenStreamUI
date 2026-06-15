package network.ermis.genstreamui.domain.model

/**
 * Token gọi genstream-agent trên VM (Bearer cho /launch, /close). TTL 3600s, reusable.
 * Map từ [network.ermis.genstreamui.domain.model.dto.res.AgentTokenDTO].
 *
 * Agent nằm ở **base + 3** (HTTP, không TLS) — xem [VmPorts.agent] và genstream-custom-auth.md §7.
 */
data class AgentToken(
    val token: String = "",
    val expiresIn: Int = 0,
    val host: String = "",
    val port: Int = 0
) {
    val isValid: Boolean get() = token.isNotEmpty() && host.isNotEmpty() && port > 0

    val vmPorts: VmPorts get() = VmPorts(port)

    /** http://{host}:{base+3}/launch — mở game, body {platform, appid}, Bearer agent token. */
    val launchUrl: String get() = "http://$host:${vmPorts.agent}/launch"

    /** http://{host}:{base+3}/close — đóng game, body {platform, appid}, Bearer agent token. */
    val closeUrl: String get() = "http://$host:${vmPorts.agent}/close"
}
