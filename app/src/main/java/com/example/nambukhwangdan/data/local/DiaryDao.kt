
package com.example.nambukhwangdan.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nambukhwangdan.model.Diary.DiaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    @Query("SELECT * FROM diary_table ORDER BY date DESC")
    fun getAllDiaries(): Flow<List<DiaryEntity>>

    @Query("SELECT * FROM diary_table WHERE date = :date")
    suspend fun getDiaryByDate(date: Long): List<DiaryEntity>

    @Query("SELECT * FROM diary_table WHERE id = :id LIMIT 1")
    suspend fun getDiaryById(id: String): DiaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiary(diary: DiaryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaries(diaries: List<DiaryEntity>)

    @Query("SELECT * FROM diary_table")
    suspend fun getAllDiariesOnce(): List<DiaryEntity>

    @Query("DELETE FROM diary_table WHERE id = :id")
    suspend fun deleteDiaryById(id: String)

    @Query("UPDATE diary_table SET liked = NOT liked WHERE id = :id")
    suspend fun toggleLike(id: String)

    @Query("SELECT * FROM diary_table WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getDiariesByDateRange(start: Long, end: Long): Flow<List<DiaryEntity>>

    @Delete
    suspend fun deleteDiary(diary: DiaryEntity)
}
