package network.ermis.genstreamui.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import network.ermis.genstreamui.compose.R
import network.ermis.genstreamui.compose.ui.theme.GenStreamTheme

@Composable
fun MineScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Text(
            text = "Mine Fragment",
            color = Color.White,
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.Center)
        )

        Image(
            painter = painterResource(id = R.drawable.ic_footer_group),
            contentDescription = "Footer",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MineScreenPreview() {
    GenStreamTheme {
        MineScreen()
    }
}

