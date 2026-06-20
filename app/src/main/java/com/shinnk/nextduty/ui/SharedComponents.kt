package com.shinnk.nextduty.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shinnk.nextduty.MainActivity

@Composable
fun AppSettingsDialog(
    leadTime: Int,
    receiveFinishAlarm: Boolean,
    finishLeadTime: Int,
    onLeadTimeChange: (Int) -> Unit,
    onReceiveFinishAlarmChange: (Boolean) -> Unit,
    onFinishLeadTimeChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("알림 설정", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "근무 교대 ${leadTime}분 전 알림",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(Modifier.height(16.dp))
                
                Slider(
                    value = leadTime.toFloat(),
                    onValueChange = { onLeadTimeChange(it.toInt()) },
                    valueRange = 1f..10f,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("1분", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("10분", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                
                Spacer(Modifier.height(24.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("퇴근 알림 받기", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("업무 종료 시 알람을 받습니다.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Switch(
                        checked = receiveFinishAlarm,
                        onCheckedChange = onReceiveFinishAlarmChange
                    )
                }

                if (receiveFinishAlarm) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "퇴근 ${finishLeadTime}분 전 알림",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Slider(
                        value = finishLeadTime.toFloat(),
                        onValueChange = { onFinishLeadTimeChange(it.toInt()) },
                        valueRange = 1f..5f,
                        steps = 3,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = receiveFinishAlarm
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1분", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("5분", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                Spacer(Modifier.height(16.dp))
                
                Text(
                    "설정을 변경하면 다음 알림부터 적용됩니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("설정 완료", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun InfoDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("정보", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("NEXTDUTY", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Text("버전: v1.2", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                Text("개발자 정보", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text("신년기 (sygys10293@gmail.com)", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.PRIVACY_POLICY_URL))
                        context.startActivity(intent)
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("개인정보처리방침 확인", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(16.dp))
                Text("오픈소스 라이선스", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text("• Kotlin Coroutines, Serialization\n• Jetpack Compose, Material 3\n• DataStore, Lifecycle\n• Coil (Image Loading)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(Modifier.height(16.dp))
                Text("© 2026 NEXTDUTY. All rights reserved.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("확인") } }
    )
}
