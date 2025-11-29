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
            .fallbackToDestructiveMigration()   // ← 이거 필수!!!
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
}

