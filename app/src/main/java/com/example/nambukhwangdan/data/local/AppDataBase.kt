
package com.example.nambukhwangdan.data.local
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.nambukhwangdan.model.Diary.DiaryEntity
import com.example.nambukhwangdan.model.Letter.LetterEntity
import com.example.nambukhwangdan.model.TomorrowLetter.TomorrowLetterEntity

@Database(
    entities = [DiaryEntity::class, LetterEntity::class, TomorrowLetterEntity::class],
    version = 12,        // ← 변경했으면 버전 과거 값보다 꼭 높여야 함
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
    abstract fun letterDao(): LetterDao
    abstract fun tomorrowLetterDao(): TomorrowLetterDao
}

val MIGRATION_10_11: Migration = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // SQL 문을 실행하여 'letters' 테이블에 'isReplied' 컬럼 추가
        // INTEGER NOT NULL DEFAULT 0: SQLite에서 Boolean(false)을 나타냄
        database.execSQL(
            "ALTER TABLE letters ADD COLUMN isReplied INTEGER NOT NULL DEFAULT 0"
        )
    }
}
val MIGRATION_11_12: Migration = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // [핵심] 기존 'isDelivered' 컬럼을 제거하고, 'isArrived'와 'isReplied'를 추가하는 마이그레이션

        // 1. 새 스키마(isArrived, isReplied 포함, isDelivered 제외)로 임시 테이블 생성
        database.execSQL("""
            CREATE TABLE tomorrow_letters_new (
                id TEXT NOT NULL,
                content TEXT NOT NULL,
                deliveryTimestamp INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                user_id TEXT NOT NULL,
                isArrived INTEGER NOT NULL DEFAULT 0,  
                isReplied INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(id)
            )
        """)

        // 2. 기존 데이터 복사 및 필드 대체
        // 기존 isDelivered 값을 새 isReplied 필드로 옮기고, isArrived는 0으로 초기화
        database.execSQL("""
            INSERT INTO tomorrow_letters_new (
                id, content, deliveryTimestamp, createdAt, user_id, isReplied
            )
            SELECT
                id, content, deliveryTimestamp, createdAt, user_id, isDelivered 
            FROM tomorrow_letters
        """)

        // 3. 기존 테이블 삭제 (isDelivered 컬럼이 남아있는 테이블)
        database.execSQL("DROP TABLE tomorrow_letters")

        // 4. 새 테이블 이름 변경
        database.execSQL("ALTER TABLE tomorrow_letters_new RENAME TO tomorrow_letters")
    }
}