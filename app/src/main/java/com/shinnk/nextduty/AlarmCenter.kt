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
        const val CHANNEL_ID = "duty_alarm_channel_v4"
        const val ALARM_REQUEST_CODE_RANGE = 50 
    }

    /**
     * 알람 예약 로직 단순화: 기존 알람을 모두 취소하고 새로 등록합니다.
     */
    fun scheduleAlarms(time: String, table: Int, number: Int, isPt: Boolean) {
        // 1. 기존에 예약된 모든 알람 취소
        cancelAllAlarms()

        // 2. 정확한 알람 권한 체크 (Android 12 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) return
        }

        // 3. 오늘 기준 남은 근무 알람들 가져오기
        val alarms = DutyCore.getAlarmSchedules(time, table, number, isPt)
        val now = LocalTime.now()
        val today = LocalDate.now()

        // 알림 클릭 시 앱 실행을 위한 인텐트
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE
        )

        alarms.forEachIndexed { index, alarm ->
            if (index >= ALARM_REQUEST_CODE_RANGE) return@forEachIndexed

            // 이미 지난 시간의 알람은 등록하지 않음
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

                // 시스템 알람 시계 아이콘이 표시되는 가장 확실한 알람 방식 사용
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, mainPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            }
        }
    }

    /**
     * 알람 취소 로직: 설정된 범위 내의 모든 RequestCode에 대해 취소 명령을 내립니다.
     */
    fun cancelAllAlarms() {
        for (i in 0 until ALARM_REQUEST_CODE_RANGE) {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, i, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    /**
     * 알림 표시: 채널을 생성하고 중요도 높은 알림을 띄웁니다.
     */
    fun showAlarmNotification(location: String, startTime: String) {
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(CHANNEL_ID, "근무 이동 알람", NotificationManager.IMPORTANCE_HIGH).apply {
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(alarmSound, audioAttributes)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("location", location)
            putExtra("startTime", startTime)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, alarmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("근무 이동 알림")
            .setContentText("[$startTime] $location 이동 준비하세요!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(pendingIntent, true) // 화면 꺼져있을 때 즉시 띄우기
            .setSound(alarmSound)
            .setOngoing(true) // 사용자가 확인할 때까지 유지
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun dismissAlarm() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
