package network.ermis.genstreamui.domain.model.dto.res

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Một gói thuê bao (subscription) của người dùng — item trong GET /users/me/subscriptions.
 *
 * Các field số giờ (hours_total/used/remaining...) backend trả về dưới dạng chuỗi thập phân
 * (vd "60.00") nên giữ kiểu [String] để không mất độ chính xác; parse khi cần hiển thị/tính toán.
 */
@Keep
data class SubscriptionDTO(
    val id: Int? = null,
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("package_id")
    val packageId: Int? = null,
    @SerializedName("plan_id")
    val planId: Int? = null,
    val status: String? = null,
    @SerializedName("started_at")
    val startedAt: String? = null,
    @SerializedName("expires_at")
    val expiresAt: String? = null,
    @SerializedName("auto_renew")
    val autoRenew: Boolean? = null,
    @SerializedName("hours_total")
    val hoursTotal: String? = null,
    @SerializedName("hours_used")
    val hoursUsed: String? = null,
    @SerializedName("pay_per_hour_hours_remaining")
    val payPerHourHoursRemaining: String? = null,
    @SerializedName("hours_remaining")
    val hoursRemaining: String? = null,
    @SerializedName("price_snapshot")
    val priceSnapshot: PriceSnapshotDTO? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

/**
 * Ảnh chụp giá + cấu hình gói tại thời điểm mua (giữ nguyên dù bảng giá đổi sau này).
 * plan_price / price_per_hour là chuỗi thập phân; ram/storage/hours là số nguyên.
 */
@Keep
data class PriceSnapshotDTO(
    @SerializedName("package_name")
    val packageName: String? = null,
    @SerializedName("cpu_model")
    val cpuModel: String? = null,
    @SerializedName("gpu_model")
    val gpuModel: String? = null,
    @SerializedName("ram_gb")
    val ramGb: Int? = null,
    @SerializedName("storage_gb")
    val storageGb: Int? = null,
    @SerializedName("monthly_hours")
    val monthlyHours: Int? = null,
    @SerializedName("plan_duration_months")
    val planDurationMonths: Int? = null,
    @SerializedName("plan_price")
    val planPrice: String? = null,
    @SerializedName("price_per_hour")
    val pricePerHour: String? = null,
    val currency: String? = null
)
