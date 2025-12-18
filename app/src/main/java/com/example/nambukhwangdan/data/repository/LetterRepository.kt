package com.example.nambukhwangdan.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.nambukhwangdan.LetterDeliveryReceiver
import com.example.nambukhwangdan.data.FirebaseFunctionsSource
import com.example.nambukhwangdan.data.local.LetterDao
import com.example.nambukhwangdan.model.Letter.Letter
import com.example.nambukhwangdan.model.Letter.toEntity
import com.example.nambukhwangdan.model.Letter.toLetter
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LetterRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val letterDao: LetterDao,
    @ApplicationContext private val applicationContext: Context) {
    // LetterRepository.kt
    fun getAllLetters(): Flow<List<Letter>> =
        letterDao.getAllLetters().map { list ->
            list.map { it.toLetter() }   // ←🔥 Mapper 적용!!
        }


    suspend fun insertLetter(letter: Letter) {
        letterDao.insertLetter(letter.toEntity())
    }

    suspend fun toggleLike(id: String) = letterDao.toggleLike(id)
    suspend fun saveLetterToFirestore(letter: Letter, userId: String): Boolean = try {
        firestore.collection("users")
            .document(userId)
            .collection("letters")
            .document(letter.id)
            .set(letter)
            .await()
        true
    } catch (e: Exception) {
        false
    }
    // LetterRepository.kt
    suspend fun deleteLetterFromFirestore(id: String, userId: String): Boolean = try {
        firestore.collection("users")
            .document(userId)
            .collection("letters")
            .document(id)
            .delete()
            .await()
        true
    } catch (e: Exception) {
        Log.e("LetterRepo", "Firebase 삭제 오류", e)
        false
    }

    suspend fun deleteLetter(id: String) = letterDao.deleteLetter(id)

    suspend fun deleteLetterFully(id: String, userId: String): Boolean {
        return try {
        cancelScheduledLetter(id)
        letterDao.deleteLetter(id)
        deleteLetterFromFirestore(id, userId)
        } catch (e: Exception) {
            Log.e("LetterRepo", "deleteLetterFully 오류", e)
            false
        }
    }

    suspend fun markLetterAsReplied(letterId: String) {
        // Room DB의 isReplied 플래그를 true로 업데이트 (DAO 함수 호출)
        letterDao.updateLetterRepliedStatus(letterId, true)
        Log.d("LetterRepo", "✅ Letter $letterId marked as replied (Local DB).")
    }
    suspend fun getLetterById(letterId: String): Letter? {
        // letterDao에서 Entity를 가져와서 Letter 객체로 변환합니다.
        return letterDao.getLetterById(letterId)?.toLetter()
    }

    // 편지 쓰기 플로우를 위한 통합 함수
    suspend fun saveAndScheduleLetter(letter: Letter, userId: String) {
        // 1. 로컬 DB에 저장
        letterDao.insertLetter(letter.toEntity())

        // 2. Firebase에 저장 (서버에 업로드)
        saveLetterToFirestore(letter, userId) // userId는 편지를 보낸 사람의 ID

        // 3. 알람 예약 (사용자가 선택한 arrival date를 deliveryTimestamp로 사용)
        scheduleLetterDelivery(letter.id, letter.date) // ⭐️ letter.date 필드를 사용
    }

    // -------------------------------------------------------------
    // ⭐️ 알람 예약 로직 (이전에 성공한 TomorrowRepository 로직 재활용)
    // -------------------------------------------------------------
    private fun scheduleLetterDelivery(letterId: String, deliveryTimestamp: Long) {
        if (deliveryTimestamp <= System.currentTimeMillis()) return
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // [핵심: 권한 체크 및 요청 로직]
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e("LetterRepo", "❌ 정확한 알람 권한이 없어 설정 화면으로 안내합니다.")

                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    // 앱 이름을 명시하여 포커싱
                    data = android.net.Uri.fromParts("package", applicationContext.packageName, null)
                }
                applicationContext.startActivity(intent)
                return
            }
        }

        // 알람 수신자 정의
        val intent = Intent(applicationContext, LetterDeliveryReceiver::class.java).apply {
            putExtra(LetterDeliveryReceiver.EXTRA_LETTER_ID, letterId)
            putExtra(LetterDeliveryReceiver.EXTRA_TYPE, LetterDeliveryReceiver.TYPE_GENERAL) // 👈 타입 지정
        }
        // PendingIntent 정의
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            letterId.hashCode()+2000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 알람 예약
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            deliveryTimestamp,
            pendingIntent
        )
        Log.d("LetterRepo", "⏰ Letter $letterId scheduled for ${Date(deliveryTimestamp)}")
    }
    fun cancelScheduledLetter(letterId: String) {
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(applicationContext, LetterDeliveryReceiver::class.java).apply {
            putExtra(LetterDeliveryReceiver.EXTRA_LETTER_ID, letterId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            letterId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            Log.d("LetterRepo", "❌ Letter $letterId 알람 취소 완료.")
        }
    }
    suspend fun sendRandomLetter(
        senderId: String,
        senderNickname: String,
        content: String
    ) {
        val receiverId = try {
            FirebaseFunctionsSource().pickRandomUser()
        } catch (e: Exception) {
            Log.e("LetterRepo", "❌ pickRandomUser Cloud Function 호출 실패", e)
            null
        }

        if (receiverId.isNullOrEmpty()) {
            Log.e("LetterRepo", "❌ 랜덤 수신자를 찾을 수 없습니다.")
            return
        }

        val letterId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        // 1. ⭐️ 기본 편지 생성 (작성자는 무조건 나 'senderId')
        val baseLetter = Letter(
            id = letterId,
            content = content,
            nickname = senderNickname,
            createdAt = now,
            date = now,
            userId = receiverId, // 🏠 수신자 집으로 배달될 예정이므로 수신자 ID
            writerId = senderId, // ✍️ 작성자는 나! (나중에 답장 받을 주소)
            replyToId = null,
            liked = false,
            isReplied = false
        )

        // 2. 🔥 [수신자에게 저장]: 상대방의 'letters' 컬렉션
        try {
            firestore.collection("users")
                .document(receiverId)
                .collection("letters")
                .document(letterId)
                .set(baseLetter)
                .await()
            Log.d("LetterRepo", "✅ 수신자($receiverId) 집으로 편지 배달 성공")
        } catch (e: Exception) {
            Log.e("LetterRepo", "❌ 수신자 저장 실패", e)
            return
        }

        // 3. 🔥 [발신자에게 저장]: 나의 'sent_letters' 컬렉션 및 로컬 DB
        // 내 보관함에 넣을 때는 '집주인(userId)'만 나로 바꿔서 저장합니다.
        val letterForSender = baseLetter.copy(userId = senderId)

        try {
            // 서버(Firestore) 저장
            firestore.collection("users")
                .document(senderId)
                .collection("sent_letters")
                .document(letterId)
                .set(letterForSender)
                .await()

            // ⭐️ 로컬 DB(Room) 저장 (toEntity()가 writerId를 포함하고 있어야 함)
            letterDao.insertLetter(letterForSender.toEntity())

            Log.d("LetterRepo", "✅ 내 보낸 편지함 및 로컬 DB 저장 완료")
        } catch (e: Exception) {
            Log.e("LetterRepo", "❌ 발신자 기록 저장 실패", e)
        }
    }

    suspend fun sendReplyLetter(replyLetter: Letter, senderId: String) {
        val receiverId = replyLetter.userId // 이미 persistLetter에서 원본의 writerId로 설정됨
        val letterId = replyLetter.id

        // 1. 🔥 [수신자에게 저장]: 상대방의 'letters' 컬렉션
        try {
            firestore.collection("users")
                .document(receiverId)
                .collection("letters")
                .document(letterId)
                .set(replyLetter)
                .await()
            Log.d("LetterRepo", "✅ 답장 배달 성공 (수신자: $receiverId)")
        } catch (e: Exception) {
            Log.e("LetterRepo", "❌ 답장 수신자 저장 실패", e)
            throw e // 실패 시 호출부로 에러 전달
        }

        // 2. 🔥 [발신자(나)에게 저장]: 나의 'sent_letters' 및 로컬 DB
        // 보낸 편지함용 객체 (이미 userId가 receiverId로 되어 있으므로 그대로 저장해도 되지만,
        // 내 보관함 일관성을 위해 userId를 나로 바꾼 복사본을 로컬/보낸편지함에 저장할 수도 있음)
        val letterForSenderRecord = replyLetter.copy(userId = senderId)

        try {
            // 서버 'sent_letters' 저장
            firestore.collection("users")
                .document(senderId)
                .collection("sent_letters")
                .document(letterId)
                .set(letterForSenderRecord)
                .await()

            // 로컬 DB 저장
            letterDao.insertLetter(letterForSenderRecord.toEntity())
            Log.d("LetterRepo", "✅ 답장 보낸 기록 저장 완료")
        } catch (e: Exception) {
            Log.e("LetterRepo", "❌ 답장 발신 기록 저장 실패", e)
        }

        // 3. ⭐️ [원본 편지 상태 업데이트]: 답장 완료 처리
        replyLetter.replyToId?.let { originalId ->
            try {
                // 로컬 DB 업데이트
                letterDao.updateLetterRepliedStatus(originalId, true)

                // (선택사항) 서버의 원본 편지 상태도 바꾸고 싶다면 아래 추가
                firestore.collection("users")
                    .document(senderId)
                    .collection("letters")
                    .document(originalId)
                    .update("isReplied", true)
                    .await()

                Log.d("LetterRepo", "✅ 원본 편지($originalId) 답장 완료 처리 성공")
            } catch (e: Exception) {
                Log.e("LetterRepo", "⚠️ 원본 편지 상태 업데이트 실패", e)
            }
        }
    }
    suspend fun existsLetter(id: String): Boolean {
        return letterDao.exists(id)
    }

    suspend fun syncLettersFromServer(userId: String) {
        withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("users").document(userId)
                    .collection("letters").get().await()

                val letters = snapshot.toObjects(Letter::class.java)
                if (letters.isNotEmpty()) {
                    // List를 통째로 넘기는 DAO 함수를 사용하세요!
                    letterDao.insertLetters(letters.map { it.toEntity() })
                }
            } catch (e: Exception) {
                Log.e("LetterRepo", "Sync Error", e)
            }
        }
    }





}
