package com.example.nambukhwangdan.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.nambukhwangdan.model.TomorrowLetter.TomorrowLetterEntity
import kotlinx.coroutines.flow.Flow

/**
 * 미래 편지(TomorrowLetterEntity)에 대한 데이터 액세스 객체(DAO).
 */
@Dao
interface TomorrowLetterDao {

    // --- TomorrowLetterEntity 관련 쿼리 ---

    /**
     * 아직 전달되지 않은 미래 편지 목록을 최신 작성일(createdAt) 기준으로 가져옵니다.
     */
    @Query("SELECT * FROM tomorrow_letters WHERE isReplied = 0 ORDER BY createdAt DESC")
    fun getAllUnDeliveredLetters(): Flow<List<TomorrowLetterEntity>>

    /**
     * 모든 미래 편지를 최신 작성일 기준으로 가져옵니다. (디버깅/전체 목록용)
     */
    @Query("SELECT * FROM tomorrow_letters ORDER BY createdAt DESC")
    fun getAllTomorrowLetters(): Flow<List<TomorrowLetterEntity>>

    /**
     * 새로운 미래 편지를 삽입합니다. 충돌 시 교체합니다.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTomorrowLetter(letter: TomorrowLetterEntity)

    /**
     * 미래 편지 데이터를 업데이트합니다. (isArrived, isReplied 상태 변경 등에 사용)
     * ⭐️ @Update를 사용하면 Room이 정확히 해당 엔티티를 업데이트합니다.
     */
    @Update
    suspend fun updateTomorrowLetter(letter: TomorrowLetterEntity)

    /**
     * 특정 ID를 가진 미래 편지를 한 번만 가져옵니다. (전달/답장 로직에 사용)
     */
    @Query("SELECT * FROM tomorrow_letters WHERE id = :id LIMIT 1")
    suspend fun getTomorrowLetterById(id: String): TomorrowLetterEntity?

    /**
     * 전달 시점(deliveryTimestamp)이 현재 시간보다 빠르거나 같고,
     * 아직 도착하지 않은(isArrived = 0) 편지 목록을 한 번만 가져옵니다. (전달 로직 실행용)
     */
    @Query("SELECT * FROM tomorrow_letters WHERE deliveryTimestamp <= :currentTime AND isArrived = 0")
    suspend fun getDueLettersOnce(currentTime: Long): List<TomorrowLetterEntity>

    // ⭐️ 기존의 UPDATE 쿼리 대신 Repository에서 updateTomorrowLetter을 사용하도록 변경합니다.
    // @Query("UPDATE tomorrow_letters SET isArrived = 1 WHERE id = :letterId")
    // suspend fun markLetterAsArrived(letterId: String)
}