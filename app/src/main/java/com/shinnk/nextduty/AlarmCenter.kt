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

    fun scheduleAlarms(tableName: String, number: Int, isPt: Boolean) {
        cancelAllAlarms()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) return
        }

        val alarms = DutyCore.getAlarmSchedules(tableName, number, isPt)
        val now = LocalTime.now()
        val today = LocalDate.now()

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE
        )

        alarms.forEachIndexed { index, alarm ->
            if (index >= ALARM_REQUEST_CODE_RANGE) return@forEachIndexed

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

                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, mainPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            }
        }
    }

    fun cancelAllAlarms() {
        for (i in 0 until ALARM_REQUEST_CODE_RANGE) {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, i, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        dismissAlarm()
    }

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
            .setFullScreenIntent(pendingIntent, true)
            .setSound(alarmSound)
            .setOngoing(true)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun dismissAlarm() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
