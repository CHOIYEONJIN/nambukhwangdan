package com.example.nambukhwangdan.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import coil.util.CoilUtils.result
import com.example.nambukhwangdan.LetterDeliveryReceiver // ⭐️ Receiver import
import com.example.nambukhwangdan.data.FirebaseFunctionsSource
import com.example.nambukhwangdan.data.local.LetterDao
import com.example.nambukhwangdan.model.Letter.Letter
import com.example.nambukhwangdan.model.Letter.toEntity
import com.example.nambukhwangdan.model.Letter.toLetter
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.Date // Date import
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

    suspend fun deleteLetterFully(id: String, userId: String) {
        letterDao.deleteLetter(id)
        cancelScheduledLetter(id)
        deleteLetterFromFirestore(id, userId)
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
        val receiverId = FirebaseFunctionsSource().pickRandomUser()

        val letter = hashMapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
            "content" to content,
            "nickname" to senderNickname,
            "sendAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("users")
            .document(receiverId)
            .collection("letters")
            .document(UUID.randomUUID().toString())
            .set(letter)
            .await()
    }


}
