package network.ermis.genstreamui.presentation.home.mine

data class MineTileItem(
    val id: Int,
    val iconResId: Int,
    val labelText: String?,
    val backgroundResId: Int,
    val tintColorString: String? = null
)