package com.example.polyspace.data.local

import androidx.room.TypeConverter
import com.example.polyspace.data.models.SubTask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

class Converters {
    private val gson = Gson()

    // --- Convert date to string ---
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    // --- Convert list of subtasks to string ---
    @TypeConverter
    fun fromSubTaskList(value: List<SubTask>): String {
        return gson.toJson(value)
    }

    // --- Convert string to list of subtasks ---
    @TypeConverter
    fun toSubTaskList(value: String): List<SubTask> {
        val type = object : TypeToken<List<SubTask>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}