package com.shinnk.nextduty.system

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.shinnk.nextduty.AlarmActivity
import com.shinnk.nextduty.data.DutyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val displayTime = intent.getStringExtra("display_time") ?: ""
        val location = intent.getStringExtra("location") ?: ""
        val isFinish = intent.getBooleanExtra("is_finish", false)
        val id = intent.getIntExtra("id", 0)
        
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("display_time", displayTime)
            putExtra("location", location)
            putExtra("is_finish", isFinish)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 
            id, 
            alarmIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 화면이 켜져 있을 때도 전체 화면을 강제하기 위해 직접 Activity 시작 시도
        // overlay 권한이 있는 경우 백그라운드에서도 Activity 시작이 가능함
        if (Settings.canDrawOverlays(context)) {
            try {
                context.startActivity(alarmIntent)
            } catch (_: Exception) {
                // 실패 시 Full Screen Intent가 처리하도록 함
            }
        }

        val notificationBuilder = NotificationCompat.Builder(context, "duty_alarm_channel")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("근무 교대 알람")
            .setContentText("$displayTime - $location")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, notificationBuilder.build())
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val repository = DutyRepository(context)
            val alarmProvider = AlarmProvider(context)
            
            CoroutineScope(Dispatchers.IO).launch {
                val isActive = repository.isAppActive.firstOrNull() ?: true
                if (isActive) {
                    val settings = repository.dutySettings.firstOrNull()
                    val leadTime = repository.alarmLeadTime.firstOrNull() ?: 5
                    val receiveFinishAlarm = repository.receiveFinishAlarm.firstOrNull() ?: true
                    val finishLeadTime = repository.finishAlarmLeadTime.firstOrNull() ?: 2
                    val allTables = repository.allTables.firstOrNull() ?: emptyList()
                    
                    if (settings != null) {
                        val table = allTables.find { it.displayName == settings.tableName }
                        if (table != null) {
                            alarmProvider.scheduleAlarms(table, settings.number, settings.isPt, leadTime, receiveFinishAlarm, finishLeadTime)
                        }
                    }
                }
            }
        }
    }
}
