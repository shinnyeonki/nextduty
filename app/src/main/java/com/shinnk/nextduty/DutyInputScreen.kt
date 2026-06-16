package com.shinnk.nextduty

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun InputScreen(
    initialSettings: DutySettings?,
    ptStatus: Boolean,
    onSave: (String, Int) -> Unit,
    onSavePtStatus: (Boolean) -> Unit
) {
    val allTables = DutyCore.getAllTables()
    val initialTableName = initialSettings?.tableName ?: "주1-1"
    var selectedTable by remember { mutableStateOf(DutyCore.getTable(initialTableName) ?: allTables.firstOrNull() ?: DutyTable("기본", 3, emptyList())) }
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
                        if (rowItems.size < 4) {
                            repeat(4 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                        }
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
                        if (rowItems.size < 4) {
                            repeat(4 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        PtStatusCard(ptStatus, onSavePtStatus)
        
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
private fun PtStatusCard(ptStatus: Boolean, onSavePtStatus: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(), 
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), 
        shape = RoundedCornerShape(24.dp), 
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("PT 근무 적용", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("PT 시간으로 보정합니다.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Switch(checked = ptStatus, onCheckedChange = onSavePtStatus, colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary))
        }
    }
}
