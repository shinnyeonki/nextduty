package com.shinnk.nextduty

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

val PATROL_POINTS = listOf(
    "0 본인 사물함",
    "1 1층 사무존 남자 화장실",
    "2 2층 진로설계관內 여자 화장실",
    "3 숙련관 동측 1층 비상계단 앞",
    "4 숙련관 1층 남자 화장실",
    "5 숙련관 서측 3층 비상계단 앞",
    "6 숙련관 3층 남자 화장실",
    "7 숙련관 동측 3층 비상계단 앞",
    "8 숙련관 2층 남자 화장실",
    "9 숙련관 동측 2층 비상계단 앞",
    "10 숙련관 2층 복도 끝 남자 화장실",
    "11 숙련관 1층 직원 출입문 입구(안쪽)",
    "12 2층 식당 좌 옆 여자 화장실",
    "13 2층 안내데스크 좌측 여자 화장실",
    "14 2층 유아놀이방 內 좌측 여자 화장실",
    "15 지하1층 나래울극장안쪽 분장대기실(좌/문틀)",
    "16 옥외1층 기사 대기실 옆 출입구",
    "17 옥외1층 정산소 주 출입구",
    "18 옥외2층 주차장 EV앞",
    "19 옥외1층 실내 주차장 입구(D6)",
    "20 5층 공조실-1 앞 소화전 좌/옆",
    "21 4층 여자 화장실",
    "22 3층 청체험관 內 여자 화장실",
    "23 3층 청체험관입구 좌/옆 여자 화장실",
    "24 3층 조이샵 좌/옆 여자 화장실",
    "25 3층 어체험관 內 여자 화장실",
    "26 1층 나래울극장 옆 여자 화장실",
    "27 1층 한울강당 옆 여자 화장실",
    "28 1층 창의실 옆 여자 화장실"
)

@Composable
fun PatrolDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("patrol_prefs", Context.MODE_PRIVATE) }
    
    var checkedIndices by remember { 
        mutableStateOf(
            prefs.getString("checked_list", "")
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.mapNotNull { it.toIntOrNull() }
                ?.toSet() ?: emptySet()
        )
    }

    var pendingCheckIndex by remember { mutableStateOf<Int?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(checkedIndices) {
        prefs.edit().putString("checked_list", checkedIndices.joinToString(",")).apply()
    }

    if (pendingCheckIndex != null) {
        AlertDialog(
            onDismissRequest = { pendingCheckIndex = null },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { 
                Text(
                    "지점 확인", 
                    style = MaterialTheme.typography.headlineSmall, 
                    fontWeight = FontWeight.Black 
                ) 
            },
            text = { 
                Text(
                    "\"${PATROL_POINTS[pendingCheckIndex!!]}\"\n이 지점을 확인했나요?", 
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        checkedIndices = checkedIndices + pendingCheckIndex!!
                        pendingCheckIndex = null
                    },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("확인 완료", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingCheckIndex = null }) {
                    Text("취소", color = Color.Gray)
                }
            }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            shape = RoundedCornerShape(28.dp),
            title = { Text("전체 초기화", fontWeight = FontWeight.Black) },
            text = { Text("진행 중인 모든 순찰 기록이 삭제됩니다. 계속하시겠습니까?") },
            confirmButton = {
                Button(
                    onClick = {
                        checkedIndices = emptySet()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("초기화", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("취소")
                }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                Surface(
                    shadowElevation = 12.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onSurface)
                            }
                            
                            Text(
                                "PATROL CHECK",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 3.sp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )

                            IconButton(
                                onClick = { showResetConfirm = true },
                                modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(Icons.Default.Refresh, "Reset", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        // Progress Section
                        val progress = if (PATROL_POINTS.isEmpty()) 0f else checkedIndices.size.toFloat() / PATROL_POINTS.size
                        val animatedProgress by animateFloatAsState(
                            targetValue = progress, 
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "progress"
                        )
                        
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = "오늘의 순찰 진행",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            )
                            Text(
                                text = "${checkedIndices.size}개 지점 완료 / 총 ${PATROL_POINTS.size}개",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            },
            containerColor = Color(0xFFF0F2F5)
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(PATROL_POINTS) { index, point ->
                    val isChecked = checkedIndices.contains(index)
                    PatrolItem(
                        text = point,
                        isChecked = isChecked,
                        onClick = {
                            if (!isChecked) {
                                pendingCheckIndex = index
                            }
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun PatrolItem(
    text: String,
    isChecked: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(if (isChecked) 1f else 1f, label = "scale")
    val backgroundColor by animateColorAsState(
        if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) 
        else Color.White,
        label = "bgColor"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
        border = if (isChecked) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)) else null,
        shadowElevation = if (isChecked) 0.dp else 6.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 28.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isChecked) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
                )
                if (isChecked) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "COMPLETED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Crossfade(targetState = isChecked, label = "icon") { checked ->
                if (checked) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Checked",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                    )
                }
            }
        }
    }
}
