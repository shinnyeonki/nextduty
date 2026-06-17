package com.shinnk.nextduty.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AppSettingsDialog(
    leadTime: Int,
    onLeadTimeChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("알림 설정", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text("알람 미리 알림 설정", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                
                val options = listOf(10, 9, 8, 7, 6, 5, 4)
                options.forEach { minutes ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLeadTimeChange(minutes) }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(selected = leadTime == minutes, onClick = { onLeadTimeChange(minutes) })
                        Text("${minutes}분 전", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("닫기") } }
    )
}

@Composable
fun InfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("정보", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("NEXT DUTY", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Text("버전: v1.0.0", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                Text("개발자 정보", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text("shinnk (shinnk.dev@gmail.com)", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                Text("오픈소스 라이선스", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text("• Kotlin Coroutines, Serialization\n• Jetpack Compose, Material 3\n• DataStore, Lifecycle\n• Coil (Image Loading)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(Modifier.height(16.dp))
                Text("© 2024 NEXT DUTY. All rights reserved.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("확인") } }
    )
}
