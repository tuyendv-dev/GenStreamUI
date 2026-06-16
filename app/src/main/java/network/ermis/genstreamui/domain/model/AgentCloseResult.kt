package network.ermis.genstreamui.domain.model

/** Kết quả gọi agent /close. */
sealed interface AgentCloseResult {
    data class Closed(val killed: Boolean, val backupStatus: String) : AgentCloseResult
    data class Failed(val message: String) : AgentCloseResult
}
