package com.shinnk.nextduty

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class AlarmCenter(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "duty_alarm_channel_v3"
    }

    fun scheduleAlarms(time: String, table: Int, number: Int, isPt: Boolean) {
        val alarms = DutyCore.getAlarmSchedules(time, table, number, isPt)
        
        cancelAllAlarms()

        val now = LocalTime.now()
        val today = LocalDate.now()

        // 알람 상태바 아이콘을 클릭했을 때 열릴 화면 설정
        val showAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            showAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        alarms.forEachIndexed { index, alarm ->
            if (alarm.triggerTime.isAfter(now)) {
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("location", alarm.location)
                    putExtra("startTime", alarm.displayStartTime)
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    index,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val triggerAtMillis = alarm.triggerTime
                    .atDate(today)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                // setAlarmClock을 사용하여 시스템 알람 수준의 우선순위 부여
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, showAppPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            }
        }
    }

    fun cancelAllAlarms() {
        for (i in 0 until 20) {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, i, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    fun showAlarmNotification(location: String, startTime: String) {
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(CHANNEL_ID, "근무 알람 (긴급)", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "근무 시작 전 강력한 알람"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(alarmSound, audioAttributes)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setBypassDnd(true) // 방해 금지 모드 무시
            }
            notificationManager.createNotificationChannel(channel)
        }

        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_USER_ACTION
            putExtra("location", location)
            putExtra("startTime", startTime)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, alarmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("근무 이동 알림 (5분 전)")
            .setContentText("[$startTime] $location 이동 준비하세요!")
            .setPriority(NotificationCompat.PRIORITY_MAX) // 최대 우선순위
            .setCategory(NotificationCompat.CATEGORY_ALARM) // 알람 카테고리 설정
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false) // 사용자가 확인하기 전까지 유지
            .setOngoing(true) // 스와이프로 삭제 불가
            .setSound(alarmSound)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true) // 화면이 꺼져있을 때 즉시 화면을 띄움
            .build()

        notification.flags = notification.flags or Notification.FLAG_INSISTENT
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun dismissAlarm() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
