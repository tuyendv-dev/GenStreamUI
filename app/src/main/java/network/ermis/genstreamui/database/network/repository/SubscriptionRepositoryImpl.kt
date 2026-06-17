package network.ermis.genstreamui.database.network.repository

import network.ermis.genstreamui.database.network.service.SubscriptionsService
import network.ermis.genstreamui.domain.repository.SubscriptionRepository
import javax.inject.Inject

/**
 * Triển khai [SubscriptionRepository]: delegate API sang [SubscriptionsService].
 * Thông tin thuê bao đổi theo phía server (số giờ còn lại...) nên không cache cục bộ.
 */
class SubscriptionRepositoryImpl @Inject constructor(
    private val subscriptionsService: SubscriptionsService
) : SubscriptionRepository {

    override suspend fun getUserSubscriptions() =
        subscriptionsService.getUserSubscriptions()

    override suspend fun getSessionActiveBySubscription(subscriptionId: Int) =
        subscriptionsService.getSessionActiveBySubscription(subscriptionId)
}
