
package com.example.nambukhwangdan.data.local
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nambukhwangdan.model.Diary.DiaryEntity
import com.example.nambukhwangdan.model.Letter.LetterEntity
import com.example.nambukhwangdan.model.TomorrowLetter.TomorrowLetterEntity

@Database(
    entities = [DiaryEntity::class, LetterEntity::class, TomorrowLetterEntity::class],
    version = 10,        // ← 변경했으면 버전 과거 값보다 꼭 높여야 함
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
    abstract fun letterDao(): LetterDao
}
