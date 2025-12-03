package com.example.nambukhwangdan.data.repository

import com.example.nambukhwangdan.data.local.DiaryDao
import com.example.nambukhwangdan.data.util.NetworkMonitor
import com.example.nambukhwangdan.model.Diary.Diary
import com.example.nambukhwangdan.model.Diary.toDiary
import com.example.nambukhwangdan.model.Diary.toEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepository @Inject constructor(
    private val diaryDao: DiaryDao,
    private val firestore: FirebaseFirestore,
    private val networkMonitor: NetworkMonitor
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getAllDiaries() = diaryDao.getAllDiaries().map { list ->
        list.map { it.toDiary() }
    }

    suspend fun insertDiary(diary: Diary) {
        val updatedAt = diary.updatedAt.takeIf { it > 0 } ?: System.currentTimeMillis()
        diaryDao.insertDiary(diary.copy(updatedAt = updatedAt).toEntity())
    }

    suspend fun insertDiaries(diaries: List<Diary>) {
        val current = diaryDao.getAllDiariesOnce().associateBy { it.id }
        val candidates = diaries.filter { diary ->
            val local = current[diary.id]?.toDiary()
            local == null || diary.updatedAt >= local.updatedAt
        }
        if (candidates.isNotEmpty()) {
            diaryDao.insertDiaries(candidates.map { it.toEntity() })
        }
    }

    suspend fun getDiaryById(id: String): Diary? = diaryDao.getDiaryById(id)?.toDiary()

    suspend fun deleteDiaryById(id: String) {
        diaryDao.deleteDiaryById(id)
    }

    suspend fun toggleLike(id: String) {
        val target = diaryDao.getDiaryById(id)?.toDiary() ?: return
        val updated = target.copy(liked = !target.liked, updatedAt = System.currentTimeMillis())
        diaryDao.insertDiary(updated.toEntity())
        auth.currentUser?.uid?.let { uid ->
            if (canSync()) {
                saveDiaryToFirestore(updated, uid)
            }
        }
    }

    private fun userCollection(userId: String) =
        firestore.collection("users")
            .document(userId)
            .collection("diaries")

    suspend fun saveDiaryToFirestore(diary: Diary, userId: String? = auth.currentUser?.uid): Boolean {
        return try {
            val uid = userId ?: return false
            if (!canSync()) return false
            userCollection(uid)
                .document(diary.id)
                .set(diary.toFirestoreMap())
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun saveDiaryForAnalysis(diary: Diary): Boolean {
        return try {
            if (!canSync()) return false
            firestore.collection("diary_entries")
                .document(diary.id)
                .set(
                    mapOf(
                        "text" to diary.content,
                        "createdAt" to diary.createdAt,
                        "userId" to diary.userId
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteDiaryFromFirestore(id: String, userId: String? = auth.currentUser?.uid): Boolean {
        return try {
            val uid = userId ?: return false
            if (!canSync()) return false
            userCollection(uid)
                .document(id)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun observeRemoteDiaries(): Flow<List<Diary>> {
        val uid = auth.currentUser?.uid
        return if (uid == null) {
            flowOf(emptyList())
        } else {
            networkMonitor.isOnline.flatMapLatest { online ->
                if (!online) {
                    flowOf(emptyList())
                } else {
                    callbackFlow {
                        val registration = userCollection(uid)
                            .addSnapshotListener { snapshot, _ ->
                                val diaries = snapshot?.documents?.mapNotNull { it.toDiarySafe() } ?: emptyList()
                                trySend(diaries)
                            }
                        awaitClose { registration.remove() }
                    }
                }
            }
        }
    }

    suspend fun syncDiaries() {
        val uid = auth.currentUser?.uid ?: return
        if (!canSync()) return
        val remoteSnapshot = userCollection(uid).get().await()
        val remoteDiaries = remoteSnapshot.documents.mapNotNull { it.toDiarySafe() }
        val localDiaries = diaryDao.getAllDiariesOnce().map { it.toDiary() }

        val remoteMap = remoteDiaries.associateBy { it.id }
        val localMap = localDiaries.associateBy { it.id }

        localDiaries.forEach { local ->
            val remote = remoteMap[local.id]
            if (remote == null || local.updatedAt > remote.updatedAt) {
                saveDiaryToFirestore(local, uid)
            }
        }

        val toInsert = remoteDiaries.mapNotNull { remote ->
            val local = localMap[remote.id]
            if (local == null || remote.updatedAt > local.updatedAt) remote else null
        }

        if (toInsert.isNotEmpty()) {
            diaryDao.insertDiaries(toInsert.map { it.toEntity() })
        }
    }

    suspend fun updateDiary(diary: Diary) {
        val updated = diary.copy(updatedAt = System.currentTimeMillis())
        diaryDao.insertDiary(updated.toEntity())
        auth.currentUser?.uid?.let { uid ->
            saveDiaryToFirestore(updated, uid)
        }
    }

    suspend fun deleteDiary(id: String) {
        diaryDao.deleteDiaryById(id)
        auth.currentUser?.uid?.let { uid ->
            deleteDiaryFromFirestore(id, uid)
        }
    }

    fun getDiariesByMonth(year: Int, month: Int): Flow<List<Diary>> {
        val start = java.time.LocalDate.of(year, month, 1)
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val end = start + java.time.YearMonth.of(year, month).lengthOfMonth() * 24L * 60L * 60L * 1000L
        return diaryDao.getDiariesByDateRange(start, end).map { list ->
            list.map { it.toDiary() }
        }
    }

    private suspend fun canSync(): Boolean = networkMonitor.isOnline.first()

    private fun Diary.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "content" to content,
        "emotion" to emotion,
        "sticker" to sticker,
        "analyzedAt" to analyzedAt,
        "replyToId" to replyToId,
        "date" to date,
        "createdAt" to createdAt,
        "liked" to liked,
        "nickname" to nickname,
        "userId" to userId,
        "updatedAt" to updatedAt,
        "sentiment" to sentimentLabel,
        "score" to sentimentScore
    )

    private fun DocumentSnapshot.toDiarySafe(): Diary? = try {
        Diary(
            id = getString("id") ?: id,
            content = getString("content") ?: "",
            analyzedAt = getLong("analyzedAt"),
            emotion = getString("emotion") ?: "",
            sticker = getString("sticker"),
            date = getLong("date") ?: System.currentTimeMillis(),
            replyToId = getString("replyToId"),
            createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
            liked = getBoolean("liked") ?: false,
            nickname = getString("nickname") ?: "나",
            userId = getString("userId") ?: "",
            updatedAt = getLong("updatedAt") ?: (getLong("createdAt") ?: System.currentTimeMillis()),
            sentimentLabel = getString("sentiment") ?: getString("sentimentLabel"),
            sentimentScore = (getDouble("score") ?: getDouble("sentimentScore"))?.toFloat()
        )
    } catch (_: Exception) {
        null
    }
}
