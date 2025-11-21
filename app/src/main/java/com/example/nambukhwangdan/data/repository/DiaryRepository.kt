package com.example.nambukhwangdan.data.repository

import com.example.nambukhwangdan.data.local.DiaryDao
import com.example.nambukhwangdan.model.DiaryEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepository @Inject constructor(private val diaryDao: DiaryDao) { // ◀ @Inject 추가!

    // Dao를 통해 DB에 접근하는 함수들
    fun getAllDiaries() = diaryDao.getAllDiaries()

    suspend fun insertDiary(diary: DiaryEntity) {
        diaryDao.insertDiary(diary)
    }
}
