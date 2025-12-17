package com.example.nambukhwangdan.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nambukhwangdan.model.Letter.LetterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LetterDao {
    @Query("SELECT * FROM letters WHERE isReplied = 0 ORDER BY createdAt DESC")
    fun getAllLetters(): Flow<List<LetterEntity>> // 함수 이름은 기존대로 유지하여 ViewModel 수정 최소화

    @Query("UPDATE letters SET isReplied = :isReplied WHERE id = :letterId")
    suspend fun updateLetterRepliedStatus(letterId: String, isReplied: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLetter(letter: LetterEntity)

    @Query("DELETE FROM letters WHERE id = :id")
    suspend fun deleteLetter(id: String)

    @Query("UPDATE letters SET liked = NOT liked WHERE id = :id")
    suspend fun toggleLike(id: String)

    @Query("SELECT COUNT(*) > 0 FROM letters WHERE id = :id")
    suspend fun exists(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLetters(letters: List<LetterEntity>)
}

