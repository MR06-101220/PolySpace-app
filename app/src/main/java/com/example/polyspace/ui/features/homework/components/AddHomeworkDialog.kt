package com.example.polyspace.ui.features.homework.components

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.polyspace.data.models.Homework
import com.example.polyspace.data.models.Priority
import com.example.polyspace.data.models.SubTask
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHomeworkDialog(
    availableSubjects: List<String>,
    homeworkToEdit: Homework? = null,
    onDismiss: () -> Unit,
    onSave: (Homework) -> Unit
) {
    var title by remember { mutableStateOf(homeworkToEdit?.title ?: "") }
    var description by remember { mutableStateOf(homeworkToEdit?.description ?: "") }
    var selectedSubject by remember { mutableStateOf(homeworkToEdit?.subject) }
    var selectedPriority by remember { mutableStateOf(homeworkToEdit?.priority ?: Priority.NORMAL) }
    var selectedDate by remember { mutableStateOf(homeworkToEdit?.dueDate) }

    var currentSubTaskText by remember { mutableStateOf("") }
    val subTasks = remember {
        mutableStateListOf<SubTask>().apply {
            if (homeworkToEdit != null) addAll(homeworkToEdit.subTasks)
        }
    }

    val subjects = remember(availableSubjects) {
        val list = if (availableSubjects.isEmpty()) listOf("Autre") else availableSubjects + "Autre"
        list.distinct().sorted()
    }
    var expandedSubject by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val calendar = Calendar.getInstance()
    selectedDate?.let { calendar.set(it.year, it.monthValue - 1, it.dayOfMonth) }
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            selectedDate = LocalDate.of(year, month + 1, day)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    var animateIn by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun triggerClose() {
        animateIn = false
        scope.launch {
            delay(200)
            onDismiss()
        }
    }

    LaunchedEffect(Unit) { animateIn = true }

    Dialog(
        onDismissRequest = { triggerClose() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        AnimatedVisibility(
            visible = animateIn,
            enter = scaleIn(
                initialScale = 0.1f,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                transformOrigin = TransformOrigin(1f, 1f)
            ) + fadeIn(tween(200)),
            exit = scaleOut(
                targetScale = 0.1f,
                animationSpec = tween(200),
                transformOrigin = TransformOrigin(1f, 1f)
            ) + fadeOut(tween(200))
        ) {

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(10.dp),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .heightIn(max = 800.dp)
                    .padding(vertical = 24.dp)
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (homeworkToEdit == null) "Nouveau Devoir" else "Modifier",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(onClick = { triggerClose() }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                Icons.Default.Close,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = {
                            Text(
                                "Titre du devoir...",
                                style = MaterialTheme.typography.headlineSmall.copy(color = Color.Gray)
                            )
                        },
                        textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        "Pour quand ?",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val today = LocalDate.now()
                        AssistChip(
                            onClick = { datePickerDialog.show() },
                            label = {
                                Text(
                                    if (selectedDate == null) "Choisir date" else selectedDate!!.format(
                                        DateTimeFormatter.ofPattern("dd MMM")
                                    )
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.CalendarMonth,
                                    null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (selectedDate != null) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                labelColor = if (selectedDate != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            ),
                            border = AssistChipDefaults.assistChipBorder(enabled = true)
                        )

                        QuickDateChip("Demain", today.plusDays(1), selectedDate) {
                            selectedDate = it
                        }
                        QuickDateChip(
                            "Lundi",
                            today.with(TemporalAdjusters.next(DayOfWeek.MONDAY)),
                            selectedDate
                        ) { selectedDate = it }
                        QuickDateChip(
                            "Dans 1 sem.",
                            today.plusWeeks(1),
                            selectedDate
                        ) { selectedDate = it }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Box(modifier = Modifier.weight(1.5f)) {
                            ExposedDropdownMenuBox(
                                expanded = expandedSubject,
                                onExpandedChange = { expandedSubject = !expandedSubject }
                            ) {
                                OutlinedTextField(
                                    value = selectedSubject ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Matière") },
                                    placeholder = { Text("Choisir...") },
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedSubject,
                                    onDismissRequest = { expandedSubject = false },
                                    modifier = Modifier.heightIn(max = 250.dp)
                                ) {
                                    subjects.forEach { subject ->
                                        DropdownMenuItem(
                                            text = { Text(subject) },
                                            onClick = {
                                                selectedSubject = subject; expandedSubject = false
                                            })
                                    }
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            val (pColor, pText) = when (selectedPriority) {
                                Priority.LOW -> Color(0xFF81C784) to "Tranquille"
                                Priority.NORMAL -> Color(0xFF64B5F6) to "Normal"
                                Priority.URGENT -> Color(0xFFE57373) to "Urgent !!"
                            }

                            Surface(
                                onClick = {
                                    selectedPriority = when (selectedPriority) {
                                        Priority.LOW -> Priority.NORMAL
                                        Priority.NORMAL -> Priority.URGENT
                                        Priority.URGENT -> Priority.LOW
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = pColor.copy(alpha = 0.2f),
                                border = null,
                                modifier = Modifier
                                    .height(56.dp)
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        pText,
                                        color = pColor.compositeOver(Color.Black),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Étapes (Optionnel)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = currentSubTaskText,
                            onValueChange = { currentSubTaskText = it },
                            placeholder = { Text("Ex: Lire le chapitre 1...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = CircleShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (currentSubTaskText.isNotBlank()) {
                                    subTasks.add(SubTask(content = currentSubTaskText))
                                    currentSubTaskText = ""
                                }
                            }),
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (currentSubTaskText.isNotBlank()) {
                                        subTasks.add(SubTask(content = currentSubTaskText))
                                        currentSubTaskText = ""
                                    }
                                }) {
                                    Icon(
                                        Icons.Default.ArrowUpward,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                    }

                    if (subTasks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            subTasks.forEach { task ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surfaceContainer,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.RadioButtonUnchecked,
                                        null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        task.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { subTasks.remove(task) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Notes supplémentaires") },
                        leadingIcon = { Icon(Icons.Outlined.Description, null) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val idToUse = homeworkToEdit?.id ?: 0L
                                val isDoneState = homeworkToEdit?.isDone ?: false
                                onSave(
                                    Homework(
                                        id = idToUse,
                                        title = title,
                                        description = description,
                                        subject = selectedSubject,
                                        priority = selectedPriority,
                                        dueDate = selectedDate,
                                        isDone = isDoneState,
                                        subTasks = subTasks.toList()
                                    )
                                )
                                triggerClose()
                            }
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            if (homeworkToEdit == null) "Ajouter ce devoir" else "Sauvegarder",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}