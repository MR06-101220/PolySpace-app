package com.example.polyspace.ui.features.timetable.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyspace.ui.features.timetable.TimetableConfig
import com.example.polyspace.utils.TimetableCalculations
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun CurrentTimePill(hourHeight: Dp) {
    val now = LocalTime.now()
    if (now.hour < TimetableConfig.START_HOUR || now.hour > TimetableConfig.END_HOUR) return

    val centerY = TimetableCalculations.calculateTimeYOffset(now, hourHeight)
    val pillHeight = 24.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = centerY - (pillHeight / 2))
            .height(pillHeight)
            .offset(x = 0.5.dp), contentAlignment = Alignment.CenterEnd
    ) {
        Surface(
            color = TimetableConfig.VIVID_RED,
            shape = RoundedCornerShape(50),
            modifier = Modifier.height(20.dp),
            shadowElevation = 4.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = now.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}