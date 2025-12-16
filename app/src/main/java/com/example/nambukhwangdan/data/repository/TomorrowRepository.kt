package com.example.nambukhwangdan.data.repository

import android.util.Log
import com.example.nambukhwangdan.data.local.DiaryDao
import com.example.nambukhwangdan.model.TomorrowLetter.TomorrowLetter
import com.example.nambukhwangdan.model.TomorrowLetter.TomorrowLetterEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
// ⭐️ AlarmManager 및 Context 관련 Import
import android.content.Context
import android.content.Intent
import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext // Hilt Context 주입을 위해 필요
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date // Log 출력을 위해 추가
import android.provider.Settings // Settings import 추가
import android.net.Uri // Uri import 추가
/**
 * 미래 편지(TomorrowLetter) 데이터에 대한 접근 지점.
 * Room(로컬)과 Firestore(원격) 간의 데이터 관리를 담당합니다.
 */
@Singleton
class TomorrowLetterRepository @Inject constructor(
    private val diaryDao: DiaryDao,
    private val firestore: FirebaseFirestore,
    // ⭐️ Application Context 주입: AlarmManager 사용을 위해 필수
    @ApplicationContext private val applicationContext: Context
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val TAG = "TL_REPO"

    // ⭐️ LetterDeliveryReceiver에서 편지 ID를 가져올 때 사용할 상수
    companion object {
        const val EXTRA_LETTER_ID = "EXTRA_LETTER_ID"
        // 리시버 클래스 이름은 여기에 직접 사용할 수 없으므로, 문자열 키만 사용합니다.
    }

    // -------------------------------------------------------------
    // TomorrowLetter - Remote (Firestore) Operations
    // -------------------------------------------------------------

    /** Firestore 사용자 미래 편지 컬렉션 경로를 반환합니다. */
    private fun tomorrowLetterCollection(userId: String) =
        firestore.collection("users")
            .document(userId)
            .collection("tomorrow_letters") // 컬렉션 경로

    /**
     * Firestore에 미래 편지 데이터를 저장/업데이트합니다.
     * ⭐️ Map으로 변환하여 Diary와 동일한 방식으로 저장하며, 로그를 출력합니다.
     */
    suspend fun saveTomorrowLetterToFirestore(letter: TomorrowLetter, userId: String? = auth.currentUser?.uid): Boolean = try {
        val uid = userId ?: run {
            Log.e(TAG, "❌ Save Failed: User ID is NULL (Not logged in)")
            return false
        }

        Log.d(TAG, "✅ Saving Letter ${letter.id} for UID: $uid")

        // Firestore 저장 로직 실행
        tomorrowLetterCollection(uid)
            .document(letter.id)
            .set(letter.toFirestoreMap())
            .await()

        Log.i(TAG, "🎉 Letter ${letter.id} Saved Successfully.")
        true
    } catch (e: Exception) {
        // 🚨 통신 실패 또는 권한 거부 시 에러 로그 출력
        Log.e(TAG, "🚨 Firestore Save Error for ${letter.id}: ${e.message}", e)
        false
    }

    // -------------------------------------------------------------
    // TomorrowLetter Operations (Room & Firestore)
    // -------------------------------------------------------------

    /** ID를 기반으로 TomorrowLetter를 한 번 조회합니다. */
    suspend fun getTomorrowLetterById(id: String): TomorrowLetter? {
        return diaryDao.getTomorrowLetterByIdDao(id)?.toTomorrowLetter()
    }

    /**
     * 아직 전달되지 않은 미래 편지 목록을 실시간으로 가져옵니다.
     */
    fun getAllUnDeliveredLetters(): Flow<List<TomorrowLetter>> =
        diaryDao.getAllUnDeliveredLettersDao().map { entities ->
            entities.map { it.toTomorrowLetter() }
        }
    fun getAllTomorrowLetters(): Flow<List<TomorrowLetter>> =
        diaryDao.getAllTomorrowLettersDao().map { entities ->
            entities.map { it.toTomorrowLetter() }
        }

    /**
     * 새로운 미래 편지를 Room에 저장하고, Firestore에 저장하며, 알람을 예약합니다.
     * 🚨 [수정] DB 저장 전에 반드시 계산된 deliveryTimestamp를 letter 객체에 반영합니다.
     */
    suspend fun insertTomorrowLetter(letter: TomorrowLetter) {
        // ⭐️ 1. 다음날 오전 9시 타임스탬프 계산
        val correctDeliveryTimestamp = calculateNextDayNineAM()

        // ⭐️ 2. Letter 객체에 올바른 deliveryTimestamp를 반영하여 새로운 객체 생성
        val letterToSave = letter.copy(deliveryTimestamp = correctDeliveryTimestamp)

        // 3. Room에 저장 (로컬) - 올바른 타임스탬프 사용
        diaryDao.insertTomorrowLetterDao(letterToSave.toTomorrowLetterEntity())
        Log.d(TAG, "Local (Room) save successful for ${letterToSave.id}")

        // 4. Firestore에 저장 (원격 저장 시도) - 올바른 타임스탬프 사용
        auth.currentUser?.uid?.let { uid ->
            saveTomorrowLetterToFirestore(letterToSave, uid)
        } ?: Log.w(TAG, "Attempted Firestore save without valid UID in insertTomorrowLetter.")

        // 5. 알람 예약 추가 (계산된 시간 사용)
        scheduleLetterDelivery(letterToSave.id, correctDeliveryTimestamp)
    }

    /**
     * 현재 시간보다 deliveryTimestamp가 빠르거나 같고 isDelivered가 false인 편지 목록을 한 번 가져옵니다.
     */
    suspend fun getDueLettersOnce(currentTime: Long): List<TomorrowLetter> =
        diaryDao.getDueLettersOnceDao(currentTime).map { it.toTomorrowLetter() }

    /**
     * 편지가 Diary로 변환되어 전달되었음을 표시하기 위해 상태를 업데이트하고 Firestore에도 반영합니다.
     */
    suspend fun markTMLetterAsReplied(letter: TomorrowLetter) {
        val updatedLetter = letter.copy(isReplied = true)
        // Room 업데이트
        diaryDao.updateTomorrowLetterDao(updatedLetter.toTomorrowLetterEntity())

        // Firestore 상태 업데이트
        auth.currentUser?.uid?.let { uid ->
            saveTomorrowLetterToFirestore(updatedLetter, uid)
        }
    }

    // -------------------------------------------------------------
    // ⭐️ AlarmManager 예약 로직
    // -------------------------------------------------------------

    /**
     * AlarmManager를 사용하여 편지 도착 시간에 맞춰 알람을 예약합니다.
     */
    private fun scheduleLetterDelivery(letterId: String, deliveryTimestamp: Long) {
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 💡 1. [핵심 수정 부분]: API 31 (Android 12) 이상에서 정확한 알람 권한 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e(TAG, "❌ 정확한 알람 권한이 없어 설정 화면으로 안내합니다.")

                // 🚨 [여기가 누락된 핵심 로직입니다]
                // 사용자에게 해당 권한을 허용하도록 설정 화면을 띄워줍니다.
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    // 이 플래그를 추가해야 Context(Repository)에서 Activity를 시작할 수 있습니다.
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    // 현재 앱 설정으로 포커스를 맞추기 위해 패키지 이름을 명시합니다.
                    data = Uri.fromParts("package", applicationContext.packageName, null)
                }
                applicationContext.startActivity(intent)

                // 권한 요청 후 함수 종료
                return
            }
        }

        val intent = Intent(applicationContext, com.example.nambukhwangdan.LetterDeliveryReceiver::class.java).apply {
            putExtra(EXTRA_LETTER_ID, letterId)
        }

        // 2. PendingIntent 고유 코드 생성 및 플래그 설정
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            letterId.hashCode(), // 요청 코드로 고유 ID 사용
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. 알람 예약 (정확한 시간 보장) - 권한이 확인된 후에만 안전하게 호출됩니다.
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, // 디바이스가 잠자기 상태에서도 깨워서 실행
            deliveryTimestamp,
            pendingIntent
        )
        Log.d(TAG, "⏰ Letter $letterId scheduled for ${Date(deliveryTimestamp)}. Delivery Time: ${Date(deliveryTimestamp)}")
    }
    /**
     * TomorrowLetter Model을 Firestore 직렬화를 위한 Map으로 변환합니다.
     */
    private fun TomorrowLetter.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "content" to content,
        "delivery_timestamp" to deliveryTimestamp,
        "created_at" to createdAt,
        "user_id" to userId,
        "is_Arrived" to isArrived,
        "is_Replied" to isReplied
    )

    /**
     * TomorrowLetterEntity (Room) -> TomorrowLetter (Model)로 변환합니다.
     */
    private fun TomorrowLetterEntity.toTomorrowLetter(): TomorrowLetter = TomorrowLetter(
        id = id,
        content = content,
        deliveryTimestamp = deliveryTimestamp,
        createdAt = createdAt,
        userId = userId,
        isReplied = isReplied,
        isArrived = isArrived
    )

    /**
     * TomorrowLetter (Model) -> TomorrowLetterEntity (Room)로 변환합니다.
     */
    private fun TomorrowLetter.toTomorrowLetterEntity(): TomorrowLetterEntity = TomorrowLetterEntity(
        id = id,
        content = content,
        deliveryTimestamp = deliveryTimestamp,
        createdAt = createdAt,
        userId = userId,
        isReplied = isReplied,
        isArrived = isArrived
    )

    /**
     * 현재 시간을 기준으로 다음 날 오전 9시의 Epoch Milliseconds (Long)를 계산합니다.
     */
    private fun calculateNextDayNineAM(): Long {
        val zoneId = ZoneId.systemDefault()
        val now = LocalDateTime.now(zoneId)

        // 다음 날 09:00:00 시점을 계산합니다.
        val nextDayNineAM = now.toLocalDate()
            .plusDays(1) // 현재 날짜에서 하루를 더합니다.
            .atTime(LocalTime.of(9, 0, 0)) // 시간을 09:00:00으로 설정합니다.

        // LocalDateTime을 ZoneId 기준으로 Instant로 변환하고, 밀리초(Long)로 반환합니다.
        return nextDayNineAM.atZone(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    suspend fun markLetterAsArrived(letter: TomorrowLetter) {
        // letter.id를 사용하여 DAO 호출
        diaryDao.markLetterAsArrived(letter.id)
    }
}