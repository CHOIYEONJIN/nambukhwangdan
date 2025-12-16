package com.example.nambukhwangdan.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.nambukhwangdan.model.Diary.DiaryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 일기(DiaryEntity)에 대한 데이터 액세스 객체(DAO).
 */
@Dao
interface DiaryDao {

    // --- DiaryEntity 관련 쿼리 ---

    @Query("SELECT * FROM diary_table ORDER BY date DESC")
    fun getAllDiaries(): Flow<List<DiaryEntity>>

    @Query("SELECT * FROM diary_table WHERE date = :date")
    suspend fun getDiaryByDate(date: Long): List<DiaryEntity>

    @Query("SELECT * FROM diary_table WHERE id = :id LIMIT 1")
    fun getDiaryById(id: String): Flow<DiaryEntity?>

    @Query("SELECT * FROM diary_table WHERE id = :id LIMIT 1")
    suspend fun getDiaryByIdOnce(id: String): DiaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiary(diary: DiaryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaries(diaries: List<DiaryEntity>)

    @Query("SELECT * FROM diary_table")
    suspend fun getAllDiariesOnce(): List<DiaryEntity>

    @Query("DELETE FROM diary_table WHERE id = :id")
    suspend fun deleteDiaryById(id: String)

    @Query("SELECT * FROM diary_table WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getDiariesByDateRange(start: Long, end: Long): Flow<List<DiaryEntity>>

    @Delete
    suspend fun deleteDiary(diary: DiaryEntity)

    // ⭐️ Diary 관련 업데이트 함수만 유지합니다.
    @Update
    suspend fun updateDiary(diary: DiaryEntity)
}