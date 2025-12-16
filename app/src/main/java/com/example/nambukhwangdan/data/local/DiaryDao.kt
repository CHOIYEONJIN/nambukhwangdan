package com.example.nambukhwangdan.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.nambukhwangdan.model.Diary.DiaryEntity
import com.example.nambukhwangdan.model.TomorrowLetter.TomorrowLetterEntity
import kotlinx.coroutines.flow.Flow

/**
 * 일기(DiaryEntity)와 미래 편지(TomorrowLetterEntity)에 대한 데이터 액세스 객체(DAO).
 * 두 엔티티에 대한 CRUD 작업을 통합 관리합니다.
 */
@Dao
interface DiaryDao {

    // --- DiaryEntity 관련 쿼리 ---

    /**
     * 모든 일기를 최신 날짜(date) 기준으로 내림차순 정렬하여 Flow로 가져옵니다. (실시간 업데이트용)
     */
    @Query("SELECT * FROM diary_table ORDER BY date DESC")
    fun getAllDiaries(): Flow<List<DiaryEntity>>

    /**
     * 특정 날짜에 작성된 일기 목록을 가져옵니다.
     */
    @Query("SELECT * FROM diary_table WHERE date = :date")
    suspend fun getDiaryByDate(date: Long): List<DiaryEntity>

    /**
     * [수정됨] 특정 ID를 가진 일기의 변경 사항을 Flow로 관찰합니다. (상세 화면 실시간 업데이트용)
     */
    @Query("SELECT * FROM diary_table WHERE id = :id LIMIT 1")
    fun getDiaryById(id: String): Flow<DiaryEntity?>

    /**
     * [추가됨] 특정 ID를 가진 일기를 한 번만 가져옵니다. (일회성 작업/업데이트에 사용)
     */
    @Query("SELECT * FROM diary_table WHERE id = :id LIMIT 1")
    suspend fun getDiaryByIdOnce(id: String): DiaryEntity?

    /**
     * 새로운 일기를 삽입하거나 충돌 시 교체합니다.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiary(diary: DiaryEntity)

    /**
     * 일기 목록을 한 번에 삽입하거나 충돌 시 교체합니다.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaries(diaries: List<DiaryEntity>)

    /**
     * 모든 일기를 한 번만 가져옵니다. (Flow가 아닌 단일 호출용, 주로 동기화에 사용)
     */
    @Query("SELECT * FROM diary_table")
    suspend fun getAllDiariesOnce(): List<DiaryEntity>

    /**
     * 특정 ID를 가진 일기를 삭제합니다.
     */
    @Query("DELETE FROM diary_table WHERE id = :id")
    suspend fun deleteDiaryById(id: String)

    /**
     * 특정 ID를 가진 일기의 '좋아요' 상태를 토글합니다.
     */
    // 이 함수는 Repository에서 전체 엔티티를 가져와 변경 후 저장하는 로직으로 대체되었으므로 사용하지 않습니다.
    // @Query("UPDATE diary_table SET liked = NOT liked WHERE id = :id")
    // suspend fun toggleLike(id: String)

    /**
     * 특정 기간(start ~ end) 사이에 작성된 일기 목록을 가져옵니다.
     */
    @Query("SELECT * FROM diary_table WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getDiariesByDateRange(start: Long, end: Long): Flow<List<DiaryEntity>>

    /**
     * 주어진 일기 엔티티를 삭제합니다.
     */
    @Delete
    suspend fun deleteDiary(diary: DiaryEntity)

    // --- TomorrowLetterEntity 관련 쿼리 ---

    /**
     * 아직 전달되지 않은 미래 편지 목록을 최신 작성일(createdAt) 기준으로 가져옵니다. (실시간 업데이트용)
     */
    @Query("SELECT * FROM tomorrow_letters WHERE isReplied = 0 ORDER BY createdAt DESC")
    fun getAllUnDeliveredLettersDao(): Flow<List<TomorrowLetterEntity>>

    /**
     * 새로운 미래 편지를 삽입합니다. 충돌 시 교체합니다.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTomorrowLetterDao(letter: TomorrowLetterEntity)

    /**
     * 미래 편지 데이터를 업데이트합니다. (주로 isDelivered 상태 변경에 사용)
     */
    @Update
    suspend fun updateTomorrowLetterDao(letter: TomorrowLetterEntity)

    /**
     * 전달 시점(deliveryTimestamp)이 현재 시간보다 빠르거나 같고,
     * 아직 전달되지 않은(isDelivered = false) 편지 목록을 한 번만 가져옵니다. (전달 로직 실행용)
     */
    @Query("SELECT * FROM tomorrow_letters WHERE deliveryTimestamp <= :currentTime AND isArrived = 0")
    suspend fun getDueLettersOnceDao(currentTime: Long): List<TomorrowLetterEntity>
    @Query("SELECT * FROM tomorrow_letters WHERE id = :id LIMIT 1")
    suspend fun getTomorrowLetterByIdDao(id: String): TomorrowLetterEntity?

    @Query("SELECT * FROM tomorrow_letters ORDER BY createdAt DESC") // ⭐️ 모든 편지 반환 쿼리 추가
    fun getAllTomorrowLettersDao(): Flow<List<TomorrowLetterEntity>>

    @Query("UPDATE tomorrow_letters SET isArrived = 1 WHERE id = :letterId")
    suspend fun markLetterAsArrived(letterId: String)
}