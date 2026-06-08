package network.ermis.genstreamui.presentation.subscription

import androidx.annotation.DrawableRes

data class PlanFeature(
    @DrawableRes val iconResId: Int,
    val text: String
)

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val subtitle: String,
    val price: String,
    val priceUnit: String,
    val isCurrentPlan: Boolean,
    val buttonText: String,
    val features: List<PlanFeature>,
    @DrawableRes val iconResId: Int
)
