package com.example.nambukhwangdan.data.repository

import com.example.nambukhwangdan.data.local.DiaryDao
import com.example.nambukhwangdan.data.remote.DiaryRemoteDataSource
import com.example.nambukhwangdan.model.DiaryEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepository @Inject constructor(
    private val diaryDao: DiaryDao,
    private val diaryRemoteDataSource: DiaryRemoteDataSource,
) { // ◀ @Inject 추가!

    // Dao를 통해 DB에 접근하는 함수들
    fun getAllDiaries() = diaryDao.getAllDiaries()

    suspend fun insertDiary(diary: DiaryEntity) {
        diaryDao.insertDiary(diary)
    }

    suspend fun insertDiaries(diaries: List<DiaryEntity>) {
        diaryDao.insertDiaries(diaries)
    }

    suspend fun getAllDiariesOnce(): List<DiaryEntity> = diaryDao.getAllDiariesOnce()

    suspend fun deleteDiaryById(id: String) {
        diaryDao.deleteDiaryById(id)
    }

    suspend fun toggleLike(id: String) {
        diaryDao.toggleLike(id)
    }

    fun observeRemoteDiaries(userId: String) = diaryRemoteDataSource.observeDiaries(userId)
}
