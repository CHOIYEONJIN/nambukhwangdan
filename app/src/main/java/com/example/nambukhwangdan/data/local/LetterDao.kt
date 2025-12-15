package com.example.nambukhwangdan.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nambukhwangdan.model.Letter.LetterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LetterDao {

    @Query("SELECT * FROM letters ORDER BY createdAt DESC")
    fun getAllLetters(): Flow<List<LetterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLetter(letter: LetterEntity)

    @Query("DELETE FROM letters WHERE id = :id")
    suspend fun deleteLetter(id: String)

    @Query("UPDATE letters SET liked = NOT liked WHERE id = :id")
    suspend fun toggleLike(id: String)

    @Query("SELECT COUNT(*) > 0 FROM letters WHERE id = :id")
    suspend fun exists(id: String): Boolean
}

