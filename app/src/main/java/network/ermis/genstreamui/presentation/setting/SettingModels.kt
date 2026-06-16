package network.ermis.genstreamui.presentation.setting

enum class SettingType {
    TOGGLE, ARROW, INFO, LIST, SEEKBAR
}

data class SettingItem(
    val id: String, // Maps to SharedPreferences key
    val title: String,
    val description: String = "",
    val type: SettingType = SettingType.TOGGLE,
    var isEnabled: Boolean = false, // Current boolean value or dependency state
    var value: String? = null, // Current string/int value
    
    // For List type
    var entries: Array<String>? = null,
    var entryValues: Array<String>? = null,
    
    // For Seekbar
    val min: Int = 0,
    val max: Int = 100,
    val step: Int = 1,
    val suffix: String = "",
    
    // For Dependencies
    val dependency: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SettingItem
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

data class SettingCategory(
    val id: String,
    val title: String,
    var items: MutableList<SettingItem>
)
