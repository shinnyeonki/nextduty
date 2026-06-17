package com.shinnk.nextduty.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shinnk.nextduty.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DutyPlanEditorDialog(
    initialTables: List<DutyTable>,
    onSave: (List<DutyTable>?) -> Unit,
    onDismiss: () -> Unit
) {
    var baseTables by remember { mutableStateOf(initialTables) }
    var editedTables by remember { mutableStateOf(baseTables) }
    var selectedTableName by remember { mutableStateOf(editedTables.firstOrNull()?.displayName ?: "") }

    LaunchedEffect(editedTables.size) {
        if (editedTables.isEmpty()) selectedTableName = ""
        else if (selectedTableName.isEmpty() || editedTables.none { it.displayName == selectedTableName }) {
            selectedTableName = editedTables.first().displayName
        }
    }

    var safeTabCount by remember { mutableIntStateOf(editedTables.size) }
    LaunchedEffect(editedTables.size) { safeTabCount = editedTables.size }

    var editingSlotIndex by remember { mutableIntStateOf(-1) }
    
    var showResetConfirm by remember { mutableStateOf(false) }
    var showSaveConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showAddTableDialog by remember { mutableStateOf(false) }
    var showExitConfirm by remember { mutableStateOf(false) }

    val handleBack = {
        if (editedTables != baseTables) showExitConfirm = true else onDismiss()
    }

    BackHandler(onBack = handleBack)

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text("변경 사항 폐기", fontWeight = FontWeight.Bold) },
            text = { Text("저장하지 않은 변경 사항이 있습니다. 정말로 나가시겠습니까?") },
            confirmButton = { TextButton(onClick = { showExitConfirm = false; onDismiss() }) { Text("나가기", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showExitConfirm = false }) { Text("취소") } }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false }, 
            title = { Text("편성표 초기화", fontWeight = FontWeight.Bold) }, 
            text = { Text("모든 수정 사항을 삭제하고 기본 설정으로 되돌릴까요?") }, 
            confirmButton = { 
                TextButton(onClick = { 
                    onSave(null)
                    // Note: Here we need to know the default tables, but for now we'll rely on the parent to update initialTables if they want.
                    // Or we could pass the default tables as well. Let's assume onSave(null) handles it.
                    showResetConfirm = false 
                    onDismiss() // Resetting is a big enough change to go back
                }) { Text("초기화", color = MaterialTheme.colorScheme.error) } 
            }, 
            dismissButton = { TextButton(onClick = { showResetConfirm = false }) { Text("취소") } }
        )
    }

    if (showSaveConfirm) {
        AlertDialog(
            onDismissRequest = { showSaveConfirm = false },
            title = { Text("변경 사항 저장", fontWeight = FontWeight.Bold) },
            text = { Text("수정된 편성표 데이터를 저장하시겠습니까?") },
            confirmButton = { 
                Button(onClick = { onSave(editedTables); baseTables = editedTables; showSaveConfirm = false }) { Text("저장") } 
            },
            dismissButton = { TextButton(onClick = { showSaveConfirm = false }) { Text("취소") } }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("편성표 삭제", fontWeight = FontWeight.Bold) },
            text = { Text("현재 선택된 '$selectedTableName' 편성표를 삭제하시겠습니까?") },
            confirmButton = { TextButton(onClick = {
                val newTables = editedTables.filter { it.displayName != selectedTableName }
                editedTables = newTables
                selectedTableName = newTables.firstOrNull()?.displayName ?: ""
                showDeleteConfirm = false
            }) { Text("삭제", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("취소") } }
        )
    }

    if (showAddTableDialog) {
        var newName by remember { mutableStateOf("") }
        var newCapacity by remember { mutableIntStateOf(3) }
        AlertDialog(
            onDismissRequest = { showAddTableDialog = false },
            title = { Text("새 편성표 추가", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("편성표 이름") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    Text("인원수: $newCapacity", style = MaterialTheme.typography.bodyMedium)
                    Slider(value = newCapacity.toFloat(), onValueChange = { newCapacity = it.toInt() }, valueRange = 1f..4f, steps = 2)
                }
            },
            confirmButton = { 
                Button(
                    enabled = newName.isNotBlank() && editedTables.none { it.displayName == newName },
                    onClick = {
                        val newTable = DutyTable(newName, newCapacity, listOf(DutySlot("09:00", "10:00", List(newCapacity) { LocationType.Off })))
                        editedTables = editedTables + newTable
                        selectedTableName = newName
                        showAddTableDialog = false
                    }
                ) { Text("추가") }
            },
            dismissButton = { TextButton(onClick = { showAddTableDialog = false }) { Text("취소") } }
        )
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = { 
                TopAppBar(
                    title = { 
                        Column {
                            Text("편성표 데이터 편집", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            if (selectedTableName.isNotEmpty()) Text(selectedTableName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }, 
                    navigationIcon = { IconButton(onClick = handleBack) { Icon(Icons.Default.Close, null) } },
                    actions = { 
                        var showMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.Settings, "설정") }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(text = { Text("새 편성표 추가") }, onClick = { showMenu = false; showAddTableDialog = true }, leadingIcon = { Icon(Icons.Default.Add, null) })
                            if (selectedTableName.isNotEmpty()) {
                                DropdownMenuItem(text = { Text("현재 편성표 삭제", color = MaterialTheme.colorScheme.error) }, onClick = { showMenu = false; showDeleteConfirm = true }, leadingIcon = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) })
                            }
                            HorizontalDivider()
                            DropdownMenuItem(text = { Text("전체 데이터 초기화", color = MaterialTheme.colorScheme.error) }, onClick = { showMenu = false; showResetConfirm = true })
                        }
                        Button(onClick = { showSaveConfirm = true }, modifier = Modifier.padding(end = 8.dp)) { Text("저장") } 
                    }
                ) 
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (editedTables.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("편성표가 없습니다. 추가해주세요.", color = Color.Gray) }
                } else {
                    val currentTableIndex = editedTables.indexOfFirst { it.displayName == selectedTableName }
                    if (currentTableIndex != -1) {
                        val currentTable = editedTables[currentTableIndex]
                        ScrollableTabRow(selectedTabIndex = currentTableIndex.coerceIn(0, maxOf(0, safeTabCount - 1)), edgePadding = 16.dp, containerColor = MaterialTheme.colorScheme.surface, divider = {}) {
                            editedTables.forEach { table -> 
                                Tab(selected = selectedTableName == table.displayName, onClick = { selectedTableName = table.displayName }, text = { Text(table.displayName, style = MaterialTheme.typography.labelLarge) }) 
                            }
                        }
                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            itemsIndexed(currentTable.slots) { index, slot -> SlotEditCard(slot = slot, onClick = { editingSlotIndex = index }) }
                            item {
                                OutlinedButton(onClick = {
                                    val newSlot = DutySlot("10:00", "11:00", List(currentTable.capacity) { LocationType.Off })
                                    val newTables = editedTables.toMutableList()
                                    newTables[currentTableIndex] = currentTable.copy(slots = currentTable.slots + newSlot)
                                    editedTables = newTables
                                }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                                    Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("슬롯 추가")
                                }
                            }
                        }
                    }
                }
            }
        }
        if (editingSlotIndex != -1) {
            val currentTable = editedTables.find { it.displayName == selectedTableName }
            if (currentTable != null && editingSlotIndex < currentTable.slots.size) {
                SlotDetailEditDialog(
                    slot = currentTable.slots[editingSlotIndex], capacity = currentTable.capacity, onDismiss = { editingSlotIndex = -1 },
                    onConfirm = { updatedSlot ->
                        editedTables = editedTables.map { if (it.displayName == selectedTableName) {
                            val newSlots = it.slots.toMutableList(); newSlots[editingSlotIndex] = updatedSlot; it.copy(slots = newSlots)
                        } else it }
                        editingSlotIndex = -1
                    },
                    onDelete = {
                        editedTables = editedTables.map { if (it.displayName == selectedTableName) {
                            val newSlots = it.slots.toMutableList(); if (editingSlotIndex in newSlots.indices) newSlots.removeAt(editingSlotIndex); it.copy(slots = newSlots)
                        } else it }
                        editingSlotIndex = -1
                    }
                )
            } else editingSlotIndex = -1
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
private fun SlotDetailEditDialog(slot: DutySlot, capacity: Int, onDismiss: () -> Unit, onConfirm: (DutySlot) -> Unit, onDelete: () -> Unit) {
    var startTime by remember { mutableStateOf(slot.startTime) }; var endTime by remember { mutableStateOf(slot.endTime) }
    var locations by remember { mutableStateOf(if (slot.locations.size >= capacity) slot.locations.take(capacity) else slot.locations + List(capacity - slot.locations.size) { LocationType.Off }) }

    AlertDialog(onDismissRequest = onDismiss, title = {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("근무지 수정", fontWeight = FontWeight.Bold)
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "삭제", tint = MaterialTheme.colorScheme.error) }
        }
    }, text = {
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
