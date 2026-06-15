package com.shinnk.nextduty

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DutyPlanEditorDialog(onSave: (List<DutyTable>?) -> Unit, onDismiss: () -> Unit) {
    val initialTables = remember { DutyCore.getAllTables() }
    var editedTables by remember { mutableStateOf(initialTables) }
    var selectedTableName by remember { mutableStateOf(editedTables.firstOrNull()?.displayName ?: "") }
    var editingSlotIndex by remember { mutableIntStateOf(-1) }
    var showResetConfirm by remember { mutableStateOf(false) }

    if (showResetConfirm) {
        AlertDialog(onDismissRequest = { showResetConfirm = false }, title = { Text("편성표 초기화", fontWeight = FontWeight.Bold) }, text = { Text("모든 수정 사항을 삭제하고 기본 설정으로 되돌릴까요?") }, confirmButton = { TextButton(onClick = { onSave(null); onDismiss() }) { Text("초기화", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { showResetConfirm = false }) { Text("취소") } })
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("편성표 데이터 편집", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) } }, actions = { TextButton(onClick = { showResetConfirm = true }) { Text("초기화", color = MaterialTheme.colorScheme.error) }; Button(onClick = { onSave(editedTables); onDismiss() }, modifier = Modifier.padding(end = 8.dp)) { Text("저장") } }) }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                ScrollableTabRow(selectedTabIndex = editedTables.indexOfFirst { it.displayName == selectedTableName }.coerceAtLeast(0), edgePadding = 16.dp, containerColor = MaterialTheme.colorScheme.surface, divider = {}) {
                    editedTables.forEach { table -> Tab(selected = selectedTableName == table.displayName, onClick = { selectedTableName = table.displayName }, text = { Text(table.displayName, style = MaterialTheme.typography.labelLarge) }) }
                }
                val currentTable = editedTables.find { it.displayName == selectedTableName }
                if (currentTable != null) {
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(currentTable.slots) { index, slot -> SlotEditCard(slot = slot, onClick = { editingSlotIndex = index }) }
                    }
                }
            }
        }
        if (editingSlotIndex != -1) {
            val currentTable = editedTables.find { it.displayName == selectedTableName }!!
            SlotDetailEditDialog(slot = currentTable.slots[editingSlotIndex], capacity = currentTable.capacity, onDismiss = { editingSlotIndex = -1 }, onConfirm = { updatedSlot ->
                val newTables = editedTables.map { if (it.displayName == selectedTableName) { val newSlots = it.slots.toMutableList(); newSlots[editingSlotIndex] = updatedSlot; it.copy(slots = newSlots) } else it }
                editedTables = newTables; editingSlotIndex = -1
            })
        }
    }
}

@Composable
private fun SlotEditCard(slot: DutySlot, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${slot.startTime} ~ ${slot.endTime}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(slot.locations.joinToString(" | ") { it.getDisplayName() }, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SlotDetailEditDialog(slot: DutySlot, capacity: Int, onDismiss: () -> Unit, onConfirm: (DutySlot) -> Unit) {
    var startTime by remember { mutableStateOf(slot.startTime) }; var endTime by remember { mutableStateOf(slot.endTime) }
    var locations by remember { mutableStateOf(if (slot.locations.size >= capacity) slot.locations.take(capacity) else slot.locations + List(capacity - slot.locations.size) { LocationType.Off }) }

    AlertDialog(onDismissRequest = { onDismiss() }, title = { Text("근무지 수정", fontWeight = FontWeight.Bold) }, text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(value = startTime, onValueChange = { startTime = it }, modifier = Modifier.weight(1f), label = { Text("시작") }); Text(" ~ "); TextField(value = endTime, onValueChange = { endTime = it }, modifier = Modifier.weight(1f), label = { Text("종료") })
            }
            Spacer(Modifier.height(20.dp)); locations.forEachIndexed { index, loc ->
                val textValue = if (loc is LocationType.Active) loc.name else if (loc is LocationType.Lunch) "점심시간" else "근무없음"
                TextField(value = textValue, onValueChange = { newValue -> val newList = locations.toMutableList(); newList[index] = when (newValue) { "근무없음" -> LocationType.Off; "점심시간" -> LocationType.Lunch; else -> LocationType.Active(newValue) }; locations = newList }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), label = { Text("${index + 1}번 근무자") })
            }
        }
    }, confirmButton = { Button(onClick = { onConfirm(DutySlot(startTime, endTime, locations)) }) { Text("확인") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } })
}
