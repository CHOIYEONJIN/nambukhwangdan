package com.example.nambukhwangdan.data.repository

// import com.example.nambukhwangdan.model.TomorrowLetter.TomorrowLetter // ❌ 제거
// import com.example.nambukhwangdan.model.TomorrowLetter.TomorrowLetterEntity // ❌ 제거
import com.example.nambukhwangdan.data.local.DiaryDao
import com.example.nambukhwangdan.model.Diary.Diary
import com.example.nambukhwangdan.model.Diary.toDiary
import com.example.nambukhwangdan.model.Diary.toEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


/**
 * [수정됨] 일기(Diary) 데이터에 대한 단일 접근 지점.
 * Room(로컬)과 Firestore(원격) 간의 데이터 동기화 및 변환을 담당합니다.
 */
@Singleton
class DiaryRepository @Inject constructor(
    private val diaryDao: DiaryDao,
    private val firestore: FirebaseFirestore
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // -------------------------------------------------------------
    // Diary - Local (Room) Operations
    // -------------------------------------------------------------

    /** 모든 일기 목록을 Flow로 가져옵니다 (Room -> Diary Model) */
    fun getAllDiaries(): Flow<List<Diary>> = diaryDao.getAllDiaries().map { list ->
        list.map { it.toDiary() }
    }

    /** 단일 일기의 변경 사항을 ID로 Flow로 관찰합니다. */
    fun getDiaryById(id: String): Flow<Diary?> = diaryDao.getDiaryById(id).map { it?.toDiary() }

    /** 특정 월의 일기 목록을 가져옵니다. */
    fun getDiariesByMonth(year: Int, month: Int): Flow<List<Diary>> {
        val start = java.time.LocalDate.of(year, month, 1)
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        // 해당 월의 마지막 날짜까지의 밀리초를 계산합니다.
        val end = start + java.time.YearMonth.of(year, month).lengthOfMonth() * 24L * 60L * 60L * 1000L
        return diaryDao.getDiariesByDateRange(start, end).map { list ->
            list.map { it.toDiary() }
        }
    }

    // -------------------------------------------------------------
    // Diary - Insert/Update/Delete (Room & Firestore)
    // -------------------------------------------------------------

    /** 새로운 일기를 Room에 삽입하고, Firestore에도 저장합니다. */
    suspend fun insertDiary(diary: Diary) {
        val updatedAt = diary.updatedAt.takeIf { it > 0 } ?: System.currentTimeMillis()
        val updated = diary.copy(updatedAt = updatedAt)
        diaryDao.insertDiary(updated.toEntity())

        auth.currentUser?.uid?.let { uid ->
            saveDiaryToFirestore(updated, uid)
        }
    }

    /** 여러 일기를 Room에 삽입합니다. (주로 동기화에 사용) */
    suspend fun insertDiaries(diaries: List<Diary>) {
        val current = diaryDao.getAllDiariesOnce().associateBy { it.id }
        val candidates = diaries.filter { diary ->
            val local = current[diary.id]?.toDiary()
            // 로컬에 없거나, 원격 일기가 더 최신인 경우에만 삽입/교체
            local == null || diary.updatedAt >= local.updatedAt
        }
        if (candidates.isNotEmpty()) {
            diaryDao.insertDiaries(candidates.map { it.toEntity() })
        }
    }

    /** 일기를 업데이트하고 Room과 Firestore에 반영합니다. */
    suspend fun updateDiary(diary: Diary) {
        val updated = diary.copy(updatedAt = System.currentTimeMillis())
        diaryDao.insertDiary(updated.toEntity())
        auth.currentUser?.uid?.let { uid ->
            saveDiaryToFirestore(updated, uid)
        }
    }

    /** 일기의 '좋아요(liked)' 상태를 토글하고 Room과 Firestore에 반영합니다. */
    suspend fun toggleLike(id: String) {
        val target = diaryDao.getDiaryByIdOnce(id)?.toDiary() ?: return
        val updated = target.copy(liked = !target.liked, updatedAt = System.currentTimeMillis())
        diaryDao.insertDiary(updated.toEntity())
        auth.currentUser?.uid?.let { uid ->
            saveDiaryToFirestore(updated, uid)
        }
    }

    /** 일기를 Room과 Firestore에서 삭제합니다. */
    suspend fun deleteDiary(id: String) {
        diaryDao.deleteDiaryById(id)
        auth.currentUser?.uid?.let { uid ->
            deleteDiaryFromFirestore(id, uid)
        }
    }

    // -------------------------------------------------------------
    // Diary - Remote (Firestore) Operations
    // -------------------------------------------------------------

    /** Firestore 사용자 일기 컬렉션 경로를 반환합니다. */
    private fun userCollection(userId: String) =
        firestore.collection("users")
            .document(userId)
            .collection("diaries")

    /** Firestore에 일기 데이터를 저장/업데이트합니다. */
    suspend fun saveDiaryToFirestore(diary: Diary, userId: String? = auth.currentUser?.uid): Boolean = try {
        val uid = userId ?: return false
        userCollection(uid)
            .document(diary.id)
            .set(diary.toFirestoreMap())
            .await()
        true
    } catch (e: Exception) { false }

    /** 분석 목적으로 별도의 컬렉션에 일기 핵심 데이터를 저장합니다. */
    suspend fun saveDiaryForAnalysis(diary: Diary): Boolean = try {
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
    } catch (e: Exception) { false }

    /** Firestore에서 일기 데이터를 삭제합니다. */
    suspend fun deleteDiaryFromFirestore(id: String, userId: String? = auth.currentUser?.uid): Boolean = try {
        val uid = userId ?: return false
        userCollection(uid)
            .document(id)
            .delete()
            .await()
        true
    } catch (e: Exception) {
        false
    }

    /** Firestore의 원격 일기 목록을 실시간으로 관찰합니다. */
    fun observeRemoteDiaries(): Flow<List<Diary>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return callbackFlow {
            val registration = userCollection(uid)
                .addSnapshotListener { snapshot, _ ->
                    // Firestore DocumentSnapshot -> Diary Model로 변환
                    val diaries = snapshot?.documents?.mapNotNull { it.toDiarySafe() } ?: emptyList()
                    trySend(diaries)
                }
            awaitClose { registration.remove() } // Flow가 닫힐 때 리스너를 제거합니다.
        }
    }

    /** 로컬 일기와 원격 일기 간의 동기화를 수행합니다. */
    suspend fun syncDiaries() {
        val uid = auth.currentUser?.uid ?: return
        val remoteSnapshot = userCollection(uid).get().await()
        val remoteDiaries = remoteSnapshot.documents.mapNotNull { it.toDiarySafe() }
        val localDiaries = diaryDao.getAllDiariesOnce().map { it.toDiary() }

        val remoteMap = remoteDiaries.associateBy { it.id }
        val localMap = localDiaries.associateBy { it.id }

        // 1. 로컬이 원격보다 최신이면 원격에 저장 (Local -> Remote)
        localDiaries.forEach { local ->
            val remote = remoteMap[local.id]
            if (remote == null || local.updatedAt > remote.updatedAt) {
                saveDiaryToFirestore(local, uid)
            }
        }

        // 2. 원격이 로컬보다 최신이면 로컬에 삽입 (Remote -> Local)
        val toInsert = remoteDiaries.mapNotNull { remote ->
            val local = localMap[remote.id]
            if (local == null || remote.updatedAt > local.updatedAt) remote else null
        }

        if (toInsert.isNotEmpty()) {
            diaryDao.insertDiaries(toInsert.map { it.toEntity() })
        }
    }

    suspend fun getDiaryByIdOnce(id: String): Diary? {
        return diaryDao.getDiaryByIdOnce(id)?.toDiary()
    }

    // -------------------------------------------------------------
    // Private Extension Functions for Data Mapping
    // -------------------------------------------------------------

    /** Diary Model을 Firestore 직렬화를 위한 Map으로 변환합니다. */
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
        "userId" to userId,
        "updatedAt" to updatedAt,
        "sentiment" to sentimentLabel,
        "score" to sentimentScore
    )

    /** Firestore DocumentSnapshot을 Diary Model로 안전하게 변환합니다. */
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
            userId = getString("userId") ?: "",
            updatedAt = getLong("updatedAt") ?: (getLong("createdAt") ?: System.currentTimeMillis()),
            sentimentLabel = getString("sentiment") ?: getString("sentimentLabel"),
            sentimentScore = (getDouble("score") ?: getDouble("sentimentScore"))?.toFloat()
        )
    } catch (_: Exception) {
        null
    }
    private fun ensureUserDocument(uid: String) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("users")
            .document(uid)
            .set(
                mapOf(
                    "createdAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
    }
}