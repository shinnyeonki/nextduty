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
        val channelId = "duty_alarm_channel_v2" // 채널 설정을 변경하기 위해 ID 변경
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) // 알람 타입 소리 사용

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                channelId,
                "근무 알람 (긴급)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "근무 시작 전 강력한 알림"
                enableVibration(true)
                setSound(alarmSound, audioAttributes)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 알람 화면으로 이동하는 인텐트
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("location", location)
            putExtra("startTime", startTime)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, alarmIntent,
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
            .setFullScreenIntent(pendingIntent, true) // 잠금 화면에서 바로 띄우기

        val notification = notificationBuilder.build()
        // 사용자가 알림을 확인하거나 취소할 때까지 소리가 반복되도록 설정
        notification.flags = notification.flags or Notification.FLAG_INSISTENT

        notificationManager.notify(1001, notification)
    }
}
