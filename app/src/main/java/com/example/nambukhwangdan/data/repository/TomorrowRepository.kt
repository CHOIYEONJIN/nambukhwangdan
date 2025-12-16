package com.example.nambukhwangdan.data.repository

import android.util.Log
import com.example.nambukhwangdan.data.local.TomorrowLetterDao // ⭐️ 새로운 DAO 임포트
import com.example.nambukhwangdan.model.TomorrowLetter.TomorrowLetter
import com.example.nambukhwangdan.model.TomorrowLetter.TomorrowLetterEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import android.content.Intent
import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date
import android.provider.Settings
import android.net.Uri
import com.example.nambukhwangdan.LetterDeliveryReceiver.Companion.EXTRA_LETTER_ID

/**
 * 미래 편지(TomorrowLetter) 데이터에 대한 접근 지점.
 * Room(로컬)과 Firestore(원격) 간의 데이터 관리를 담당합니다.
 */
@Singleton
class TomorrowLetterRepository @Inject constructor(
    // ⭐️ 주입받는 DAO를 TomorrowLetterDao로 변경
    private val tomorrowLetterDao: TomorrowLetterDao,
    // diaryDao는 제거하거나, Diary 기능이 필요할 때만 남깁니다.
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val applicationContext: Context
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val TAG = "TL_REPO"

    companion object {
        const val EXTRA_LETTER_ID = "EXTRA_LETTER_ID"
    }

    // -------------------------------------------------------------
    // TomorrowLetter - Remote (Firestore) Operations
    // -------------------------------------------------------------

    private fun tomorrowLetterCollection(userId: String) =
        firestore.collection("users")
            .document(userId)
            .collection("tomorrow_letters")

    suspend fun saveTomorrowLetterToFirestore(letter: TomorrowLetter, userId: String? = auth.currentUser?.uid): Boolean = try {
        val uid = userId ?: run {
            Log.e(TAG, "❌ Save Failed: User ID is NULL (Not logged in)")
            return false
        }

        Log.d(TAG, "✅ Saving Letter ${letter.id} for UID: $uid")

        tomorrowLetterCollection(uid)
            .document(letter.id)
            .set(letter.toFirestoreMap())
            .await()

        Log.i(TAG, "🎉 Letter ${letter.id} Saved Successfully.")
        true
    } catch (e: Exception) {
        Log.e(TAG, "🚨 Firestore Save Error for ${letter.id}: ${e.message}", e)
        false
    }

    // -------------------------------------------------------------
    // TomorrowLetter Operations (Room & Firestore)
    // -------------------------------------------------------------

    suspend fun getTomorrowLetterById(id: String): TomorrowLetter? {
        // ⭐️ 수정: tomorrowLetterDao 사용
        return tomorrowLetterDao.getTomorrowLetterById(id)?.toTomorrowLetter()
    }

    fun getAllUnDeliveredLetters(): Flow<List<TomorrowLetter>> =
        // ⭐️ 수정: tomorrowLetterDao 사용
        tomorrowLetterDao.getAllUnDeliveredLetters().map { entities ->
            entities.map { it.toTomorrowLetter() }
        }

    fun getAllTomorrowLetters(): Flow<List<TomorrowLetter>> =
        // ⭐️ 수정: tomorrowLetterDao 사용
        tomorrowLetterDao.getAllTomorrowLetters().map { entities ->
            entities.map { it.toTomorrowLetter() }
        }

    suspend fun insertTomorrowLetter(letter: TomorrowLetter) {
        val correctDeliveryTimestamp = calculateNextDayNineAM()
        val letterToSave = letter.copy(deliveryTimestamp = correctDeliveryTimestamp)

        // ⭐️ 수정: 새로운 DAO 사용
        tomorrowLetterDao.insertTomorrowLetter(letterToSave.toTomorrowLetterEntity())
        Log.d(TAG, "Local (Room) save successful for ${letterToSave.id}")

        auth.currentUser?.uid?.let { uid ->
            saveTomorrowLetterToFirestore(letterToSave, uid)
        } ?: Log.w(TAG, "Attempted Firestore save without valid UID in insertTomorrowLetter.")

        scheduleLetterDelivery(letterToSave.id, correctDeliveryTimestamp)
    }

    suspend fun getDueLettersOnce(currentTime: Long): List<TomorrowLetter> =
        // ⭐️ 수정: tomorrowLetterDao 사용
        tomorrowLetterDao.getDueLettersOnce(currentTime).map { it.toTomorrowLetter() }

    /**
     * 편지가 Diary로 변환되어 전달되었음을 표시하기 위해 isReplied 상태를 업데이트하고 Firestore에도 반영합니다.
     */
    suspend fun markTMLetterAsReplied(letter: TomorrowLetter) {
        val updatedLetter = letter.copy(isReplied = true)
        // ⭐️ 수정: tomorrowLetterDao의 @Update 함수 사용
        tomorrowLetterDao.updateTomorrowLetter(updatedLetter.toTomorrowLetterEntity())

        auth.currentUser?.uid?.let { uid ->
            saveTomorrowLetterToFirestore(updatedLetter, uid)
        }
    }

    /**
     * Receiver에 의해 편지가 도착했음을 표시합니다. (isArrived = true로 변경)
     */
    suspend fun markLetterAsArrived(letter: TomorrowLetter) {
        val updatedLetter = letter.copy(isArrived = true)
        // ⭐️ 수정: tomorrowLetterDao의 @Update 함수 사용
        tomorrowLetterDao.updateTomorrowLetter(updatedLetter.toTomorrowLetterEntity())
        Log.d(TAG, "✅ Letter ${letter.id} marked as Arrived in Room.")
    }

    // -------------------------------------------------------------
    // ⭐️ AlarmManager 예약 로직
    // -------------------------------------------------------------

    private fun scheduleLetterDelivery(letterId: String, deliveryTimestamp: Long) {
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e(TAG, "❌ 정확한 알람 권한이 없어 설정 화면으로 안내합니다.")

                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    data = Uri.fromParts("package", applicationContext.packageName, null)
                }
                applicationContext.startActivity(intent)
                return
            }
        }

        val intent = Intent(applicationContext, com.example.nambukhwangdan.LetterDeliveryReceiver::class.java).apply {
            putExtra(EXTRA_LETTER_ID, letterId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            letterId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            deliveryTimestamp,
            pendingIntent
        )
        Log.d(TAG, "⏰ Letter $letterId scheduled for ${Date(deliveryTimestamp)}. Delivery Time: ${Date(deliveryTimestamp)}")
    }

    // -------------------------------------------------------------
    // Utility Mapping Functions
    // -------------------------------------------------------------

    private fun TomorrowLetter.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "content" to content,
        "delivery_timestamp" to deliveryTimestamp,
        "created_at" to createdAt,
        "user_id" to userId,
        "is_Arrived" to isArrived,
        "is_Replied" to isReplied
    )

    private fun TomorrowLetterEntity.toTomorrowLetter(): TomorrowLetter = TomorrowLetter(
        id = id,
        content = content,
        deliveryTimestamp = deliveryTimestamp,
        createdAt = createdAt,
        userId = userId,
        isReplied = isReplied,
        isArrived = isArrived
    )

    private fun TomorrowLetter.toTomorrowLetterEntity(): TomorrowLetterEntity = TomorrowLetterEntity(
        id = id,
        content = content,
        deliveryTimestamp = deliveryTimestamp,
        createdAt = createdAt,
        userId = userId,
        isReplied = isReplied,
        isArrived = isArrived
    )

    private fun calculateNextDayNineAM(): Long {
        val zoneId = ZoneId.systemDefault()
        val now = LocalDateTime.now(zoneId)

        val nextDayNineAM = now.toLocalDate()
            .plusDays(1)
            .atTime(LocalTime.of(9, 0, 0))

        return nextDayNineAM.atZone(zoneId)
            .toInstant()
            .toEpochMilli()
    }
}