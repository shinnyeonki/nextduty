package com.shinnk.nextduty.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
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
        
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("display_time", displayTime)
            putExtra("location", location)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(alarmIntent)
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
