
package com.example.nambukhwangdan.data.local
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nambukhwangdan.model.Diary.DiaryEntity
import com.example.nambukhwangdan.model.Letter.LetterEntity

@Database(
    entities = [DiaryEntity::class, LetterEntity::class],
    version = 4,        // ← 변경했으면 버전 과거 값보다 꼭 높여야 함
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
    abstract fun letterDao(): LetterDao
}
