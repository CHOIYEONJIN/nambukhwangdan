package com.example.nambukhwangdan // ⭐️ 실제 프로젝트 패키지 경로로 수정

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.nambukhwangdan.data.repository.TomorrowLetterRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

// ⭐️ Hilt를 통해 Repository를 주입받기 위해 @AndroidEntryPoint 사용
@AndroidEntryPoint
class LetterDeliveryReceiver : BroadcastReceiver() {

    // ⭐️ 주입: Repository를 통해 DB 조회 및 업데이트를 수행합니다.
    @Inject
    lateinit var repository: TomorrowLetterRepository

    // ⭐️ 주입: 알림 채널 관리 및 알림 발송을 위한 NotificationManagerCompat
    @Inject
    lateinit var notificationManager: NotificationManagerCompat

    companion object {
        const val EXTRA_LETTER_ID = "EXTRA_LETTER_ID"
        private const val TAG = "DeliveryReceiver"
        const val CHANNEL_ID = "tomorrow_letter_channel" // 알림 채널 ID
        const val NOTIFICATION_ID_BASE = 1000 // 알림 ID 베이스
    }

    override fun onReceive(context: Context, intent: Intent) {
        // BroadcastReceiver는 메인 스레드에서 실행되므로,
        // 오래 걸리는 DB/네트워크 작업은 코루틴으로 처리해야 합니다.
        CoroutineScope(Dispatchers.IO).launch {
            val letterId = intent.getStringExtra(EXTRA_LETTER_ID)
            Log.d(TAG, "Received alarm for Letter ID: $letterId")

            if (letterId != null) {
                processAndNotify(context, letterId)
            }
        }
    }

    // -------------------------------------------------------------
    // ⭐️ 핵심 로직: 편지 처리 및 알림 발송
    // -------------------------------------------------------------
    private suspend fun processAndNotify(context: Context, letterId: String) {
        // 1. 편지 조회
        val letter = repository.getTomorrowLetterById(letterId)
        if (letter == null) {
            Log.w("DeliveryReceiver", "삭제된 편지 → 알람 처리 중단")
            return
        }
        if (letter != null) {
            // 2. 편지 상태 업데이트 (전달됨으로 표시)
            // (ViewModel이 처리하는 것이 일반적이나, 여기서는 Receiver에서 직접 호출)
            repository.markLetterAsDelivered(letter)

            // 3. 시스템 알림 생성 및 발송
            showNotification(context, letterId, letter.content)

            Log.i(TAG, "Letter $letterId successfully processed and notified.")
        } else {
            Log.e(TAG, "❌ Letter ID $letterId not found in DB.")
        }
    }

    // -------------------------------------------------------------
    // ⭐️ 시스템 알림 생성 로직
    // -------------------------------------------------------------
    private fun showNotification(context: Context, letterId: String, content: String) {
        // 🔴 Android 13+ 알림 권한 체크
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val hasPermission =
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                Log.w(TAG, "POST_NOTIFICATIONS 권한 없음 → 알림 스킵")
                return
            }
        }

        // 1. 알림 내용 구성
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email) // 앱 아이콘으로 대체 필요
            .setContentTitle("내일의 나에게서 편지가 도착했어요! 📬")
            // 편지 내용 일부를 표시
            .setContentText(content.substring(0, minOf(content.length, 100)) + "...")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // 탭하면 알림 제거
            // 큰 텍스트 스타일을 사용하여 편지 내용을 더 보여줍니다.
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .build()

        // 2. 알림 발송 (각 편지마다 고유한 알림 ID를 사용)
        // 같은 편지 ID의 해시코드를 사용하면 같은 알림을 덮어씁니다.
        notificationManager.notify(
            NOTIFICATION_ID_BASE + letterId.hashCode(), // 고유한 알림 ID
            notification
        )
    }
}