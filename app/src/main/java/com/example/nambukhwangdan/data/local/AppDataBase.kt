
// Room 데이터베이스를 정의하는 파일
// DiaryEntity와 LetterEntity를 하나의 데이터베이스로 관리하며, DAO를 노출한다.
package com.example.nambukhwangdan.data.local
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nambukhwangdan.model.Diary.DiaryEntity
import com.example.nambukhwangdan.model.Letter.LetterEntity

// @Database: Room이 테이블(엔티티)와 DAO를 묶어 실제 SQLite 테이블을 생성하도록 명시한다.
// version: 스키마 변경 시 마이그레이션 버전을 올려야 데이터 손실 없이 업데이트된다.
@Database(
    entities = [DiaryEntity::class, LetterEntity::class],
    version = 6,        // ← 변경했으면 버전 과거 값보다 꼭 높여야 함
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    // 일기 데이터를 다루는 DAO
    abstract fun diaryDao(): DiaryDao
    // 편지 데이터를 다루는 DAO
    abstract fun letterDao(): LetterDao
}
