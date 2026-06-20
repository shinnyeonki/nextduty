package com.shinnk.nextduty.ui

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.withTimeoutOrNull

val PATROL_POINTS = listOf(
    "본인 사물함", "1층 사무존 남자 화장실", "2층 진로설계관內 여자 화장실", "숙련관 동측 1층 비상계단 앞",
    "숙련관 1층 남자 화장실", "숙련관 서측 3층 비상계단 앞", "숙련관 3층 남자 화장실", "숙련관 동측 3층 비상계단 앞",
    "숙련관 2층 남자 화장실", "숙련관 동측 2층 비상계단 앞", "숙련관 2층 복도 끝 남자 화장실", "숙련관 1층 직원 출입문 입구(안쪽)",
    "2층 식당 좌 옆 여자 화장실", "2층 안내데스크 좌측 여자 화장실", "2층 유아놀이방 內 좌측 여자 화장실", "지하1층 나래울극장안쪽 분장대기실(좌/문틀)",
    "옥외1층 기사 대기실 옆 출입구", "옥외1층 정산소 주 출입구", "옥외2층 주차장 EV앞", "옥외1층 실내 주차장 입구(D6)",
    "5층 공조실-1 앞 소화전 좌/옆", "4층 여자 화장실", "3층 청체험관 內 여자 화장실", "3층 청체험관입구 좌/옆 여자 화장실",
    "3층 조이샵 좌/옆 여자 화장실", "3층 어체험관 內 여자 화장실", "1층 나래울극장 옆 여자 화장실", "1층 한울강당 옆 여자 화장실", "1층 창의실 옆 여자 화장실"
)

