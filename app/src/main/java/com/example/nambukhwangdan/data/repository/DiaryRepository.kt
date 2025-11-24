package com.example.nambukhwangdan.data.repository

import com.example.nambukhwangdan.data.local.DiaryDao
import com.example.nambukhwangdan.model.DiaryEntity
import javax.inject.Inject
import javax.inject.Singleton

// DiaryRepository.kt
@Singleton
class DiaryRepository @Inject constructor(
    private val diaryDao: DiaryDao
) {
    fun getAllDiaries() = diaryDao.getAllDiaries()

    suspend fun insertDiary(diary: DiaryEntity) {
        diaryDao.insertDiary(diary)
    }

    suspend fun toggleLike(id: String) {
        diaryDao.toggleLike(id)
    }


    suspend fun getAllDiariesOnce() = diaryDao.getAllDiariesOnce()

    suspend fun deleteDiaryById(id: String) {
        diaryDao.deleteDiaryById(id)
    }

}

