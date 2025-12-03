
// Room DAO: 일기 테이블 CRUD 및 동기화 지원 쿼리 정의
// Flow 반환을 통해 DB 변경이 Compose UI에 자동 반영되며, suspend 함수로 코루틴 내에서 안전하게 실행된다.
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

    // 최신 일기 순으로 모두 조회. UI가 지속적으로 관찰하는 Flow 제공
    @Query("SELECT * FROM diary_table ORDER BY date DESC")
    fun getAllDiaries(): Flow<List<DiaryEntity>>

    // 특정 날짜에 작성된 일기 목록 조회 (suspend: DB I/O는 코루틴에서 수행)
    @Query("SELECT * FROM diary_table WHERE date = :date")
    suspend fun getDiaryByDate(date: Long): List<DiaryEntity>

    // ID로 단일 일기 조회. 로컬 캐시 동기화 시 사용
    @Query("SELECT * FROM diary_table WHERE id = :id LIMIT 1")
    suspend fun getDiaryById(id: String): DiaryEntity?

    // 단일 일기 upsert. Firestore 업로드 전에 로컬 상태를 최신으로 유지
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiary(diary: DiaryEntity)

    // 여러 일기 upsert. 스냅샷 리스너 데이터 반영
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaries(diaries: List<DiaryEntity>)

    // 일회성 전체 조회. 충돌 해결이나 pending 목록 계산에 활용
    @Query("SELECT * FROM diary_table")
    suspend fun getAllDiariesOnce(): List<DiaryEntity>

    // ID 기반 삭제. 원격 삭제 이벤트 반영
    @Query("DELETE FROM diary_table WHERE id = :id")
    suspend fun deleteDiaryById(id: String)

    // 좋아요 토글. 사용자 상호작용 후 updatedAt 비교를 위한 상태 변동
    @Query("UPDATE diary_table SET liked = NOT liked WHERE id = :id")
    suspend fun toggleLike(id: String)

    // 기간 필터 조회. 감정 캘린더 등 분석 화면에서 사용
    @Query("SELECT * FROM diary_table WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getDiariesByDateRange(start: Long, end: Long): Flow<List<DiaryEntity>>

    // 특정 엔티티 삭제. UI에서 스와이프 삭제 등 이벤트 처리 시 사용
    @Delete
    suspend fun deleteDiary(diary: DiaryEntity)
}
