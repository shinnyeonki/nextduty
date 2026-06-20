package com.shinnk.nextduty.system

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import com.shinnk.nextduty.data.DutyCalculator
import com.shinnk.nextduty.data.DutyTable
import java.time.LocalDateTime
import java.time.ZoneId

class AlarmProvider(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        private var ringtone: Ringtone? = null
    }

    fun scheduleAlarms(table: DutyTable, number: Int, isPt: Boolean, leadTime: Int, receiveFinishAlarm: Boolean, finishLeadTime: Int) {
        cancelAllAlarms()
        val schedules = DutyCalculator.getAlarmSchedules(table, number, isPt, leadTime, receiveFinishAlarm, finishLeadTime)
        val now = LocalDateTime.now()

        schedules.forEachIndexed { index, alarm ->
            val triggerDateTime = LocalDateTime.of(now.toLocalDate(), alarm.triggerTime)
            if (triggerDateTime.isAfter(now)) {
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("display_time", alarm.displayStartTime)
                    putExtra("location", alarm.location)
                    putExtra("is_finish", alarm.isFinish)
                    putExtra("id", index)
                }
                val pendingIntent = PendingIntent.getBroadcast(context, index, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000, pendingIntent)
            }
        }
    }

    fun cancelAllAlarms() {
        for (i in 0..20) {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }

    fun startAlarmSound() {
        try {
            if (ringtone?.isPlaying == true) return
            
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            
            ringtone = RingtoneManager.getRingtone(context, alarmUri)?.apply {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    isLooping = true
                }
                play()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAlarmSound() {
        ringtone?.stop()
        ringtone = null
        
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.cancel()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1001)
    }
}
