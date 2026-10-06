package com.polyspace.app.data.repository

import com.polyspace.app.data.local.HomeworkDao // Vérifie ton import DAO
import com.polyspace.app.data.models.Homework
import kotlinx.coroutines.flow.Flow

class HomeworkRepository(private val homeworkDao: HomeworkDao) {

    // Exposing the DAO methods as Flow
    val allHomeworks: Flow<List<Homework>> = homeworkDao.getAllHomeworks()

    suspend fun insert(homework: Homework): Long {
        return homeworkDao.insert(homework)
    }

    suspend fun update(homework: Homework) {
        homeworkDao.update(homework)
    }

    suspend fun delete(homework: Homework) {
        homeworkDao.delete(homework)
    }
}