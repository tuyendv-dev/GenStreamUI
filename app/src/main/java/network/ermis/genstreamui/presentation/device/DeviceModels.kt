package network.ermis.genstreamui.presentation.device

import java.io.Serializable

data class DeviceItem(
    val id: String,
    val name: String,
    val isAddButton: Boolean = false,
    val iconResId: Int? = null
) : Serializable

data class DeviceCategory(
    val id: String,
    val title: String,
    val items: List<DeviceItem>
)
