package com.shinnk.nextduty

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        containerColor = MaterialTheme.colorScheme.background,
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
            AnimatedVisibility(
                visible = !isAppActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                InactiveOverlay()
            }
        }
    }
}

@Composable
private fun TopActionBar(isAppActive: Boolean, onToggleActive: (Boolean) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (isAppActive) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isAppActive) "서비스 활성 상태" else "서비스 중지 상태",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isAppActive) MaterialTheme.colorScheme.onSurface else Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Switch(
                    modifier = Modifier.scale(0.85f),
                    checked = isAppActive,
                    onCheckedChange = onToggleActive,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun InactiveOverlay() {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = false) { },
        color = Color.Black.copy(alpha = 0.55f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(bottom = 64.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "서비스가 정지되었습니다",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "상단의 스위치를 켜면 다시 알림과 근무 확인이 시작됩니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
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
        Text(
            "오늘의 근무 설정",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold
        )
        
        Spacer(Modifier.height(32.dp))

        SettingsModernSection("근무 시간") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("주간1" to "JU1", "주간2" to "JU2").forEach { (label, value) ->
                    ModernSelectableChip(
                        label = label,
                        selected = selectedTime == value,
                        onClick = { selectedTime = value },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        
        SettingsModernSection("편성표 번호") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..4).forEach { i ->
                    ModernSelectableChip(
                        label = i.toString(),
                        selected = selectedTable == i,
                        onClick = { selectedTable = i },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        
        SettingsModernSection("나의 근무 번호") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..4).forEach { i ->
                    ModernSelectableChip(
                        label = i.toString(),
                        selected = selectedNumber == i,
                        onClick = { selectedNumber = i },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("PT 여부", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("PT 인가요?", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Switch(checked = ptStatus, onCheckedChange = onSavePtStatus)
            }
        }

        Spacer(Modifier.height(32.dp))
        
        Button(
            onClick = { onSave(selectedTime, selectedTable, selectedNumber) },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("저장 및 서비스 시작", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(24.dp))
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
        // Modern Clock
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Black)) {
                    append(currentTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                }
                withStyle(SpanStyle(fontWeight = FontWeight.Light, fontSize = 24.sp, color = Color.Gray)) {
                    append(":" + currentTime.format(DateTimeFormatter.ofPattern("ss")))
                }
            },
            style = MaterialTheme.typography.displayLarge,
            fontSize = 72.sp
        )
        
        Spacer(Modifier.height(24.dp))
        
        ModernDutyCard(
            title = "현재 나의 근무지",
            location = currentLocation,
            range = currentRange,
            isHighlight = true,
            icon = Icons.Default.LocationOn
        )

        Spacer(Modifier.height(16.dp))

        ModernDutyCard(
            title = "다음 예정 근무지",
            location = nextLocation,
            range = nextStartTime,
            isHighlight = false,
            icon = Icons.Default.Info
        )

        Spacer(Modifier.height(24.dp))
        
        // Countdown Section
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("퇴근까지 남은 시간", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Text(
                    text = formatDuration(remaining),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (remaining.isZero) Color.Gray else MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Info Summary Footer
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${if(settings.time == "JU1") "주간1" else "주간2"} | 편성표 ${settings.table}번 | 번호 ${settings.number}번",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if(settings.isPt) "PT 근무 적용됨" else "일반 근무 적용됨",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                IconButton(
                    onClick = onEdit,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

@Composable
private fun ModernDutyCard(
    title: String, 
    location: String, 
    range: String, 
    isHighlight: Boolean,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlight) 6.dp else 0.dp),
        border = if (!isHighlight) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isHighlight) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else Color.Gray
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = location, 
                    style = MaterialTheme.typography.headlineSmall, 
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isHighlight) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (range.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings, // Logic icon for shift
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isHighlight) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f) else Color.Gray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = range, 
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isHighlight) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f) else Color.Gray
                        )
                    }
                }
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun ModernSelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 0.dp else 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        tonalElevation = if (selected) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SettingsModernSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

// Logic remain exactly the same
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
