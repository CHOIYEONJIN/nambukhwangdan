
package com.example.nambukhwangdan.data.local
import com.example.nambukhwangdan.data.local.DiaryDao
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.nambukhwangdan.model.Converters
import com.example.nambukhwangdan.model.DiaryEntity

@Database(
    entities = [DiaryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
}
