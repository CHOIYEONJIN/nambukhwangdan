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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
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
        }

        // PendingIntent 정의
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            letterId.hashCode(),
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
            FirebaseFunctionsSource().pickRandomUser() // ⭐️ 랜덤 유저 ID 가져오기
        } catch (e: Exception) {
            Log.e("LetterRepo", "❌ pickRandomUser Cloud Function 호출 실패", e)
            null // 실패 시 null 반환
        }

        // 🚨 1. 수신자 ID 유효성 검사
        if (receiverId.isNullOrEmpty()) {
            Log.e("LetterRepo", "❌ 랜덤 수신자를 찾을 수 없습니다. (유저 수 부족 또는 서버 오류)")
            return // 전송 중단
        }

        val letterId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        // ⭐️ Letter 모델을 사용하여 데이터 일관성 유지
        val baseLetter = Letter(
            id = letterId,
            content = content,
            nickname = senderNickname,
            createdAt = now,
            date = now, // 즉시 도착 (도착 시간)
            userId = receiverId, // ⭐️ 이 편지의 '소유자' ID (컬렉션 주인)
            replyToId = null,
            liked = false
        )

        // 2. 🔥 [수신자에게 저장]: 'letters' (받은 편지함)
        try {
            firestore.collection("users")
                .document(receiverId)
                .collection("letters")
                .document(letterId)
                .set(baseLetter) // ⭐️ Letter 모델 객체 저장
                .await()
            Log.d("LetterRepo", "✅ 수신자($receiverId)에게 편지 저장 성공")
        } catch (e: Exception) {
            Log.e("LetterRepo", "❌ 수신자에게 편지 저장 실패", e)
            // 수신자에게 저장 실패 시 발신자에게도 저장하지 않도록 여기서 return 가능
            return
        }

        // 3. 🔥 [발신자에게 저장]: 'sent_letters' (보낸 편지 기록)
        // 보낸 사람 컬렉션에 저장할 때는 userId를 senderId로 변경하여 저장
        val letterForSender = baseLetter.copy(userId = senderId)

        try {
            firestore.collection("users")
                .document(senderId)
                .collection("sent_letters") // ⭐️ 'sent_letters' 컬렉션 사용
                .document(letterId)
                .set(letterForSender) // ⭐️ Letter 모델 객체 저장
                .await()
            Log.d("LetterRepo", "✅ 발신자($senderId)에게 편지 저장 성공")
        } catch (e: Exception) {
            Log.e("LetterRepo", "❌ 발신자에게 편지 저장 실패", e)
        }
    }
    suspend fun existsLetter(id: String): Boolean {
        return letterDao.exists(id)
    }


}
