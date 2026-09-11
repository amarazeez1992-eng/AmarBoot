package com.personal.gridbot.amaros.bots

import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AmarBotLabClock(modifier: Modifier = Modifier, color: Color = Color(0xFF9FE8FF)) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(1000) } }
    Text(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(now)), modifier = modifier, color = color, fontSize = 11.sp, fontWeight = FontWeight.Black)
}
