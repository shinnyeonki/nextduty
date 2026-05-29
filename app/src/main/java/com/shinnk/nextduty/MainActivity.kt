package com.shinnk.nextduty

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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

class MainActivity : ComponentActivity() {

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var alarmCenter: AlarmCenter

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
        alarmCenter = AlarmCenter(this)
        alarmCenter.dismissAlarm()

        enableEdgeToEdge()
        
        preferenceManager = PreferenceManager(this)
        
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
                        preferenceManager.saveDutySettings(time, table, number, ptStatus)
                        if (isAppActive) {
                            alarmCenter.scheduleAlarms(time, table, number, ptStatus)
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
                                alarmCenter.scheduleAlarms(settings.time, settings.table, settings.number, settings.isPt)
                            }
                        } else {
                            alarmCenter.cancelAllAlarms()
                        }
                    }
                },
                onEdit = {
                    alarmCenter.cancelAllAlarms()
                },
            )
        }
    }

    override fun onResume() {
        super.onResume()
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        // 1. 알림 권한 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        // 2. 정확한 알람 권한 (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "정확한 알람 권한이 필요합니다. 설정에서 허용해주세요.", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
                return
            }
        }

        // 3. 전체 화면 알림 권한 (Android 14+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!notificationManager.canUseFullScreenIntent()) {
                Toast.makeText(this, "전체 화면 알림 권한이 필요합니다. '전체 화면 알림 허용'을 켜주세요.", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
                return
            }
        }
    }
}
