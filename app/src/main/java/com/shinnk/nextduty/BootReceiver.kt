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
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            scope.launch {
                try {
                    val prefs = PreferenceManager(context)
                    val settings = prefs.dutySettings.first()
                    
                    if (settings != null) {
                        val alarmCenter = AlarmCenter(context)
                        
                        // 1. 전체 알람 스케줄 재등록
                        alarmCenter.scheduleAlarms(
                            time = settings.time,
                            table = settings.table,
                            number = settings.number,
                            isPt = settings.isPt
                        )

                        // 2. 리팩토링: '방금 지난 알람' 복구 동작
                        // 부팅 중(최근 10분 이내)에 울렸어야 할 알람이 있는지 확인
                        val now = LocalTime.now()
                        val missedAlarms = DutyCore.getAlarmSchedules(
                            settings.time, settings.table, settings.number, settings.isPt
                        ).filter { 
                            it.triggerTime.isBefore(now) && it.triggerTime.isAfter(now.minusMinutes(10))
                        }

                        // 놓친 알람이 있다면 가장 최근 것 하나를 즉시 실행
                        missedAlarms.lastOrNull()?.let { lastMissed ->
                            val receiverIntent = Intent(context, AlarmReceiver::class.java).apply {
                                putExtra("location", lastMissed.location)
                                putExtra("startTime", lastMissed.displayStartTime)
                            }
                            context.sendBroadcast(receiverIntent)
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
