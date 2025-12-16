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
            .addMigrations(MIGRATION_10_11)
            .build()
    @Singleton
    @Provides
    fun provideDiaryDao(db: AppDatabase): DiaryDao =
        db.diaryDao()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore =
        FirebaseFirestore.getInstance()


    @Provides
    @Singleton
    fun provideLetterDao(appDatabase: AppDatabase): LetterDao =
        appDatabase.letterDao()

    // ⭐️ 1. NotificationManagerCompat 주입 및 알림 채널 생성
    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManagerCompat {
        val manager = NotificationManagerCompat.from(context)

        // Android 8.0 (Oreo) 이상에서는 알림 채널을 생성해야 합니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                LetterDeliveryReceiver.CHANNEL_ID,
                "미래 편지 도착", // 사용자에게 표시될 알림 채널 이름
                NotificationManager.IMPORTANCE_HIGH
            )
            // 알림 채널 생성
            manager.createNotificationChannel(channel)
        }
        return manager
    }

}

