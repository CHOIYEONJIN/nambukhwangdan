package com.example.nambukhwangdan.data.repository

import com.example.nambukhwangdan.data.local.DiaryDao
import com.example.nambukhwangdan.model.Diary.Diary
import com.example.nambukhwangdan.model.Diary.toDiary
import com.example.nambukhwangdan.model.Diary.toEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepository @Inject constructor(
    private val diaryDao: DiaryDao,
    private val firestore: FirebaseFirestore
) {

    // Dao를 통해 DB에 접근하는 함수들
    fun getAllDiaries() = diaryDao.getAllDiaries().map { list ->
        list.map { it.toDiary() }   // ←🔥 Mapper 적용!!
    }

    suspend fun insertDiary(diary: Diary) {
        diaryDao.insertDiary(diary.toEntity())
    }
    suspend fun deleteDiaryById(id: String) {
        diaryDao.deleteDiaryById(id)
    }
    suspend fun toggleLike(id: String) {
        diaryDao.toggleLike(id)
    }
    // Firestore 저장
    suspend fun saveDiaryToFirestore(diary: Diary, userId: String): Boolean = try {
        firestore.collection("users")
            .document(userId)
            .collection("diaries")
            .document(diary.id)
            .set(diary)
            .await()
        true
    } catch (e: Exception) { false }
    suspend fun saveDiaryForAnalysis(diary: Diary): Boolean = try {
        firestore.collection("diary_entries")     // 함수가 듣고 있는 컬렉션
            .document(diary.id)                  // 여기서 diary.id가 바로 docId!
            .set(
                mapOf(
                    "text" to diary.content,
                    "createdAt" to diary.createdAt,
                    "userId" to diary.userId
                )
            )
            .await()
        true
    } catch (e: Exception) { false }


    // DiaryRepository.kt
    suspend fun deleteDiaryFromFirestore(id: String,userId:String): Boolean = try {
        firestore.collection("users")
            .document(userId)
            .collection("diaries")
            .document(id)
            .delete()
            .await()
        true
    } catch (e: Exception) {
        false
    }

}
