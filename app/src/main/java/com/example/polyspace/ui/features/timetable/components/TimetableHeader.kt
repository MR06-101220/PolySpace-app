package com.example.polyspace.ui.features.timetable.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TimetableHeader(
    visibleDate: LocalDate,
    onDateClick: () -> Unit,
    onViewMenuClick: () -> Unit,
    showViewMenu: Boolean,
    onDismissMenu: () -> Unit,
    targetDaysVisible: Float,
    onViewOptionSelected: (Float) -> Unit,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onDateClick() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = visibleDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH))
                    .replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
        Box(contentAlignment = Alignment.TopEnd) {
            IconButton(onClick = onViewMenuClick) {
                Icon(Icons.Default.ViewWeek, "Vue", tint = MaterialTheme.colorScheme.onSurface)
            }

            if (showViewMenu) {
                IOSStylePopupMenu(
                    onDismissRequest = onDismissMenu,
                    content = { closeMenu ->
                        val options = listOf("1 Jour" to 1f, "2 Jours" to 2f, "3 Jours" to 3f, "Semaine" to 5f)
                        Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                            options.forEach { (label, value) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            label,
                                            fontWeight = if (targetDaysVisible == value) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onViewOptionSelected(value)
                                        closeMenu()
                                    },
                                    trailingIcon = if (targetDaysVisible == value) {
                                        { Icon(Icons.Default.Check, null) }
                                    } else null,
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Actualiser",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Refresh,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                onClick = {
                                    onRefreshClick()
                                    closeMenu()
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun IOSStylePopupMenu(
    onDismissRequest: () -> Unit,
    content: @Composable (closeMenu: () -> Unit) -> Unit
) {
    val expandedState = remember { MutableTransitionState(false) }
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        expandedState.targetState = true
    }

    fun triggerClose() {
        expandedState.targetState = false
    }

    LaunchedEffect(expandedState.currentState) {
        if (!expandedState.currentState && !expandedState.targetState) {
            onDismissRequest()
        }
    }

    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(0, with(density) { 45.dp.roundToPx() }),
        onDismissRequest = { triggerClose() },
        properties = PopupProperties(focusable = true)
    ) {
        AnimatedVisibility(
            visibleState = expandedState,
            enter = scaleIn(
                transformOrigin = TransformOrigin(1f, 0f),
                animationSpec = tween(durationMillis = 250)
            ) + fadeIn(tween(150)),
            exit = scaleOut(
                transformOrigin = TransformOrigin(1f, 0f),
                animationSpec = tween(durationMillis = 350)
            ) + fadeOut(tween(300))
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                content(::triggerClose)
            }
        }
    }
}