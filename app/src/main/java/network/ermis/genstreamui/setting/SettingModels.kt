package network.ermis.genstreamui.setting

enum class SettingType {
    TOGGLE, ARROW, INFO
}

data class SettingItem(
    val id: String,
    val title: String,
    val description: String = "",
    val type: SettingType = SettingType.TOGGLE,
    var isEnabled: Boolean = false,
    var value: String? = null
)

data class SettingCategory(
    val id: String,
    val title: String,
    val items: List<SettingItem>
)
