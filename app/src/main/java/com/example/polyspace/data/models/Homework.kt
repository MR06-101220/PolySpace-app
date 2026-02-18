package com.example.polyspace.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

enum class Priority(val value: Int) {
    LOW(1),
    NORMAL(2),
    URGENT(3)
}

data class SubTask(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    var isDone: Boolean = false
)

@Entity(tableName = "homework_table")
data class Homework(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,
    val description: String = "",
    val subject: String? = null,

    val dueDate: LocalDate? = null,
    val priority: Priority = Priority.NORMAL,

    val subTasks: List<SubTask> = emptyList(),

    val isDone: Boolean = false,
    val creationDate: Long = System.currentTimeMillis()
)