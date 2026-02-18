package com.example.polyspace.ui.features.timetable.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogWrapper(
    state: DatePickerState,
    isLandscape: Boolean,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val transitionState = remember { MutableTransitionState(false).apply { targetState = true } }
    val scope = rememberCoroutineScope()
    val dialogBackgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val primaryColor = MaterialTheme.colorScheme.primary

    fun closeWithAnimation() {
        transitionState.targetState = false
        scope.launch {
            delay(200)
            onDismiss()
        }
    }

    val selectedMillis = state.selectedDateMillis
    val selectedDateText = remember(selectedMillis) {
        if (selectedMillis != null) {
            val date = Instant.ofEpochMilli(selectedMillis).atZone(ZoneId.of("UTC")).toLocalDate()
            date.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.FRENCH))
                .split(" ")
                .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
        } else {
            "Choisir"
        }
    }

    Dialog(
        onDismissRequest = { closeWithAnimation() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedVisibility(
            visibleState = transitionState,
            enter = scaleIn(initialScale = 0.95f, animationSpec = tween(300)) + fadeIn(tween(200)),
            exit = scaleOut(targetScale = 0.95f, animationSpec = tween(200)) + fadeOut(tween(200))
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = dialogBackgroundColor,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(16.dp)
                    .shadow(24.dp, RoundedCornerShape(28.dp))
                    .heightIn(max = 650.dp)
            ) {
                if (isLandscape) {
                    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(120.dp)
                                .background(primaryColor)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = selectedDateText.replace(" ", "\n"),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Column(modifier = Modifier.width(360.dp)) {
                            Box(modifier = Modifier.height(320.dp)) {
                                DatePickerContent(state, dialogBackgroundColor)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { closeWithAnimation() }) { Text("Annuler") }
                                Button(
                                    onClick = {
                                        state.selectedDateMillis?.let {
                                            onDateSelected(Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate())
                                        }
                                        closeWithAnimation()
                                    },
                                    enabled = state.selectedDateMillis != null
                                ) { Text("Valider") }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.width(340.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, start = 24.dp, end = 12.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedDateText,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { closeWithAnimation() }) {
                                Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .heightIn(max = 380.dp)
                        ) {
                            DatePickerContent(state, dialogBackgroundColor)
                        }
                        Button(
                            onClick = {
                                state.selectedDateMillis?.let {
                                    onDateSelected(Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate())
                                }
                                closeWithAnimation()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = state.selectedDateMillis != null
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Valider", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerContent(state: DatePickerState, backgroundColor: Color) {
    DatePicker(
        state = state,
        title = null,
        headline = null,
        showModeToggle = false,
        colors = DatePickerDefaults.colors(
            containerColor = backgroundColor,
            selectedDayContainerColor = MaterialTheme.colorScheme.primary,
            todayDateBorderColor = MaterialTheme.colorScheme.primary,
            todayContentColor = MaterialTheme.colorScheme.primary
        )
    )
}