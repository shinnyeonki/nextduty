package com.shinnk.nextduty

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun StatusScreen(settings: DutySettings, onEdit: () -> Unit) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    val lifecycleOwner = LocalLifecycleOwner.current
    
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                currentTime = LocalTime.now()
                delay(1000L - (System.currentTimeMillis() % 1000L))
            }
        }
    }

    val dutyInfo = remember(currentTime, settings) { DutyCore.calculateDutyInfo(currentTime, settings) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = currentTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                style = TextStyle(
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    shadow = Shadow(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), offset = Offset(0f, 4f), blurRadius = 10f)
                )
            )
            Text(
                text = ":" + currentTime.format(DateTimeFormatter.ofPattern("ss")),
                modifier = Modifier.padding(bottom = 10.dp, start = 4.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }
        
        Spacer(Modifier.height(32.dp))
        PremiumDutyCard("현재 위치", dutyInfo.currentLoc, dutyInfo.currentRange, true, Icons.Default.LocationOn)
        Spacer(Modifier.height(20.dp))
        PremiumDutyCard("다음 이동지", dutyInfo.nextLoc, dutyInfo.nextStart, false, Icons.Default.Info)

        Spacer(Modifier.height(24.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "업무 종료까지", 
                    style = MaterialTheme.typography.bodyMedium, 
                    fontWeight = FontWeight.Bold, 
                    color = Color.Gray
                )
                Text(
                    text = DutyCore.formatDuration(dutyInfo.remaining), 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Black, 
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            shape = RoundedCornerShape(24.dp),
            onClick = onEdit
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    val table = DutyCore.getTable(settings.tableName)
                    Text(text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(table?.displayName ?: settings.tableName)
                        }
                        append(" • ${settings.number}번 근무")
                    }, style = MaterialTheme.typography.bodyMedium)
                    Text(text = if(settings.isPt) "PT 근무 모드 활성" else "일반 근무 모드", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }
    }
}
