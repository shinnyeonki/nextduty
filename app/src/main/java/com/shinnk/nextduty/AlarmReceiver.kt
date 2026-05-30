package com.shinnk.nextduty

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val location = intent.getStringExtra("location") ?: "다음 근무지"
        val startTime = intent.getStringExtra("startTime") ?: ""
        
        // 1. 알림 표시 (Full Screen Intent 포함 - 화면 꺼짐 대응)
        AlarmCenter(context).showAlarmNotification(location, startTime)

        // 2. 액티비티 즉시 실행 (다른 앱 위에 표시 권한 활용 - 화면 켜짐 대응)
        // 안드로이드 10 이상에서는 이 권한이 있어야 백그라운드에서 액티비티를 직접 띄울 수 있습니다.
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
