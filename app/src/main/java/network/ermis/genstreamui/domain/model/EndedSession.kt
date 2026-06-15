package network.ermis.genstreamui.domain.model

/**
 * Kết quả kết thúc một phiên chơi — map từ
 * [network.ermis.genstreamui.domain.model.dto.res.EndSessionDataDTO].
 *
 * [consumedHours] giữ nguyên dạng String (số thập phân độ chính xác cao từ backend) để hiển thị/đối soát.
 */
data class EndedSession(
    val session: Session = Session(),
    val consumedHours: String = "",
    val overageSeconds: Long = 0
)
