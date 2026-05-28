package com.shinnk.nextduty

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
    isAppActive: Boolean,
    onSaveSettings: (String, Int, Int) -> Unit,
    onSavePtStatus: (Boolean) -> Unit,
    onSaveAppActiveStatus: (Boolean) -> Unit,
    onEdit: () -> Unit,
) {
    var isEditing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopActionBar(
                isAppActive = isAppActive,
                onToggleActive = onSaveAppActiveStatus
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Main Content
            if (dutySettings == null || isEditing) {
                InputScreen(
                    initialSettings = dutySettings,
                    ptStatus = ptStatus,
                    onSave = { time, table, number ->
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

            // Inactive Overlay
            if (!isAppActive) {
                InactiveOverlay()
            }
        }
    }
}

@Composable
private fun TopActionBar(isAppActive: Boolean, onToggleActive: (Boolean) -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isAppActive) "서비스 활성화됨" else "서비스 일시 정지",
                style = MaterialTheme.typography.titleSmall,
                color = if (isAppActive) MaterialTheme.colorScheme.primary else Color.Gray,
                fontWeight = FontWeight.Bold
            )
            Switch(
                modifier = Modifier.scale(0.8f),
                checked = isAppActive,
                onCheckedChange = onToggleActive
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
    }
}

@Composable
private fun InactiveOverlay() {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = false) { },
        color = Color.Black.copy(alpha = 0.4f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    "서비스가 정지되었습니다",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun InputScreen(
    initialSettings: DutySettings?,
    ptStatus: Boolean,
    onSave: (String, Int, Int) -> Unit,
    onSavePtStatus: (Boolean) -> Unit
) {
    var selectedTime by remember { mutableStateOf(initialSettings?.time ?: "JU1") }
    var selectedTable by remember { mutableIntStateOf(initialSettings?.table ?: 1) }
    var selectedNumber by remember { mutableIntStateOf(initialSettings?.number ?: 1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Text("근무 설정", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(32.dp))

        SettingsSection("근무 시간") {
            Row {
                listOf("주1" to "JU1", "주2" to "JU2").forEach { (label, value) ->
                    DutyRadioButton(label, selectedTime == value) { selectedTime = value }
                    Spacer(Modifier.width(16.dp))
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        SettingsSection("편성표 번호") {
            Row {
                (1..4).forEach { i ->
                    DutyRadioButton(i.toString(), selectedTable == i) { selectedTable = i }
                    if (i < 4) Spacer(Modifier.width(8.dp))
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        SettingsSection("나의 근무 번호") {
            Row {
                (1..4).forEach { i ->
                    DutyRadioButton(i.toString(), selectedNumber == i) { selectedNumber = i }
                    if (i < 4) Spacer(Modifier.width(8.dp))
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(Modifier.padding(16.dp).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("PT 인가요?", fontWeight = FontWeight.Medium)
                Switch(checked = ptStatus, onCheckedChange = onSavePtStatus)
            }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { onSave(selectedTime, selectedTable, selectedNumber) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("저장 및 근무 시작", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun StatusScreen(settings: DutySettings, onEdit: () -> Unit) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(1000)
        }
    }

    val (currentLocation, currentRange, nextLocation, nextStartTime, remaining) = remember(currentTime, settings) {
        calculateDutyInfo(currentTime, settings)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(40.dp))
        
        DutyCard(
            title = "현재 근무지",
            location = currentLocation,
            range = currentRange,
            isHighlight = true
        )

        Spacer(Modifier.height(16.dp))

        DutyCard(
            title = "다음 근무지",
            location = nextLocation,
            range = nextStartTime,
            isHighlight = false
        )

        Spacer(Modifier.height(40.dp))
        
        Text("퇴근까지 남은 시간", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(
            text = formatDuration(remaining),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Medium,
            color = if (remaining.isZero) Color.Gray else MaterialTheme.colorScheme.error
        )

        Spacer(Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "현재 설정: ${if(settings.time == "JU1") "주1" else "주2"} | 편성표 ${settings.table} | 번호 ${settings.number} | PT ${if(settings.isPt) "O" else "X"}",
                style = MaterialTheme.typography.bodyMedium, color = Color.Gray
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onEdit, Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Text("옵션 수정")
            }
        }
    }
}

@Composable
private fun DutyCard(title: String, location: String, range: String, isHighlight: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isHighlight) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(if (isHighlight) 24.dp else 20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlight) 4.dp else 0.dp)
    ) {
        Column(Modifier.padding(if (isHighlight) 24.dp else 20.dp)) {
            Text(title, style = if (isHighlight) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(8.dp))
            Text(location, style = if (isHighlight) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (range.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(range, style = if (isHighlight) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall, color = Color.Gray)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
fun DutyRadioButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, Modifier.padding(start = 4.dp))
    }
}

private data class DutyInfo(
    val currentLoc: String,
    val currentRange: String,
    val nextLoc: String,
    val nextStart: String,
    val remaining: Duration
)

private fun calculateDutyInfo(currentTime: LocalTime, settings: DutySettings): DutyInfo {
    val tableKey = "${settings.time}_${settings.table}"
    val timeSlots = DutyMasterData.totalMap[tableKey] ?: emptyList()
    
    val isPt = settings.isPt
    val shiftStart = if (isPt && settings.time == "JU2") LocalTime.of(11, 30) else (if (settings.time == "JU1") LocalTime.of(8, 0) else LocalTime.of(11, 0))
    val shiftEnd = if (isPt && settings.time == "JU1") LocalTime.of(16, 30) else (if (settings.time == "JU1") LocalTime.of(17, 0) else LocalTime.of(20, 0))

    val (currLoc, currRange) = when {
        currentTime.isBefore(shiftStart) -> "출근 전" to "시작 시간: $shiftStart"
        !currentTime.isBefore(shiftEnd) -> "업무 종료" to "퇴근 완료"
        else -> {
            val slot = timeSlots.find { s ->
                val start = LocalTime.parse(s.startTime)
                val end = LocalTime.parse(s.endTime)
                !currentTime.isBefore(start) && currentTime.isBefore(end)
            }
            (slot?.locations?.getOrNull(settings.number - 1) ?: "근무 외 시간") to (slot?.let { "${it.startTime} ~ ${it.endTime}" } ?: "")
        }
    }

    val nextSlot = timeSlots.find { currentTime.isBefore(LocalTime.parse(it.startTime)) }
    val (nLoc, nStart) = if (nextSlot != null && LocalTime.parse(nextSlot.startTime).isBefore(shiftEnd)) {
        (nextSlot.locations.getOrNull(settings.number - 1) ?: "없음") to "시작 예정: ${nextSlot.startTime}"
    } else {
        "없음 (퇴근 예정)" to "수고하셨습니다"
    }

    val remaining = if (currentTime.isBefore(shiftEnd)) Duration.between(currentTime, shiftEnd) else Duration.ZERO
    
    return DutyInfo(currLoc, currRange, nLoc, nStart, remaining)
}

private fun formatDuration(duration: Duration): String = String.format(
    Locale.getDefault(), "%02d:%02d:%02d",
    duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()
)
