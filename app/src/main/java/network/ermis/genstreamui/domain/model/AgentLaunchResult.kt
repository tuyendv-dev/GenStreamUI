package network.ermis.genstreamui.domain.model

/**
 * Kết quả gọi agent /launch (Play-Now). Launch là fire-and-forget — [Failed] không chặn stream
 * Desktop, chỉ để log/thông báo.
 */
sealed interface AgentLaunchResult {
    data class Launched(val pid: Int, val saveStatus: String) : AgentLaunchResult
    data class Failed(val message: String) : AgentLaunchResult
}
