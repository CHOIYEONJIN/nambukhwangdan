
package com.example.nambukhwangdan.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nambukhwangdan.model.DiaryEntity
import kotlinx.coroutines.flow.Flow

/*
@Dao
interface DiaryDao {

    @Query("SELECT * FROM diary_table ORDER BY date DESC")
    fun getAllDiaries(): Flow<List<DiaryEntity>>

    @Query("SELECT * FROM diary_table WHERE date = :date")
    suspend fun getDiaryByDate(date: Long): List<DiaryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiary(diary: DiaryEntity)

    @Delete
    suspend fun deleteDiary(diary: DiaryEntity)
}
*/
@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_table ORDER BY date DESC")
    fun getAllDiaries(): Flow<List<DiaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiary(diary: DiaryEntity)
}
