package com.shinnk.nextduty.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shinnk.nextduty.data.*
import java.time.LocalTime

@Composable
fun HomeFeature(
    dutySettings: DutySettings?,
    allTables: List<DutyTable>,
    shiftPattern: ShiftPattern,
    isEditing: Boolean,
    onSaveSettings: (String, Int) -> Unit,
    onSaveShiftPattern: (ShiftPattern) -> Unit,
    onEdit: () -> Unit
) {
    if (isEditing || dutySettings == null) {
        InputScreen(
            initialSettings = dutySettings,
            allTables = allTables,
            shiftPattern = shiftPattern,
            onSave = onSaveSettings,
            onSaveShiftPattern = onSaveShiftPattern
        )
    } else {
        val currentTable = allTables.find { it.displayName == dutySettings.tableName } ?: allTables.first()
        StatusScreen(
            settings = dutySettings,
            table = currentTable,
            onEdit = onEdit
        )
    }
}

@Composable
private fun StatusScreen(settings: DutySettings, table: DutyTable, onEdit: () -> Unit) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while(true) {
            currentTime = LocalTime.now()
            kotlinx.coroutines.delay(1000)
        }
    }

    val info = DutyCalculator.calculateDutyInfo(currentTime, table, settings)

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(settings.tableName, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                Text("${settings.number}번 근무자", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            }
            FilledIconButton(onClick = onEdit, shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.Edit, null) }
        }

        Spacer(Modifier.height(32.dp))

        // 1. 현재 근무 카드 (크게)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("현재 위치", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(info.currentLoc, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
                Text(info.currentRange, style = MaterialTheme.typography.bodyLarge)
            }
        }

        Spacer(Modifier.height(16.dp))

        // 2. 다음 근무 카드 (크게)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("다음 근무", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                Text(info.nextLoc, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Text(info.nextStart, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(16.dp))

        // 3. 남은 시간 (박스 없이 표시)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "남은 시간", 
                style = MaterialTheme.typography.labelLarge, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                DutyCalculator.formatDuration(info.remaining),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = (-0.5).sp
            )
        }
        
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun InputScreen(
    initialSettings: DutySettings?,
    allTables: List<DutyTable>,
    shiftPattern: ShiftPattern,
    onSave: (String, Int) -> Unit,
    onSaveShiftPattern: (ShiftPattern) -> Unit
) {
    val initialTableName = initialSettings?.tableName ?: "주1-1"
    var selectedTable by remember { mutableStateOf(allTables.find { it.displayName == initialTableName } ?: allTables.first()) }
    var selectedNumber by remember { mutableIntStateOf(initialSettings?.number ?: 1) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTable) { if (selectedNumber > selectedTable.capacity) selectedNumber = 1 }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("설정 확인", fontWeight = FontWeight.Bold) },
            text = { Text("'${selectedTable.displayName}' 편성표의 ${selectedNumber}번 근무로 설정하시겠습니까?") },
            confirmButton = { Button(onClick = { onSave(selectedTable.displayName, selectedNumber); showConfirmDialog = false }) { Text("확인") } },
            dismissButton = { TextButton(onClick = { showConfirmDialog = false }) { Text("취소") } }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(12.dp))
        Text("근무 설정", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Text("편성표와 본인의 번호를 선택하세요.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(Modifier.height(32.dp))

        PremiumInputSection("편성표 선택", Icons.AutoMirrored.Filled.List) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                allTables.chunked(4).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowItems.forEach { table ->
                            PremiumSelectableChip(
                                label = table.displayName,
                                selected = selectedTable.displayName == table.displayName,
                                onClick = { selectedTable = table },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowItems.size < 4) repeat(4 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        PremiumInputSection("나의 근무 번호", Icons.Default.Person) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                (1..selectedTable.capacity).toList().chunked(4).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowItems.forEach { i ->
                            PremiumSelectableChip(
                                label = i.toString(), 
                                selected = selectedNumber == i, 
                                onClick = { selectedNumber = i }, 
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowItems.size < 4) repeat(4 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        ShiftPatternSection(shiftPattern, onSaveShiftPattern)
        
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = { showConfirmDialog = true },
            modifier = Modifier.fillMaxWidth().height(64.dp).shadow(8.dp, RoundedCornerShape(20.dp), spotColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(20.dp)
        ) { Text("설정 완료", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun PremiumInputSection(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable () -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun PremiumSelectableChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (selected) FontWeight.Black else FontWeight.Medium, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ShiftPatternSection(currentPattern: ShiftPattern, onSaveShiftPattern: (ShiftPattern) -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("출퇴근 패턴 선택", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShiftPattern.entries.forEach { pattern ->
                val selected = currentPattern == pattern
                Surface(
                    onClick = { onSaveShiftPattern(pattern) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            pattern.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
