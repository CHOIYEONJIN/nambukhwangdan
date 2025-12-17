package com.example.nambukhwangdan

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.nambukhwangdan.data.repository.LetterRepository
import com.example.nambukhwangdan.data.repository.TomorrowLetterRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LetterDeliveryReceiver : BroadcastReceiver() {

    @Inject
    lateinit var tomorrowRepository: TomorrowLetterRepository

    @Inject
    lateinit var letterRepository: LetterRepository

    @Inject
    lateinit var notificationManager: NotificationManagerCompat

    companion object {
        const val EXTRA_LETTER_ID = "EXTRA_LETTER_ID"
        private const val TAG = "DeliveryReceiver"
        const val CHANNEL_ID = "tomorrow_letter_channel"
        const val NOTIFICATION_ID_BASE = 1000
    }

    override fun onReceive(context: Context, intent: Intent) {
        val letterId = intent.getStringExtra(EXTRA_LETTER_ID)
        Log.d(TAG, "⏰ 알람 수신 - Letter ID: $letterId")

        if (letterId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                processAndNotify(context, letterId)
            }
        }
    }

    private suspend fun processAndNotify(context: Context, letterId: String) {
        var finalContent: String? = null
        var finalTitle = ""

        // 1. 우선 '내일의 편지'에서 조회
        val tomorrowLetter = tomorrowRepository.getTomorrowLetterById(letterId)

        if (tomorrowLetter != null) {
            Log.d(TAG, "✅ 내일의 편지 매칭됨")
            finalContent = tomorrowLetter.content
            finalTitle = "어제의 나에게서 편지가 도착했어요! 📬"
            tomorrowRepository.markLetterAsArrived(tomorrowLetter)
        } else {
            // 2. 없으면 '일반 편지'에서 조회
            val generalLetter = letterRepository.getLetterById(letterId)
            if (generalLetter != null) {
                Log.d(TAG, "✅ 일반 편지 매칭됨")
                finalContent = generalLetter.content
                finalTitle = "편지가 도착했습니다! ✉️"
            }
        }

        // 3. 데이터를 찾았다면 알림 발송
        if (finalContent != null) {
            showNotification(context, letterId, finalContent, finalTitle)
            Log.i(TAG, "🎉 알림 프로세스 성공: $letterId")
        } else {
            Log.e(TAG, "❌ DB에서 ID를 찾을 수 없음: $letterId")
        }
    }

    private fun showNotification(context: Context, letterId: String, content: String, title: String) {
        // [필수] Android 8.0 이상을 위한 알림 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "편지 도착 알림",
                NotificationManager.IMPORTANCE_HIGH // 중요도 HIGH여야 팝업이 뜸
            ).apply {
                description = "편지가 도착했을 때 알려드립니다."
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        // Android 13+ 알림 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                Log.w(TAG, "❌ 알림 권한이 없어 알림을 띄우지 못했습니다.")
                return
            }
        }

        // 알림 생성
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(title) // 내일의 편지/일반 편지 제목 분기 적용
            .setContentText(if (content.length > 30) content.take(30) + "..." else content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .build()

        notificationManager.notify(
            NOTIFICATION_ID_BASE + letterId.hashCode(),
            notification
        )
    }
}