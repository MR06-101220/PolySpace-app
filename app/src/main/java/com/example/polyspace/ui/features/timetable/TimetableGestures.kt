package com.example.polyspace.ui.features.timetable

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

suspend fun PointerInputScope.detectZoomGestures(
    headerHeightPx: Float,
    topPaddingPx: Float,
    verticalState: ScrollState,
    getHourHeight: () -> Dp,
    onZoomChange: (Boolean) -> Unit,
    onScrollEnabledChange: (Boolean) -> Unit,
    onHeightChange: (Dp) -> Unit,
    coroutineScope: CoroutineScope
) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            val pressedChanges = event.changes.filter { it.pressed }
            val isFingerLifting = event.changes.any { it.changedToUp() }

            if (pressedChanges.size >= 2 && !isFingerLifting) {
                onScrollEnabledChange(false)
                onZoomChange(true)

                val zoomChange = event.calculateZoom()
                val centroid = event.calculateCentroid()

                if (zoomChange != 1f) {
                    val currentHourHeight = getHourHeight()
                    val oldHeight = currentHourHeight.value

                    val newHeightRaw = (oldHeight * zoomChange)
                    val newHeightCoerced = newHeightRaw.coerceIn(40f, 200f)

                    if (oldHeight != newHeightCoerced) {
                        val totalScrollableHeight = oldHeight * (TimetableConfig.END_HOUR - TimetableConfig.START_HOUR + 1)
                        val viewportY = centroid.y - headerHeightPx
                        val absoluteContentY = verticalState.value + viewportY - topPaddingPx
                        val fraction = if (totalScrollableHeight > 0) absoluteContentY / totalScrollableHeight else 0f

                        onHeightChange(newHeightCoerced.dp)

                        val newTotalScrollableHeight = newHeightCoerced * (TimetableConfig.END_HOUR - TimetableConfig.START_HOUR + 1)
                        val newAbsoluteContentY = fraction * newTotalScrollableHeight
                        val rawScroll = newAbsoluteContentY + topPaddingPx - viewportY

                        val protectionZone = newHeightCoerced * 5f
                        val fingerPosInDoc = absoluteContentY.coerceAtLeast(0f)
                        val ratio = (fingerPosInDoc / protectionZone).coerceIn(0f, 1f)
                        val stickinessFactor = ratio * ratio * ratio * ratio * ratio
                        val finalScroll = (rawScroll * stickinessFactor).roundToInt()

                        coroutineScope.launch { verticalState.scrollTo(finalScroll) }
                    }
                }
                event.changes.forEach { if (it.pressed) it.consume() }
            }
        } while (event.changes.any { it.pressed })

        onZoomChange(false)
        coroutineScope.launch {
            delay(200)
            onScrollEnabledChange(true)
        }
    }
}