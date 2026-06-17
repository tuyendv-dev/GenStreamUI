package network.ermis.genstreamui.domain.model.mapper

import network.ermis.genstreamui.domain.model.Subscription
import network.ermis.genstreamui.domain.model.dto.res.SubscriptionDTO

/**
 * Map DTO mạng -> model core domain cho gói thuê bao. Mọi field nullable từ [SubscriptionDTO]
 * được quy về giá trị an toàn; số giờ dạng chuỗi ("60.00") parse về Double (lỗi -> 0.0).
 */
fun SubscriptionDTO.toDomain(): Subscription = Subscription(
    id = id ?: 0,
    packageId = packageId ?: 0,
    planId = planId ?: 0,
    status = status.orEmpty(),
    expiresAt = expiresAt.orEmpty(),
    hoursTotal = hoursTotal?.toDoubleOrNull() ?: 0.0,
    hoursUsed = hoursUsed?.toDoubleOrNull() ?: 0.0,
    hoursRemaining = hoursRemaining?.toDoubleOrNull() ?: 0.0,
    packageName = priceSnapshot?.packageName.orEmpty(),
    cpuModel = priceSnapshot?.cpuModel.orEmpty(),
    gpuModel = priceSnapshot?.gpuModel.orEmpty()
)
