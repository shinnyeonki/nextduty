package com.shinnk.nextduty

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            scope.launch {
                try {
                    val prefs = PreferenceManager(context)
                    val settings = prefs.dutySettings.first()
                    
                    if (settings != null) {
                        AlarmCenter(context).scheduleAlarms(
                            time = settings.time,
                            table = settings.table,
                            number = settings.number,
                            isPt = settings.isPt
                        )
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
