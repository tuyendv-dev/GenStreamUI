package network.ermis.genstreamui.compose.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val BgColor = Color(0xFF000000)
val BgColor80 = Color(0x80000000)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0x50FFFFFF)
val CardBg = Color(0xFF262424)

private val DarkColorScheme = darkColorScheme(
    primary = White,
    background = BgColor,
    surface = CardBg,
    onPrimary = Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun GenStreamTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
