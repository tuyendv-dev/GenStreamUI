package network.ermis.genstreamui.domain.model

/**
 * Model core gói thuê bao ở tầng domain — UI/ViewModel dùng trực tiếp thay cho DTO.
 * Map từ [network.ermis.genstreamui.domain.model.dto.res.SubscriptionDTO] qua SubscriptionMapper.
 *
 * Số giờ backend trả dạng chuỗi thập phân ("60.00") đã được parse về [Double] cho dễ hiển thị/tính.
 */
data class Subscription(
    val id: Int = 0,
    val packageId: Int = 0,
    val planId: Int = 0,
    val status: String = "",
    val expiresAt: String = "",
    val hoursTotal: Double = 0.0,
    val hoursUsed: Double = 0.0,
    val hoursRemaining: Double = 0.0,
    val packageName: String = "",
    val cpuModel: String = "",
    val gpuModel: String = ""
)
