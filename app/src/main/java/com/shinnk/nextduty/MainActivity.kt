package com.shinnk.nextduty

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class MainActivity : ComponentActivity() {

    private lateinit var preferenceManager: PreferenceManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "알림 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 화면 깨우기 및 잠금 화면 위에 표시 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        // 앱이 켜지면 알람 소리 중지
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1001)

        enableEdgeToEdge()
        
        preferenceManager = PreferenceManager(this)
        
        checkNotificationPermission()

        setContent {
            val dutySettings by preferenceManager.dutySettings.collectAsState(initial = null)
            val ptStatus by preferenceManager.ptStatus.collectAsState(initial = false)
            val isAppActive by preferenceManager.isAppActive.collectAsState(initial = true)

            DutyApp(
                dutySettings = dutySettings,
                ptStatus = ptStatus,
                isAppActive = isAppActive,
                onSaveSettings = { time, table, number ->
                    lifecycleScope.launch {
                        // Capture the current ptStatus when saving settings
                        preferenceManager.saveDutySettings(time, table, number, ptStatus)
                        if (isAppActive) {
                            scheduleAlarms(time, table, number, ptStatus)
                        }
                    }
                },
                onSavePtStatus = { status ->
                    lifecycleScope.launch {
                        preferenceManager.savePtStatus(status)
                    }
                },
                onSaveAppActiveStatus = { isActive ->
                    lifecycleScope.launch {
                        preferenceManager.saveAppActiveStatus(isActive)
                        if (isActive) {
                            dutySettings?.let { settings ->
                                scheduleAlarms(settings.time, settings.table, settings.number, settings.isPt)
                            }
                        } else {
                            cancelAllAlarms()
                        }
                    }
                },
                onEdit = {
                    cancelAllAlarms()
                },
            )
        }
    }

    private fun checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun scheduleAlarms(time: String, table: Int, number: Int, isPt: Boolean) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val tableKey = "${time}_$table"
        val timeSlots = DutyMasterData.totalMap[tableKey] ?: return
        
        cancelAllAlarms() // Clear existing ones first

        val now = LocalTime.now()
        val today = LocalDate.now()

        timeSlots.forEachIndexed { index, slot ->
            val startTime = LocalTime.parse(slot.startTime)
            
            // Apply PT Logic to Alarm Scheduling
            var alarmTime = startTime.minusMinutes(5)
            
            if (isPt) {
                if (time == "JU2" && slot.startTime == "11:00") {
                    // JU2 PT starts at 11:30, so alarm at 11:25
                    alarmTime = LocalTime.of(11, 25)
                } else if (time == "JU1" && !startTime.isBefore(LocalTime.of(16, 30))) {
                    // JU1 PT ends at 16:30, no new shift alarms at or after 16:30
                    return@forEachIndexed
                }
            }
            
            if (alarmTime.isAfter(now)) {
                val intent = Intent(this, AlarmReceiver::class.java).apply {
                    putExtra("location", slot.locations.getOrNull(number - 1))
                    putExtra("startTime", if (isPt && time == "JU2" && slot.startTime == "11:00") "11:30" else slot.startTime)
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    index,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val zonedDateTime = alarmTime.atDate(today).atZone(ZoneId.systemDefault())
                val triggerAtMillis = zonedDateTime.toInstant().toEpochMilli()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            }
        }
    }

    private fun cancelAllAlarms() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        for (i in 0 until 20) {
            val intent = Intent(this, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                i,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}
