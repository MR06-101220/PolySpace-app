package com.example.polyspace.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.polyspace.data.models.Homework
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeworkDao {

    // Get all homeworks
    @Query("SELECT * FROM homework_table ORDER BY isDone ASC, dueDate ASC")
    fun getAllHomeworks(): Flow<List<Homework>>

    // Subject filter
    @Query("SELECT * FROM homework_table WHERE subject = :subjectName ORDER BY dueDate ASC")
    fun getHomeworksBySubject(subjectName: String): Flow<List<Homework>>

    // Get all homeworks for a specific subject and filter by isDone
    @Query("SELECT COUNT(*) FROM homework_table WHERE subject = :subjectName AND isDone = 0")
    fun countHomeworksForSubject(subjectName: String): Flow<Int>

    // Get a specific homework by ID
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(homework: Homework): Long

    // Update a homework
    @Update
    suspend fun update(homework: Homework)

    // Delete a homework
    @Delete
    suspend fun delete(homework: Homework)

    // Delete a homework by ID
    @Query("DELETE FROM homework_table WHERE id = :id")
    suspend fun deleteById(id: Long)
}