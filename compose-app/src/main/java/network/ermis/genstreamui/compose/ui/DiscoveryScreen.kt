package network.ermis.genstreamui.compose.ui

import android.content.Context
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import network.ermis.genstreamui.compose.GameModel
import network.ermis.genstreamui.compose.PlayGameActivity
import network.ermis.genstreamui.compose.R
import network.ermis.genstreamui.compose.scaleClickEffect
import network.ermis.genstreamui.compose.ui.theme.CardBg
import network.ermis.genstreamui.compose.ui.theme.GenStreamTheme
import network.ermis.genstreamui.compose.ui.theme.TextSecondary

@Composable
fun DiscoveryScreen() {
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

    val banners = remember {
        listOf(
            GameModel("Atomic Heart", "Stream game Recommendation", R.drawable.image_1),
            GameModel("The Witcher 3: Wild Hunt", "Stream game Recommendation", R.drawable.image_1),
            GameModel("Call of Duty: Mobile", "Stream game Recommendation", R.drawable.image_1),
            GameModel("Dead or Alive 6", "Stream game Recommendation", R.drawable.image_1),
            GameModel("NieR: Automata", "Stream game Recommendation", R.drawable.image_1)
        )
    }

    val adventureGames = remember {
        listOf(
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_4),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_2),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_11),
            GameModel("NieR: Automata", "Slay monster/ Game...", R.drawable.image_5)
        )
    }

    val fightingGames = remember {
        listOf(
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_4),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_2),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_11),
            GameModel("NieR: Automata", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_5),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_4),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_2),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_11),
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
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            // Padding to prevent content from going behind the fixed Top Bar
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(52.dp))
            }

            // Banner Section
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(horizontal = 40.dp, vertical = 8.dp)
                ) {
                    // Left main sliding banner
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        modifier = Modifier
                            .weight(0.68f)
                            .aspectRatio(17f / 9f)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val pagerState = rememberPagerState(initialPage = 0) { banners.size }
                            
                            LaunchedEffect(pagerState) {
                                while (true) {
                                    delay(2000)
                                    if (banners.isNotEmpty()) {
                                        val nextPage = (pagerState.currentPage + 1) % banners.size
                                        pagerState.animateScrollToPage(nextPage)
                                    }
                                }
                            }

                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                val game = banners[page]
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .scaleClickEffect { launchPlayGame(game) }
                                ) {
                                    Image(
                                        painter = painterResource(id = game.imageRes),
                                        contentDescription = game.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(0.5f)
                                            .align(Alignment.BottomCenter)
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, Color.Black)
                                                )
                                            )
                                    )
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = game.title,
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = game.description,
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            // Banner indicators
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(bottom = 10.dp, end = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(banners.size) { index ->
                                    val isSelected = pagerState.currentPage == index
                                    Box(
                                        modifier = Modifier
                                            .size(if (isSelected) 8.dp else 6.dp)
                                            .background(
                                                color = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.5f),
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Right static banners
                    Column(
                        modifier = Modifier
                            .weight(0.32f)
                            .fillMaxHeight()
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .scaleClickEffect {
                                    launchPlayGame(GameModel("The Witcher 3: Wild Hunt", "Stream game Recommendation", R.drawable.image_2))
                                }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.image_2),
                                contentDescription = "Top side banner",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .scaleClickEffect {
                                    launchPlayGame(GameModel("Call of Duty: Mobile", "Stream game Recommendation", R.drawable.image_3))
                                }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.image_3),
                                contentDescription = "Bottom side banner",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // Adventure game title
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Adventure game",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 40.dp, top = 24.dp, bottom = 12.dp)
                )
            }

            // Adventure game items
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 36.dp)
                ) {
                    Column {
                        adventureGames.chunked(4).forEach { rowItems ->
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

            // Fighting game title
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Fighting game",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 40.dp, top = 24.dp, bottom = 12.dp)
                )
            }

            // Fighting game items
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

@Composable
fun GameItem(game: GameModel, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        modifier = Modifier
            .fillMaxWidth()
            .scaleClickEffect(onClick)
    ) {
        Column(
            modifier = Modifier.padding(4.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.66f)
            ) {
                Image(
                    painter = painterResource(id = game.imageRes),
                    contentDescription = game.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = game.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = game.description,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_windows),
                    contentDescription = "Windows",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_steam),
                    contentDescription = "Steam",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiscoveryScreenPreview() {
    GenStreamTheme {
        DiscoveryScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun GameItemPreview() {
    GenStreamTheme {
        GameItem(
            game = GameModel(
                title = "Atomic Heart",
                description = "Stream game Recommendation",
                imageRes = R.drawable.image_1
            ),
            onClick = {}
        )
    }
}

