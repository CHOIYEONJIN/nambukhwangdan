package com.example.nambukhwangdan.di

import android.content.Context
import androidx.room.Room
import com.example.nambukhwangdan.data.local.AppDatabase
import com.example.nambukhwangdan.data.local.DiaryDao
import com.example.nambukhwangdan.data.local.LetterDao
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.nambukhwangdan.LetterDeliveryReceiver
import com.example.nambukhwangdan.data.local.MIGRATION_11_12
import com.example.nambukhwangdan.data.local.TomorrowLetterDao // ⭐️ TomorrowLetterDao 주입을 위한 임포트

val MIGRATION_10_11: Migration = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE letters ADD COLUMN isReplied INTEGER NOT NULL DEFAULT 0"
        )
    }
}
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "diary_database"
        )
            .addMigrations(MIGRATION_10_11, MIGRATION_11_12)
            .build()

    // 1. Diary DAO 주입
    @Singleton
    @Provides
    fun provideDiaryDao(db: AppDatabase): DiaryDao =
        db.diaryDao()

    // 2. Letter DAO 주입
    @Provides
    @Singleton
    fun provideLetterDao(appDatabase: AppDatabase): LetterDao =
        appDatabase.letterDao()

    // ⭐️ 3. TomorrowLetter DAO 주입 (이제 Repository가 이 DAO를 사용합니다)
    @Provides
    @Singleton
    fun provideTomorrowLetterDao(appDatabase: AppDatabase): TomorrowLetterDao =
        appDatabase.tomorrowLetterDao()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore =
        FirebaseFirestore.getInstance()

    // 4. NotificationManager 주입 및 채널 생성
    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManagerCompat {
        val manager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                LetterDeliveryReceiver.CHANNEL_ID,
                "미래 편지 도착",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }
        return manager
    }
}