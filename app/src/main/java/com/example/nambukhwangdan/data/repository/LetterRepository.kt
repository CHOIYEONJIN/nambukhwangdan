// 레터 레포지토리
// 역할: ViewModel에 동기화된 편지 데이터를 제공하고 Firestore ↔ Room 간 동기화/충돌 해결/익명 스케줄 전송을 담당한다.
package com.example.nambukhwangdan.data.repository

import com.example.nambukhwangdan.data.local.LetterDao
import com.example.nambukhwangdan.data.util.NetworkMonitor
import com.example.nambukhwangdan.model.Letter.Letter
import com.example.nambukhwangdan.model.Letter.toEntity
import com.example.nambukhwangdan.model.Letter.toFirestoreMap
import com.example.nambukhwangdan.model.Letter.toLetter
import com.example.nambukhwangdan.model.Letter.toLetterSafe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class LetterRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val letterDao: LetterDao,
    private val networkMonitor: NetworkMonitor
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var remoteListeners: List<ListenerRegistration> = emptyList()
    private val syncing = AtomicBoolean(false)

    fun observeLetters(): Flow<List<Letter>> =
        letterDao.getAllLetters().map { list -> list.map { it.toLetter() } }

    fun observeSyncedLetters(): Flow<List<Letter>> =
        networkMonitor.isOnline.flatMapLatest { online ->
            if (online) {
                startSync()
            }
            observeLetters()
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    suspend fun sendLetter(letter: Letter) {
        val enriched = letter.copy(
            updatedAt = System.currentTimeMillis()
        )
        letterDao.insertLetter(enriched.toEntity())
        if (networkMonitor.isOnline.first()) {
            pushLetterToFirestore(enriched)
        }
    }

    suspend fun toggleLike(id: String) {
        val now = System.currentTimeMillis()
        letterDao.toggleLike(id, now)
        val local = letterDao.getLettersOnce().firstOrNull { it.id == id }?.toLetter() ?: return
        if (networkMonitor.isOnline.first()) {
            pushLetterToFirestore(local.copy(updatedAt = now))
        }
    }

    suspend fun deleteLetter(id: String) {
        letterDao.deleteLetter(id)
        val uid = auth.currentUser?.uid ?: return
        if (networkMonitor.isOnline.first()) {
            firestore.collection("users").document(uid)
                .collection("letters").document(id).delete().await()
        }
    }

    suspend fun scheduleAnonymousDelivery(letter: Letter): String? {
        val receiverId = getRandomUserIdExcluding(auth.currentUser?.uid ?: return null) ?: return null
        val scheduledLetter = letter.copy(
            id = letter.id.ifEmpty { UUID.randomUUID().toString() },
            receiverId = receiverId,
            receiverName = null,
            anonymous = true,
            updatedAt = System.currentTimeMillis()
        )
        letterDao.insertLetter(scheduledLetter.toEntity())
        if (networkMonitor.isOnline.first()) {
            firestore.collection("letters")
                .document(scheduledLetter.id)
                .set(scheduledLetter.toFirestoreMap())
                .await()
        }
        return scheduledLetter.id
    }

    suspend fun sendScheduledLetterToSelf(letter: Letter) {
        val uid = auth.currentUser?.uid ?: return
        val scheduled = letter.copy(
            id = letter.id.ifEmpty { UUID.randomUUID().toString() },
            receiverId = uid,
            receiverName = "미래의 나",
            anonymous = false,
            updatedAt = System.currentTimeMillis()
        )
        letterDao.insertLetter(scheduled.toEntity())
        if (networkMonitor.isOnline.first()) {
            pushLetterToFirestore(scheduled)
        }
    }

    fun startSync() {
        val uid = auth.currentUser?.uid ?: return
        if (remoteListeners.isNotEmpty()) return

        val inbox = userInbox(uid)
        val sent = userLetters(uid)
        remoteListeners = listOf(
            inbox.addSnapshotListener { snapshot, _ ->
                val remote = snapshot?.documents?.mapNotNull { it.toLetterSafe() } ?: emptyList()
                scope.launch { applyRemote(remote) }
            },
            sent.addSnapshotListener { snapshot, _ ->
                val remote = snapshot?.documents?.mapNotNull { it.toLetterSafe() } ?: emptyList()
                scope.launch { applyRemote(remote) }
            }
        )

        scope.launch { syncLetters() }
    }

    private suspend fun applyRemote(remoteLetters: List<Letter>) {
        val local = letterDao.getLettersOnce().associateBy { it.id }
        val toInsert = remoteLetters.mapNotNull { remote ->
            val localEntry = local[remote.id]?.toLetter()
            if (localEntry == null || remote.updatedAt >= localEntry.updatedAt) remote else null
        }
        if (toInsert.isNotEmpty()) {
            letterDao.insertLetters(toInsert.map { it.toEntity() })
        }
    }

    suspend fun syncLetters() {
        if (!syncing.compareAndSet(false, true)) return
        try {
            if (!networkMonitor.isOnline.first()) return
            val uid = auth.currentUser?.uid ?: return
            val remoteSnapshot = userLetters(uid).get().await()
            val inboxSnapshot = userInbox(uid).get().await()
            val remoteLetters = (remoteSnapshot.documents + inboxSnapshot.documents)
                .mapNotNull { it.toLetterSafe() }
            val localLetters = letterDao.getLettersOnce().map { it.toLetter() }

            val remoteMap = remoteLetters.associateBy { it.id }

            localLetters.forEach { local ->
                val remote = remoteMap[local.id]
                if (remote == null || local.updatedAt > remote.updatedAt) {
                    pushLetterToFirestore(local)
                }
            }

            val localMap = localLetters.associateBy { it.id }
            val toInsert = remoteLetters.mapNotNull { remote ->
                val local = localMap[remote.id]
                if (local == null || remote.updatedAt > local.updatedAt) remote else null
            }
            if (toInsert.isNotEmpty()) {
                letterDao.insertLetters(toInsert.map { it.toEntity() })
            }
        } finally {
            syncing.set(false)
        }
    }

    private suspend fun pushLetterToFirestore(letter: Letter) {
        if (!networkMonitor.isOnline.first()) return
        val receiverId = letter.receiverId ?: auth.currentUser?.uid ?: return
        val senderId = letter.senderId.ifEmpty { auth.currentUser?.uid ?: "" }
        val payload = letter.copy(senderId = senderId)
        firestore.collection("letters")
            .document(payload.id)
            .set(payload.toFirestoreMap())
            .await()
        firestore.collection("users").document(receiverId)
            .collection("inbox")
            .document(payload.id)
            .set(
                payload.copy(
                    senderName = if (payload.anonymous) "익명" else payload.senderName,
                    senderId = if (payload.anonymous) "" else payload.senderId
                ).toFirestoreMap()
            )
            .await()
        firestore.collection("users").document(senderId)
            .collection("letters")
            .document(payload.id)
            .set(payload.toFirestoreMap())
            .await()
    }

    private suspend fun getRandomUserIdExcluding(currentUserId: String): String? {
        val users = firestore.collection("users")
        val total = users.count().get(AggregateSource.SERVER).await().count
        if (total <= 1) return null

        val randomIndex = Random.nextLong(total)
        val query = users.orderBy(FieldPath.documentId())
            .offset(randomIndex.toInt())
            .limit(1)
            .get()
            .await()
        val candidate = query.documents.firstOrNull { it.id != currentUserId }
        if (candidate != null) return candidate.id

        val fallback = users.whereNotEqualTo(FieldPath.documentId(), currentUserId).limit(1).get().await()
        return fallback.documents.firstOrNull()?.id
    }

    private fun userLetters(userId: String) =
        firestore.collection("users").document(userId).collection("letters")

    private fun userInbox(userId: String) =
        firestore.collection("users").document(userId).collection("inbox")
}
