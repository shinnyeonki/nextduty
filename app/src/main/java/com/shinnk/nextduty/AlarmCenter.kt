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
        val tableKey = "${time}_$table"
        val timeSlots = DutyCore.totalMap[tableKey] ?: return
        
        cancelAllAlarms()

        val now = LocalTime.now()
        val today = LocalDate.now()

        timeSlots.forEachIndexed { index, slot ->
            val startTime = LocalTime.parse(slot.startTime)
            var alarmTime = startTime.minusMinutes(5)
            
            if (isPt) {
                if (time == "JU2" && slot.startTime == "11:00") {
                    alarmTime = LocalTime.of(11, 25)
                } else if (time == "JU1" && !startTime.isBefore(LocalTime.of(16, 30))) {
                    return@forEachIndexed
                }
            }
            
            if (alarmTime.isAfter(now)) {
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("location", slot.locations.getOrNull(number - 1))
                    putExtra("startTime", if (isPt && time == "JU2" && slot.startTime == "11:00") "11:30" else slot.startTime)
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    index,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val triggerAtMillis = alarmTime.atDate(today).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                    } else {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(CHANNEL_ID, "근무 알람 (긴급)", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "근무 시작 전 강력한 알림"
                enableVibration(true)
                setSound(alarmSound, audioAttributes)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
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
            .setContentTitle("근무 이동 알림 (5분 전)")
            .setContentText("[$startTime] $location 이동 준비하세요!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(alarmSound)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .build()

        notification.flags = notification.flags or Notification.FLAG_INSISTENT
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun dismissAlarm() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
