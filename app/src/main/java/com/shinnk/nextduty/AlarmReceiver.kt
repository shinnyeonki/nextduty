package com.shinnk.nextduty

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val location = intent.getStringExtra("location") ?: "다음 근무지"
        val startTime = intent.getStringExtra("startTime") ?: ""
        
        // 1. 알림 표시 (Full Screen Intent 포함 - 화면 꺼짐 대응)
        // 화면이 꺼져있거나 잠겨있을 때는 fullScreenIntent를 통해 OS가 안전하게 화면을 깨우고 AlarmActivity를 실행합니다.
        AlarmCenter(context).showAlarmNotification(location, startTime)

        // 2. 액티비티 즉시 실행 (폰 사용 중일 때 대응)
        // 폰을 적극적으로 사용 중인 상황(화면이 켜져있고 잠금 해제 상태)에서만 액티비티를 직접 실행합니다.
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val isScreenOn = powerManager.isInteractive
        val isLocked = keyguardManager.isKeyguardLocked

        if (isScreenOn && !isLocked) {
            val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("location", location)
                putExtra("startTime", startTime)
            }
            
            try {
                context.startActivity(alarmIntent)
            } catch (e: Exception) {
                // 권한이 없는 경우 등 예외 발생 시 로그만 출력
                // 이 경우에도 1번의 시스템 알림(팝업)은 작동합니다.
                e.printStackTrace()
            }
        }
    }
}

