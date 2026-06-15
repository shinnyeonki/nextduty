package com.shinnk.nextduty

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.LocalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val pendingResult = goAsync()
            scope.launch {
                try {
                    val prefs = PreferenceManager(context)
                    val settings = prefs.dutySettings.first()
                    val isAppActive = prefs.isAppActive.first()
                    
                    if (settings != null && isAppActive) {
                        val alarmCenter = AlarmCenter(context)
                        
                        // 알람 재등록
                        alarmCenter.scheduleAlarms(
                            tableName = settings.tableName,
                            number = settings.number,
                            isPt = settings.isPt
                        )

                        // 방금(10분 이내) 지난 알람이 있다면 즉시 알림
                        val now = LocalTime.now()
                        val missedAlarms = DutyCore.getAlarmSchedules(
                            settings.tableName, settings.number, settings.isPt
                        ).filter { 
                            it.triggerTime.isBefore(now) && it.triggerTime.isAfter(now.minusMinutes(10))
                        }

                        missedAlarms.lastOrNull()?.let { lastMissed ->
                            alarmCenter.showAlarmNotification(lastMissed.location, lastMissed.displayStartTime)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
