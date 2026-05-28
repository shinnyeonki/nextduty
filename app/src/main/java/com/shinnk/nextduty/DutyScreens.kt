package com.shinnk.nextduty

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.Duration
import java.util.Locale

@Composable
fun DutyApp(
    dutySettings: DutySettings?,
    ptStatus: Boolean,
    onSaveSettings: (String, Int, Int) -> Unit,
    onSavePtStatus: (Boolean) -> Unit,
    onEdit: () -> Unit,
) {
    var isEditing by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (dutySettings == null || isEditing) {
            InputScreen(
                initialTime = dutySettings?.time ?: "JU1",
                initialTable = dutySettings?.table ?: 1,
                initialNumber = dutySettings?.number ?: 1,
                ptStatus = ptStatus,
                onSaveSettings = { time, table, number ->
                    onSaveSettings(time, table, number)
                    isEditing = false
                },
                onSavePtStatus = onSavePtStatus
            )
        } else {
            StatusScreen(dutySettings, onEdit = {
                isEditing = true
                onEdit()
            })
        }
    }
}

@Composable
fun InputScreen(
    initialTime: String = "JU1",
    initialTable: Int = 1,
    initialNumber: Int = 1,
    ptStatus: Boolean,
    onSaveSettings: (String, Int, Int) -> Unit,
    onSavePtStatus: (Boolean) -> Unit
) {
    var selectedTime by remember { mutableStateOf(initialTime) }
    var selectedTable by remember { mutableIntStateOf(initialTable) }
    var selectedNumber by remember { mutableIntStateOf(initialNumber) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            "근무 설정",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))

        SettingsSection("근무 시간") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DutyRadioButton(
                    label = "주1",
                    selected = selectedTime == "JU1",
                    onClick = { selectedTime = "JU1" }
                )
                Spacer(modifier = Modifier.width(16.dp))
                DutyRadioButton(
                    label = "주2",
                    selected = selectedTime == "JU2",
                    onClick = { selectedTime = "JU2" }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection("편성표 번호") {
            Row {
                (1..4).forEach { i ->
                    DutyRadioButton(
                        label = i.toString(),
                        selected = selectedTable == i,
                        onClick = { selectedTable = i }
                    )
                    if (i < 4) Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection("나의 근무 번호") {
            Row {
                (1..4).forEach { i ->
                    DutyRadioButton(
                        label = i.toString(),
                        selected = selectedNumber == i,
                        onClick = { selectedNumber = i }
                    )
                    if (i < 4) Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("PT 인가요?", fontWeight = FontWeight.Medium)
                Switch(checked = ptStatus, onCheckedChange = onSavePtStatus)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onSaveSettings(selectedTime, selectedTable, selectedNumber) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("저장 및 근무 시작", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
fun DutyRadioButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, modifier = Modifier.padding(start = 4.dp))
    }
}

@Composable
fun StatusScreen(
    settings: DutySettings,
    onEdit: () -> Unit
) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(1000)
        }
    }

    val tableKey = "${settings.time}_${settings.table}"
    val timeSlots = DutyMasterData.totalMap[tableKey] ?: emptyList()
    
    // PT Time adjustments
    val isPt = settings.isPt
    val actualShiftStart = if (isPt && settings.time == "JU2") LocalTime.of(11, 30) else {
        if (settings.time == "JU1") LocalTime.of(8, 0) else LocalTime.of(11, 0)
    }
    val actualShiftEnd = if (isPt && settings.time == "JU1") LocalTime.of(16, 30) else {
        if (settings.time == "JU1") LocalTime.of(17, 0) else LocalTime.of(20, 0)
    }

    // Determine Current Location
    val currentLocation: String
    val currentRange: String
    
    when {
        currentTime.isBefore(actualShiftStart) -> {
            currentLocation = "출근 전"
            currentRange = "시작 시간: $actualShiftStart"
        }
        !currentTime.isBefore(actualShiftEnd) -> {
            currentLocation = "업무 종료"
            currentRange = "퇴근 완료"
        }
        else -> {
            val slot = timeSlots.find { s ->
                val start = LocalTime.parse(s.startTime)
                val end = LocalTime.parse(s.endTime)
                !currentTime.isBefore(start) && currentTime.isBefore(end)
            }
            currentLocation = slot?.locations?.getOrNull(settings.number - 1) ?: "근무 외 시간"
            currentRange = slot?.let { "${it.startTime} ~ ${it.endTime}" } ?: ""
        }
    }

    // Determine Next Location
    val nextLocation: String
    val nextStartTime: String
    
    val nextSlot = timeSlots.find { s ->
        val start = LocalTime.parse(s.startTime)
        currentTime.isBefore(start)
    }
    
    if (nextSlot != null && LocalTime.parse(nextSlot.startTime).isBefore(actualShiftEnd)) {
        nextLocation = nextSlot.locations.getOrNull(settings.number - 1) ?: "없음"
        nextStartTime = "시작 예정: ${nextSlot.startTime}"
    } else {
        nextLocation = "없음 (퇴근 예정)"
        nextStartTime = "수고하셨습니다"
    }

    val remainingDuration = if (currentTime.isBefore(actualShiftEnd)) {
        Duration.between(currentTime, actualShiftEnd)
    } else {
        Duration.ZERO
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(40.dp))

        // Current Duty Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("현재 근무지", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentLocation,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                if (currentRange.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        currentRange,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Next Duty Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("다음 근무지", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = nextLocation,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(nextStartTime, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
        
        Text("퇴근까지 남은 시간", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(
            text = String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d",
                remainingDuration.toHours(),
                remainingDuration.toMinutesPart(),
                remainingDuration.toSecondsPart()
            ),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Medium,
            color = if (remainingDuration.isZero) Color.Gray else MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.weight(1f))

        // Settings Summary and Edit Button
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "현재 설정: ${if(settings.time == "JU1") "주1" else "주2"} | 편성표 ${settings.table} | 나의 근무 번호 ${settings.number} | PT ${if(isPt) "O" else "X"}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("옵션 수정")
            }
        }
    }
}
