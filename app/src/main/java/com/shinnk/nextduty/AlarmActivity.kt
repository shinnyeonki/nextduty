package com.shinnk.nextduty

import android.os.*
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shinnk.nextduty.system.AlarmProvider
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

class AlarmActivity : ComponentActivity() {
    private lateinit var alarmProvider: AlarmProvider
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alarmProvider = AlarmProvider(this)

        // 화면 깨우기 및 잠금 화면 위에 표시 설정
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)

        val displayTime = intent.getStringExtra("display_time") ?: "00:00"
        val location = intent.getStringExtra("location") ?: "근무지"
        val isFinish = intent.getBooleanExtra("is_finish", false)

        startVibration()
        alarmProvider.startAlarmSound()

        setContent {
            BackHandler {
                // 뒤로 가기 버튼으로 알람 끄기 방지
            }
            AlarmScreen(displayTime, location, isFinish) {
                stopAlarm()
                finish()
            }
        }
    }

    private fun startVibration() {
        val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibrator = vibratorManager.defaultVibrator

        val pattern = longArrayOf(0, 800, 400, 800, 400)
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
    }

    private fun stopAlarm() {
        alarmProvider.stopAlarmSound()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }
}

@Composable
fun AlarmScreen(time: String, location: String, isFinish: Boolean, onDismiss: () -> Unit) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm:ss") }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(1000)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121212) // 중후한 다크 배경
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 상단: 현재 시간 표시 (업무 중 시간 확인용)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "현재 시각",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = currentTime.format(timeFormatter),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )
            }

            // 중앙: 알람 내용 (가독성 최대화)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isFinish) "[ 업무 종료 ]" else "[ 근무 교대 ]",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isFinish) Color(0xFF81C784) else Color(0xFF64B5F6),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = time,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 110.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 110.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = location,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 52.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 60.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            // 하단: 버튼 (실수 방지를 위해 크게 배치)
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "알람 끄기",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black
                    )
                )
            }
        }
    }
}
