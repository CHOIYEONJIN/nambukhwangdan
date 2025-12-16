
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
    version = 11,        // ← 변경했으면 버전 과거 값보다 꼭 높여야 함
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
    abstract fun letterDao(): LetterDao
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