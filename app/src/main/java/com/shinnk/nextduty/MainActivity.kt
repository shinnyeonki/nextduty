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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import android.app.NotificationChannel
import android.os.PowerManager
import com.shinnk.nextduty.data.DutyRepository
import com.shinnk.nextduty.system.AlarmProvider
import com.shinnk.nextduty.ui.MainApp
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        const val PRIVACY_POLICY_URL = "https://shinnk.notion.site/NEXTDUTY-38265b7eac9480e485ccfa15c7045e73"
    }

    private lateinit var repository: DutyRepository
    private lateinit var alarmProvider: AlarmProvider

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "필수 권한이 거부되었습니다. 설정에서 허용해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        alarmProvider = AlarmProvider(this)
        alarmProvider.stopAlarmSound()

        enableEdgeToEdge()
        repository = DutyRepository(this)
        
        setContent {
            val dutySettings by repository.dutySettings.collectAsState(initial = null)
            val isPt by repository.isPt.collectAsState(initial = false)
            val isAppActive by repository.isAppActive.collectAsState(initial = true)
            val workScheduleImages by repository.workScheduleImages.collectAsState(initial = emptyList())
            val dutyTableImages by repository.dutyTableImages.collectAsState(initial = emptyList())
            val allTables by repository.allTables.collectAsState(initial = repository.getDefaultTables())
            val alarmLeadTime by repository.alarmLeadTime.collectAsState(initial = 5)

            MainApp(
                dutySettings = dutySettings,
                allTables = allTables,
                isPt = isPt,
                isAppActive = isAppActive,
                workScheduleImages = workScheduleImages,
                dutyTableImages = dutyTableImages,
                alarmLeadTime = alarmLeadTime,
                onSaveSettings = { tableName, number, pt ->
                    lifecycleScope.launch {
                        repository.saveDutySettings(tableName, number, pt)
                        if (isAppActive) {
                            val table = allTables.find { it.displayName == tableName }
                            if (table != null) alarmProvider.scheduleAlarms(table, number, pt, alarmLeadTime)
                        }
                    }
                },
                onSaveAppActiveStatus = { isActive ->
                    lifecycleScope.launch {
                        repository.saveAppActiveStatus(isActive)
                        if (isActive) {
                            dutySettings?.let { settings ->
                                val table = allTables.find { it.displayName == settings.tableName }
                                if (table != null) alarmProvider.scheduleAlarms(table, settings.number, settings.isPt, alarmLeadTime)
                            }
                        } else {
                            alarmProvider.cancelAllAlarms()
                        }
                    }
                },
                onSaveWorkScheduleImages = { images -> lifecycleScope.launch { repository.saveWorkScheduleImages(images) } },
                onSaveDutyTableImages = { images -> lifecycleScope.launch { repository.saveDutyTableImages(images) } },
                onEdit = { lifecycleScope.launch { alarmProvider.cancelAllAlarms() } },
                onSaveCustomDutyTables = { tables ->
                    lifecycleScope.launch {
                        repository.saveCustomDutyTables(tables)
                        dutySettings?.let { settings ->
                            if (isAppActive) {
                                val currentTables = repository.allTables.firstOrNull() ?: emptyList()
                                val table = currentTables.find { it.displayName == settings.tableName }
                                if (table != null) alarmProvider.scheduleAlarms(table, settings.number, settings.isPt, alarmLeadTime)
                            }
                        }
                    }
                },
                onSaveAlarmLeadTime = { minutes ->
                    lifecycleScope.launch {
                        repository.saveAlarmLeadTime(minutes)
                        dutySettings?.let { settings ->
                            if (isAppActive) {
                                val table = allTables.find { it.displayName == settings.tableName }
                                if (table != null) alarmProvider.scheduleAlarms(table, settings.number, settings.isPt, minutes)
                            }
                        }
                    }
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        checkAndRequestPermissions()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "근무 교대 알람"
            val descriptionText = "근무 교대 시간을 알려주는 알림입니다."
            val importance = NotificationManager.IMPORTANCE_MAX
            val channel = NotificationChannel("duty_alarm_channel", name, importance).apply {
                description = descriptionText
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply { data = Uri.fromParts("package", packageName, null) }
                startActivity(intent)
                return
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!nm.canUseFullScreenIntent()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply { data = Uri.fromParts("package", packageName, null) }
                startActivity(intent)
                return
            }
        }
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply { data = Uri.fromParts("package", packageName, null) }
            startActivity(intent)
            return
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
            try { startActivity(intent) } catch (_: Exception) { startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)) }
        }
    }
}