@Composable
fun PatrolDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("patrol_prefs", Context.MODE_PRIVATE) }
    
    var patrolPoints by remember {
        mutableStateOf(prefs.getString("custom_patrol_points", null)?.split("||")?.filter { it.isNotEmpty() } ?: PATROL_POINTS)
    }

    var checkedIndices by remember { 
        mutableStateOf(prefs.getString("checked_list", "")?.split(",")?.filter { it.isNotEmpty() }?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet())
    }

    var isEditMode by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    
    var editingPointIndex by remember { mutableStateOf<Int?>(null) }
    var pointNameInput by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(checkedIndices) { prefs.edit().putString("checked_list", checkedIndices.joinToString(",")).apply() }
    LaunchedEffect(patrolPoints) { prefs.edit().putString("custom_patrol_points", patrolPoints.joinToString("||")).apply() }

    if (showAddDialog || editingPointIndex != null) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; editingPointIndex = null; pointNameInput = "" },
            title = { Text(if (editingPointIndex != null) "포인트 수정" else "새 포인트 추가", fontWeight = FontWeight.Bold) },
            text = { OutlinedTextField(value = pointNameInput, onValueChange = { pointNameInput = it }, label = { Text("지점 명칭") }, modifier = Modifier.fillMaxWidth(), singleLine = true) },
            confirmButton = {
                Button(onClick = {
                    if (pointNameInput.isNotBlank()) {
                        if (editingPointIndex != null) {
                            val newList = patrolPoints.toMutableList(); newList[editingPointIndex!!] = pointNameInput; patrolPoints = newList
                        } else patrolPoints = patrolPoints + pointNameInput
                    }
                    showAddDialog = false; editingPointIndex = null; pointNameInput = ""
                }) { Text("확인") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false; editingPointIndex = null; pointNameInput = "" }) { Text("취소") } }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false }, shape = RoundedCornerShape(28.dp), title = { Text("전체 초기화", fontWeight = FontWeight.Black) },
            text = { Text("진행 중인 모든 순찰 기록이 삭제됩니다. 계속하시겠습니까?") },
            confirmButton = { Button(onClick = { checkedIndices = emptySet(); showResetConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), shape = RoundedCornerShape(14.dp)) { Text("초기화", fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showResetConfirm = false }) { Text("취소") } }
        )
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                Surface(shadowElevation = 12.dp, color = Color.White, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onDismiss, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)) { Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onSurface) }
                            Text("PATROL CHECK", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, letterSpacing = 3.sp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(onClick = { isEditMode = !isEditMode }, modifier = Modifier.background(if (isEditMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)) {
                                    Icon(imageVector = if (isEditMode) Icons.Default.Done else Icons.Default.Edit, contentDescription = "Edit", tint = if (isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                }
                                IconButton(onClick = { showResetConfirm = true }, modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f), CircleShape)) { Icon(Icons.Default.Refresh, "Reset", tint = MaterialTheme.colorScheme.error) }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        val progress = if (patrolPoints.isEmpty()) 0f else checkedIndices.size.toFloat() / patrolPoints.size
                        val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = spring(stiffness = Spring.StiffnessLow), label = "progress")
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                Text(text = if (isEditMode) "순찰 지점 편집" else "오늘의 순찰 진행", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                                if (!isEditMode) Text(text = "${(progress * 100).toInt()}%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            }
                            if (!isEditMode) {
                                LinearProgressIndicator(progress = { animatedProgress }, modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                Text(text = "${checkedIndices.size}개 지점 완료 / 총 ${patrolPoints.size}개 | 2초간 꾹 눌러서 체크", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            } else Text(text = "지점을 추가하거나 수정, 삭제할 수 있습니다.", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        }
                    }
                }
            },
            containerColor = Color(0xFFF0F2F5)
        ) { padding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                itemsIndexed(patrolPoints) { index, point ->
                    val isChecked = checkedIndices.contains(index)
                    PatrolItem(
                        index = index, 
                        text = point, 
                        isChecked = isChecked, 
                        isEditMode = isEditMode, 
                        onClick = {
                            if (isEditMode) { 
                                editingPointIndex = index
                                pointNameInput = point 
                            }
                        }, 
                        onLongClick = {
                            if (!isEditMode) {
                                checkedIndices = if (isChecked) checkedIndices - index else checkedIndices + index
                            }
                        },
                        onDelete = {
                            val newList = patrolPoints.toMutableList()
                            newList.removeAt(index)
                            patrolPoints = newList
                            checkedIndices = checkedIndices.filter { it != index }.map { if (it > index) it - 1 else it }.toSet()
                        }
                    )
                }
                if (isEditMode) {
                    item {
                        Button(onClick = { showAddDialog = true }, modifier = Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary)) {
                            Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("순찰 지점 추가", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
private fun PatrolItem(
    index: Int, 
    text: String, 
    isChecked: Boolean, 
    isEditMode: Boolean = false, 
    onClick: () -> Unit, 
    onLongClick: () -> Unit,
    onDelete: () -> Unit = {}
) {
    val backgroundColor by animateColorAsState(if (isChecked && !isEditMode) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else Color.White, label = "bgColor")
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(isChecked, isEditMode) {
                if (isEditMode) {
                    detectTapGestures(onTap = { onClick() })
                } else {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        val held = withTimeoutOrNull(2000) {
                            waitForUpOrCancellation()
                            false // Released before timeout
                        } ?: true // Timed out (held for 2s)

                        if (held) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongClick()
                            // Consume the up event to prevent other interactions
                            waitForUpOrCancellation()
                        }
                    }
                }
            }, 
        shape = RoundedCornerShape(24.dp), 
        color = backgroundColor,
        border = if (isChecked && !isEditMode) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)) else null,
        shadowElevation = if (isChecked && !isEditMode) 0.dp else 6.dp
    ) {
        Row(modifier = Modifier.padding(horizontal = 24.dp, vertical = if (isEditMode) 20.dp else 28.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = if (isChecked && !isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(32.dp)) {
                Box(contentAlignment = Alignment.Center) { Text(text = index.toString(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = if (isChecked && !isEditMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Spacer(Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = if (isChecked && !isEditMode) FontWeight.ExtraBold else FontWeight.Bold, color = if (isChecked && !isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, lineHeight = 24.sp)
                if (isChecked && !isEditMode) {
                    Spacer(Modifier.height(4.dp))
                    Text(text = "COMPLETED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                }
            }
            Spacer(Modifier.width(16.dp))
            if (isEditMode) {
                Row {
                    IconButton(onClick = onClick) { Icon(Icons.Default.Edit, "Edit", tint = Color.Gray.copy(alpha = 0.6f)) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)) }
                }
            } else {
                Crossfade(targetState = isChecked, label = "icon") { checked ->
                    if (checked) Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Checked", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                    else Box(modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}
