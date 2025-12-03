package com.example.nambukhwangdan.di

import android.content.Context
import androidx.room.Room
import com.example.nambukhwangdan.data.local.AppDatabase
import com.example.nambukhwangdan.data.local.DiaryDao
import com.example.nambukhwangdan.data.local.LetterDao
import com.example.nambukhwangdan.data.repository.DiaryRepository
import com.example.nambukhwangdan.data.repository.LetterRepository
import com.example.nambukhwangdan.data.util.NetworkMonitor
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

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor =
        NetworkMonitor(context)

    @Provides
    @Singleton
    fun provideDiaryRepository(
        diaryDao: DiaryDao,
        firestore: FirebaseFirestore,
        networkMonitor: NetworkMonitor
    ): DiaryRepository = DiaryRepository(diaryDao, firestore, networkMonitor)

    @Provides
    @Singleton
    fun provideLetterRepository(
        firestore: FirebaseFirestore,
        letterDao: LetterDao,
        networkMonitor: NetworkMonitor
    ): LetterRepository = LetterRepository(firestore, letterDao, networkMonitor)
}

