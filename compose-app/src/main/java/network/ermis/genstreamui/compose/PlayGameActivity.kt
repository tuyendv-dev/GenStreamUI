package network.ermis.genstreamui.compose

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import network.ermis.genstreamui.compose.ui.theme.GenStreamTheme
import network.ermis.genstreamui.compose.ui.theme.TextPrimary
import network.ermis.genstreamui.compose.ui.theme.TextSecondary

class PlayGameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Immersive landscape setup
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Read extras or use fallbacks
        val gameTitle = intent.getStringExtra("GAME_TITLE") ?: "Call Duty"
        val gameDesc = intent.getStringExtra("GAME_DESC") ?: "Unveil the Hidden Truth in Black Myth: Call Duty become the legend"
        val gameImage = intent.getIntExtra("GAME_IMAGE", R.drawable.image_1)

        setContent {
            GenStreamTheme {
                PlayGameScreen(
                    title = gameTitle,
                    description = gameDesc,
                    imageRes = gameImage
                )
            }
        }
    }
}

@Composable
fun PlayGameScreen(title: String, description: String, imageRes: Int) {
    val activity = LocalContext.current as? Activity
    
    // Zoom-out animation for artwork
    var artworkScale by remember { mutableStateOf(1.3f) }
    val animatedScale by animateFloatAsState(
        targetValue = artworkScale,
        animationSpec = tween(1500),
        label = "artworkScale"
    )
    LaunchedEffect(Unit) {
        artworkScale = 1.0f
    }

    // Compatibility badge visibility toggle
    var isBadgeVisible by remember { mutableStateOf(false) }

    // Sequential fade-in + slide-up animations for game details
    val alphas = remember { List(4) { Animatable(0f) } }
    val translationsY = remember { List(4) { Animatable(30f) } }

    LaunchedEffect(Unit) {
        val animDuration = 500
        val staggerDelay = 333L
        for (i in 0 until 4) {
            launch {
                alphas[i].animateTo(1f, animationSpec = tween(animDuration))
            }
            launch {
                translationsY[i].animateTo(0f, animationSpec = tween(animDuration))
            }
            delay(staggerDelay)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // Clicking anywhere else closes the badge
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isBadgeVisible = false
            }
    ) {
        // Right section: Game artwork (takes up 65% width)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.65f)
                .align(Alignment.CenterEnd)
                .clip(RoundedCornerShape(0.dp))
                // Prevent badge close on direct clicks if needed, or let it close
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isBadgeVisible = false
                }
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Artwork",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    }
            )
            // Blending Gradient: Horizontal black-to-transparent overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Black, Color.Transparent),
                            startX = 0f,
                            endX = Float.POSITIVE_INFINITY
                        )
                    )
            )
        }

        // Top Bar Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back navigation button
            Image(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "Back",
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier
                    .size(32.dp)
                    .clickable { activity?.finish() }
                    .padding(4.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Status battery icon
            Image(
                painter = painterResource(id = R.drawable.ic_status_group),
                contentDescription = "Status",
                colorFilter = ColorFilter.tint(Color.White)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Clock
            Text(
                text = "9:41",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Left Section: Game Details column
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(450.dp) // Bound the width to prevent layout overflow into the right artwork
                .padding(start = 54.dp, end = 20.dp)
                .align(Alignment.CenterStart),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Item 0: Title
            Text(
                text = title,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer {
                    alpha = alphas[0].value
                    translationY = translationsY[0].value
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Item 1: Description
            Text(
                text = description,
                color = TextPrimary,
                fontSize = 14.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = alphas[1].value
                    translationY = translationsY[1].value
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Item 2: Platforms row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.graphicsLayer {
                    alpha = alphas[2].value
                    translationY = translationsY[2].value
                }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_windows),
                    contentDescription = "Windows",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_steam),
                    contentDescription = "Steam",
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(52.dp))

            // Item 3: Buttons row & Compatibility badge
            Column(
                modifier = Modifier.graphicsLayer {
                    alpha = alphas[3].value
                    translationY = translationsY[3].value
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play Now button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(width = 235.dp, height = 44.dp)
                            .background(Color.White, RoundedCornerShape(22.dp)) // pill shape
                            .scaleClickEffect { /* Play action */ }
                    ) {
                        Text(
                            text = "Play now",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // More (...) button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(width = 44.dp, height = 44.dp)
                            .background(Color.Transparent, RoundedCornerShape(22.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
                            .scaleClickEffect {
                                isBadgeVisible = !isBadgeVisible
                            }
                    ) {
                        Text(
                            text = "•••",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Check Compatibility Badge
                AnimatedVisibility(
                    visible = isBadgeVisible,
                    enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { 20 },
                    exit = fadeOut(tween(200)) + slideOutVertically(tween(200)) { 20 }
                ) {
                    Box(
                        modifier = Modifier
                            .padding(start = 120.dp, top = 6.dp) // Placed close to More button
                            .background(Color(0xFF262424), RoundedCornerShape(6.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .clickable { /* Check Compatibility Action */ }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Check Compatibility",
                            color = Color.White,
                            fontSize = 14.sp
                        )
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
        )
    }
}
