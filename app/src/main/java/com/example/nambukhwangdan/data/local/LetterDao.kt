// Room DAO: 편지 테이블 CRUD와 동기화에 필요한 쿼리를 담당한다.
// Firestore ↔ Room 동기화 시 로컬 변경분을 저장하고, Flow를 통해 UI로 스트리밍한다.
package com.example.nambukhwangdan.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nambukhwangdan.model.Letter.LetterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LetterDao {

    // 전체 편지를 최신 예약 시간 순으로 제공. Flow 덕분에 DB 변경이 곧바로 Compose UI에 반영된다.
    @Query("SELECT * FROM letters ORDER BY scheduledAt DESC")
    fun getAllLetters(): Flow<List<LetterEntity>>

    // 단일 편지를 upsert. 로컬 작성 → UI 즉시 반영 후 Firestore 업로드에 사용.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLetter(letter: LetterEntity)

    // 여러 편지를 한꺼번에 upsert. 스냅샷 리스너로 가져온 원격 데이터를 덮어쓴다.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLetters(letters: List<LetterEntity>)

    // 특정 편지를 삭제. 원격에서 삭제된 경우 로컬을 정리한다.
    @Query("DELETE FROM letters WHERE id = :id")
    suspend fun deleteLetter(id: String)

    // 좋아요 토글. updatedAt을 함께 갱신하여 충돌 시 최신 타임스탬프를 비교할 근거 제공.
    @Query("UPDATE letters SET liked = NOT liked, updatedAt = :updatedAt WHERE id = :id")
    suspend fun toggleLike(id: String, updatedAt: Long)

    // 한 번만 조회할 때 사용. 동기화 pending 목록을 계산하거나 충돌 해결 시 사용 가능.
    @Query("SELECT * FROM letters")
    suspend fun getLettersOnce(): List<LetterEntity>
}
