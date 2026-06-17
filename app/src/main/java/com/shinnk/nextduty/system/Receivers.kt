package com.shinnk.nextduty.system

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.shinnk.nextduty.AlarmActivity
import com.shinnk.nextduty.R
import com.shinnk.nextduty.data.DutyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val displayTime = intent.getStringExtra("display_time") ?: ""
        val location = intent.getStringExtra("location") ?: ""
        val id = intent.getIntExtra("id", 0)
        
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("display_time", displayTime)
            putExtra("location", location)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 
            id, 
            alarmIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, "duty_alarm_channel")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("근무 교대 알람")
            .setContentText("$displayTime - $location")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
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
                    val allTables = repository.allTables.firstOrNull() ?: emptyList()
                    
                    if (settings != null) {
                        val table = allTables.find { it.displayName == settings.tableName }
                        if (table != null) {
                            alarmProvider.scheduleAlarms(table, settings.number, settings.isPt, leadTime)
                        }
                    }
                }
            }
        }
    }
}
