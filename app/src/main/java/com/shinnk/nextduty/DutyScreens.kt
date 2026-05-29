package com.shinnk.nextduty

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.Duration
import java.util.Locale
import kotlin.math.abs

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
    var showTableDialog by remember { mutableStateOf(false) }

    if (showTableDialog) {
        DutyTableDialog(
            initialIndex = 0,
            onDismiss = { showTableDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopActionBar(
                isAppActive = isAppActive,
                onToggleActive = onSaveAppActiveStatus,
                onShowTable = { showTableDialog = true }
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
private fun TopActionBar(
    isAppActive: Boolean, 
    onToggleActive: (Boolean) -> Unit,
    onShowTable: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 좌측: 편성표 버튼
                TextButton(
                    onClick = onShowTable,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange, 
                        contentDescription = null, 
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "편성표", 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                // 우측: 상태 토글 및 확장 영역
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 향후 추가될 버튼 (예: 설정)을 위한 자리
                    // IconButton(onClick = { /* TODO */ }) { Icon(Icons.Default.Settings, null) }
                    
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
                        ) {
                            Text(
                                text = if (isAppActive) "서비스 ON" else "서비스 OFF",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isAppActive) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                            Spacer(Modifier.width(4.dp))
                            Switch(
                                modifier = Modifier.scale(0.75f),
                                checked = isAppActive,
                                onCheckedChange = onToggleActive,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }
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

    val dutyInfo = remember(currentTime, settings) {
        DutyCore.calculateDutyInfo(currentTime, settings)
    }
    val currentLocation = dutyInfo.currentLoc
    val currentRange = dutyInfo.currentRange
    val nextLocation = dutyInfo.nextLoc
    val nextStartTime = dutyInfo.nextStart
    val remaining = dutyInfo.remaining

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
                    text = DutyCore.formatDuration(remaining),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
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

@Composable
fun DutyTableDialog(
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    var userScrollEnabled by remember { mutableStateOf(true) }
    val pagerState = rememberPagerState(initialPage = initialIndex) { 4 }
    val tableInfo = listOf(
        "주간1 (1, 2번)",
        "주간1 (3, 4번)",
        "주간2 (1번)",
        "주간2 (2, 3번)"
    )
    val images = listOf(
        R.drawable.duty_ju1_12,
        R.drawable.duty_ju1_34,
        R.drawable.duty_ju2_1,
        R.drawable.duty_ju2_23
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false // 이미지 조작 중 실수로 닫히는 방지
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    key = { it },
                    pageSpacing = 16.dp,
                    beyondViewportPageCount = 1,
                    userScrollEnabled = userScrollEnabled
                ) { page ->
                    ZoomableImage(
                        resId = images[page],
                        onZoomChanged = { isZoomed -> userScrollEnabled = !isZoomed }
                    )
                }

                // Top Bar in Dialog
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.3f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                    Surface(
                        color = Color.Black.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = tableInfo[pagerState.currentPage],
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    Spacer(Modifier.size(48.dp))
                }

                // Indicators
                Row(
                    Modifier
                        .height(50.dp)
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(4) { iteration ->
                        val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoomableImage(resId: Int, onZoomChanged: (Boolean) -> Unit) {
    var scale by remember(resId) { mutableFloatStateOf(1f) }
    var offset by remember(resId) { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RectangleShape)
            .pointerInput(resId) {
                detectTapGestures(onDoubleTap = {
                    if (scale > 1f) {
                        scale = 1f
                        offset = Offset.Zero
                        onZoomChanged(false)
                    } else {
                        scale = 3f
                        onZoomChanged(true)
                    }
                })
            }
            .pointerInput(resId) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val isMultiTouch = event.changes.size > 1
                        
                        // 이미지가 확대되었거나, 두 손가락으로 줌을 시도할 때
                        if (scale > 1f || isMultiTouch) {
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()
                            
                            // 실제 변화가 있을 때만 이벤트를 소비(Consume)
                            // 이렇게 해야 DOWN/UP 이벤트가 통과되어 더블 탭이 작동함
                            if (zoomChange != 1f || panChange != Offset.Zero) {
                                val newScale = (scale * zoomChange).coerceIn(1f, 5f)
                                scale = newScale
                                if (scale > 1f) {
                                    offset += panChange
                                } else {
                                    offset = Offset.Zero
                                }
                                onZoomChanged(scale > 1f)
                                event.changes.forEach { it.consume() }
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
            contentScale = ContentScale.Fit
        )
    }
}
