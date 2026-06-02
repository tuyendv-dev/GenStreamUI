package network.ermis.genstreamui.compose.ui

import android.content.Intent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import network.ermis.genstreamui.compose.GameModel
import network.ermis.genstreamui.compose.PlayGameActivity
import network.ermis.genstreamui.compose.R
import network.ermis.genstreamui.compose.scaleClickEffect
import network.ermis.genstreamui.compose.ui.theme.GenStreamTheme
import network.ermis.genstreamui.compose.ui.theme.TextSecondary

@Composable
fun FindGameScreen() {
    val context = LocalContext.current
    val gridState = rememberLazyGridState()

    // Scroll-linked floating footer animation state
    var isFooterVisible by remember { mutableStateOf(true) }
    LaunchedEffect(gridState.isScrollInProgress) {
        if (gridState.isScrollInProgress) {
            isFooterVisible = false
        } else {
            delay(500)
            isFooterVisible = true
        }
    }

    val footerAlpha by animateFloatAsState(
        targetValue = if (isFooterVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "footerAlpha"
    )
    val footerTranslationX by animateDpAsState(
        targetValue = if (isFooterVisible) 0.dp else 120.dp,
        animationSpec = tween(500),
        label = "footerTranslationX"
    )

    // Calculate alpha for background banner and text based on scroll offset
    val bannerAlpha by remember {
        derivedStateOf {
            if (gridState.firstVisibleItemIndex > 0) {
                0f
            } else {
                val scrollOffset = gridState.firstVisibleItemScrollOffset.toFloat()
                val fadeDistance = 800f
                (1f - (scrollOffset / fadeDistance)).coerceIn(0f, 1f)
            }
        }
    }

    val platformBgColor by remember {
        derivedStateOf {
            Color.Black.copy(alpha = 1f - bannerAlpha)
        }
    }

    val steamGames = remember {
        listOf(
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_11),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_3),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_4),
            GameModel("NieR: Automata", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_5),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_11),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_3),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_4),
            GameModel("NieR: Automata", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_5)
        )
    }

    val fightingGames = remember {
        listOf(
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_11),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_3),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_4),
            GameModel("NieR: Automata", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_5),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_11),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_3),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_4),
            GameModel("NieR: Automata", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_5)
        )
    }

    fun launchPlayGame(game: GameModel) {
        val intent = Intent(context, PlayGameActivity::class.java).apply {
            putExtra("GAME_TITLE", game.title)
            putExtra("GAME_DESC", game.description)
            putExtra("GAME_IMAGE", game.imageRes)
        }
        context.startActivity(intent)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Fixed Banner Background in Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .graphicsLayer { alpha = bannerAlpha }
        ) {
            Image(
                painter = painterResource(id = R.drawable.image_banner),
                contentDescription = "Banner background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Gradient Overlay matching XML
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black)
                        )
                    )
            )
        }

        // Scrollable content
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            // Spacer representing top padding and platform bar space
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(134.dp))
            }

            // Title
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Grand Theft Auto V",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 40.dp)
                        .graphicsLayer { alpha = bannerAlpha }
                )
            }

            // Description
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "\"Grand Theft Auto V\" has become one of the most popular and influential games globally since it release in 2013, thanks to its rich content and outstanding game quality",
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .padding(horizontal = 40.dp)
                        .padding(top = 8.dp)
                        .graphicsLayer { alpha = bannerAlpha }
                )
            }

            // Windows icon
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 40.dp)
                        .padding(top = 17.dp)
                        .graphicsLayer { alpha = bannerAlpha }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_windows),
                        contentDescription = "Windows Icon",
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Horizontal Small Banners (Row of 5 items)
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val smallBanners = listOf(
                        R.drawable.image_2 to "The Witcher 3",
                        R.drawable.image_3 to "Call of Duty",
                        R.drawable.image_4 to "Atomic Heart",
                        R.drawable.image_5 to "NieR: Automata",
                        R.drawable.image_11 to "Dark Souls"
                    )

                    smallBanners.forEach { (imgRes, name) ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(134f / 80f)
                                .scaleClickEffect {
                                    launchPlayGame(GameModel(name, "Steam Game", imgRes))
                                }
                        ) {
                            Image(
                                painter = painterResource(id = imgRes),
                                contentDescription = name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // Steam Shooter Games Title
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Steam Shooter Games",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 40.dp, top = 32.dp, bottom = 12.dp)
                )
            }

            // Steam Shooter Games Items
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 36.dp)
                ) {
                    Column {
                        steamGames.chunked(4).forEach { rowItems ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                rowItems.forEach { game ->
                                    Box(modifier = Modifier.weight(1f).padding(4.dp)) {
                                        GameItem(game = game, onClick = { launchPlayGame(game) })
                                    }
                                }
                                if (rowItems.size < 4) {
                                    repeat(4 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f).padding(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Fighting Games Title
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Fighting game",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 40.dp, top = 40.dp, bottom = 12.dp)
                )
            }

            // Fighting Games Items
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 36.dp)
                ) {
                    Column {
                        fightingGames.chunked(4).forEach { rowItems ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                rowItems.forEach { game ->
                                    Box(modifier = Modifier.weight(1f).padding(4.dp)) {
                                        GameItem(game = game, onClick = { launchPlayGame(game) })
                                    }
                                }
                                if (rowItems.size < 4) {
                                    repeat(4 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f).padding(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sticky Platform Header (Transparent overlays on top, shifts color smoothly to solid black on scroll)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(platformBgColor)
                .padding(horizontal = 40.dp)
                .padding(top = 64.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PC Games",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "XGPU",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Floating Footer
        Image(
            painter = painterResource(id = R.drawable.ic_footer_group),
            contentDescription = "Footer",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 16.dp)
                .graphicsLayer {
                    alpha = footerAlpha
                    translationX = footerTranslationX.toPx()
                }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FindGameScreenPreview() {
    GenStreamTheme {
        FindGameScreen()
    }
}

