package network.ermis.genstreamui.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import network.ermis.genstreamui.compose.R
import network.ermis.genstreamui.compose.ui.theme.GenStreamTheme
import network.ermis.genstreamui.compose.ui.theme.TextPrimary
import network.ermis.genstreamui.compose.ui.theme.TextSecondary

@Composable
fun MainScreen() {
    val pagerState = rememberPagerState(initialPage = 1) { 3 }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // Horizontal Pager for Screens (Mine, Discovery, Find Game)
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> MineScreen()
                1 -> DiscoveryScreen()
                2 -> FindGameScreen()
            }
        }

        // Overlay Top Bar matching XML layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Menu button
            Image(
                painter = painterResource(id = R.drawable.ic_menu),
                contentDescription = "Menu",
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { /* Handle Menu Click */ }
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Custom Tabs
            val tabs = listOf("Mine", "Discovery", "Find game")
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = pagerState.currentPage == index
                    Text(
                        text = title,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                            .padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Status bar battery, wifi group
            Image(
                painter = painterResource(id = R.drawable.ic_status_group),
                contentDescription = "Status",
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.wrapContentSize()
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Time
            Text(
                text = "9:41",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    GenStreamTheme {
        MainScreen()
    }
}

