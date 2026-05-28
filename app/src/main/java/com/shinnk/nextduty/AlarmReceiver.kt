package com.shinnk.nextduty

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val location = intent.getStringExtra("location") ?: "다음 근무지"
        val startTime = intent.getStringExtra("startTime") ?: ""
        
        showNotification(context, location, startTime)
    }

    private fun showNotification(context: Context, location: String, startTime: String) {
        val channelId = "duty_alarm_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) // 부드러운 소리를 위해 NOTIFICATION 타입 사용

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                channelId,
                "근무 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "근무 시작 5분 전 알림"
                enableVibration(true)
                setSound(alarmSound, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("FROM_ALARM", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, mainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("근무 이동 알림 (5분 전)")
            .setContentText("[$startTime] $location 이동 준비하세요!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(alarmSound)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)

        val notification = notificationBuilder.build()
        // 사용자가 알림을 클릭하거나 지울 때까지 소리가 반복되도록 설정
        notification.flags = notification.flags or Notification.FLAG_INSISTENT

        notificationManager.notify(1001, notification) // 고정 ID를 사용하여 알람이 여러 개 쌓이지 않게 함
    }
}
