package com.shinnk.nextduty

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.io.FileOutputStream

// --- Storage Helper ---

object ImageStorage {
    fun saveUriToInternal(context: android.content.Context, uri: Uri): String? {
        return try {
            val fileName = "duty_${System.currentTimeMillis()}_${(100..999).random()}.jpg"
            val destFile = File(context.filesDir, "work_schedules/$fileName")
            destFile.parentFile?.mkdirs()
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun deleteFile(path: String) {
        try {
            val file = File(path)
            if (file.exists()) file.delete()
        } catch (e: Exception) { /* ignore */ }
    }
}

// --- Main App Component ---

@Composable
fun DutyApp(
    dutySettings: DutySettings?,
    ptStatus: Boolean,
    isAppActive: Boolean,
    workScheduleImages: List<String>,
    onSaveSettings: (String, Int, Int) -> Unit,
    onSavePtStatus: (Boolean) -> Unit,
    onSaveAppActiveStatus: (Boolean) -> Unit,
    onSaveWorkScheduleImages: (List<String>) -> Unit,
    onEdit: () -> Unit,
) {
    var isEditing by remember { mutableStateOf(false) }
    var showTableDialog by remember { mutableStateOf(false) }
    var showWorkScheduleDialog by remember { mutableStateOf(false) }
    var showPatrolDialog by remember { mutableStateOf(false) }

    if (showTableDialog) {
        DutyTableDialog(onDismiss = { showTableDialog = false })
    }

    if (showWorkScheduleDialog) {
        WorkScheduleDialog(
            images = workScheduleImages,
            onSaveImages = onSaveWorkScheduleImages,
            onDismiss = { showWorkScheduleDialog = false }
        )
    }

    if (showPatrolDialog) {
        PatrolDialog(onDismiss = { showPatrolDialog = false })
    }

    Scaffold(
        topBar = {
            PremiumTopBar(
                isAppActive = isAppActive,
                onToggleActive = onSaveAppActiveStatus,
                onShowTable = { showTableDialog = true },
                onShowWorkSchedule = { showWorkScheduleDialog = true },
                onShowPatrol = { showPatrolDialog = true }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AnimatedContent(
                targetState = dutySettings == null || isEditing,
                transitionSpec = {
                    fadeIn(tween(400)) togetherWith fadeOut(tween(400))
                },
                label = "ScreenTransition"
            ) { editing ->
                if (editing) {
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
                    StatusScreen(dutySettings!!, onEdit = {
                        isEditing = true
                        onEdit()
                    })
                }
            }

            AnimatedVisibility(
                visible = !isAppActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                PremiumInactiveOverlay()
            }
        }
    }
}

// --- Components: Top Bar & Overlay ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumTopBar(
    isAppActive: Boolean, 
    onToggleActive: (Boolean) -> Unit,
    onShowTable: () -> Unit,
    onShowWorkSchedule: () -> Unit,
    onShowPatrol: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {},
        navigationIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onShowTable) {
                    Icon(Icons.AutoMirrored.Filled.List, "편성표", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onShowWorkSchedule) {
                    Icon(Icons.Default.DateRange, "근무표", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onShowPatrol) {
                    Icon(Icons.Default.Security, "순찰", tint = MaterialTheme.colorScheme.primary)
                }
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Text(
                    text = if (isAppActive) "ON" else "OFF",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isAppActive) MaterialTheme.colorScheme.primary else Color.Gray
                )
                Spacer(Modifier.width(6.dp))
                Switch(
                    modifier = Modifier.scale(0.8f),
                    checked = isAppActive,
                    onCheckedChange = onToggleActive,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
private fun PremiumInactiveOverlay() {
    Surface(
        modifier = Modifier.fillMaxSize().clickable(enabled = false) { },
        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.75f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                }
                Spacer(Modifier.height(32.dp))
                Text("서비스 중지됨", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
                Spacer(Modifier.height(12.dp))
                Text(
                    "상단 스위치를 켜면 알림, 근무 확인 서비스가 다시 시작됩니다.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// --- Screen: Input / Setup ---

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
            .padding(horizontal = 24.dp)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            "NEXT DUTY",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            letterSpacing = 3.sp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
        Spacer(Modifier.height(8.dp))
        Text("Setup your duty", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
        Text("근무 정보를 설정하고 서비스를 시작하세요.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        
        Spacer(Modifier.height(32.dp))

        PremiumInputSection("근무 시간", Icons.Default.Settings) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("주간1" to "JU1", "주간2" to "JU2").forEach { (label, value) ->
                    PremiumSelectableChip(
                        label = label,
                        selected = selectedTime == value,
                        onClick = { selectedTime = value },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        
        PremiumInputSection("편성표 번호", Icons.AutoMirrored.Filled.List) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                (1..4).forEach { i ->
                    PremiumSelectableChip(
                        label = i.toString(),
                        selected = selectedTable == i,
                        onClick = { selectedTable = i },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        
        PremiumInputSection("나의 근무 번호", Icons.Default.Person) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                (1..4).forEach { i ->
                    PremiumSelectableChip(
                        label = i.toString(),
                        selected = selectedNumber == i,
                        onClick = { selectedNumber = i },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        
        PtStatusCard(ptStatus, onSavePtStatus)

        Spacer(Modifier.height(32.dp))
        
        Button(
            onClick = { onSave(selectedTime, selectedTable, selectedNumber) },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("저장 및 동기화", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        }
        
        Spacer(Modifier.height(24.dp))
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
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("PT 여부", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("PT 근무를 적용할까요?", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Switch(
                checked = ptStatus, 
                onCheckedChange = onSavePtStatus,
                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

// --- Screen: Status / Dashboard ---

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))
        Text(
            "NEXT DUTY",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            letterSpacing = 3.sp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
        Spacer(Modifier.height(24.dp))
        
        // Premium Clock (Side-by-side)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = currentTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                style = TextStyle(
                    fontSize = 76.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-2).sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    shadow = Shadow(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        offset = Offset(0f, 8f),
                        blurRadius = 15f
                    )
                )
            )
            Text(
                text = ":" + currentTime.format(DateTimeFormatter.ofPattern("ss")),
                modifier = Modifier.padding(bottom = 14.dp, start = 4.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }
        
        Spacer(Modifier.height(32.dp))
        
        PremiumDutyCard("현재 근무지", dutyInfo.currentLoc, dutyInfo.currentRange, true, Icons.Default.LocationOn)
        Spacer(Modifier.height(16.dp))
        PremiumDutyCard("다음 근무지", dutyInfo.nextLoc, dutyInfo.nextStart, false, Icons.Default.Info)

        Spacer(Modifier.height(24.dp))
        
        // Compact Countdown
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "퇴근까지 남은 시간", 
                    style = MaterialTheme.typography.labelSmall, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = DutyCore.formatDuration(dutyInfo.remaining),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Info Summary Footer
        Surface(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            shape = RoundedCornerShape(24.dp),
            onClick = onEdit
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(if(settings.time == "JU1") "주간1" else "주간2")
                            }
                            append(" • 편성표 ${settings.table}번 • ${settings.number}번")
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = if(settings.isPt) "PT 근무 모드" else "일반 근무 모드",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// --- Shared Premium UI Elements ---

@Composable
private fun PremiumDutyCard(title: String, location: String, range: String, isActive: Boolean, icon: ImageVector) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "alpha"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) 
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.6f)
                )
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = alpha), CircleShape)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else Color.Gray,
                    letterSpacing = 0.2.sp
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    text = location,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (range.isNotEmpty()) {
                    Text(
                        text = range,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumSelectableChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(if (selected) 1.03f else 1f, label = "scale")
    Surface(
        onClick = onClick,
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (selected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        tonalElevation = if (selected) 4.dp else 0.dp
    ) {
        Box(modifier = Modifier.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = if (selected) FontWeight.Black else FontWeight.Medium, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun PremiumInputSection(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

// --- Dialogs ---

@Composable
fun WorkScheduleDialog(
    images: List<String>,
    onSaveImages: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState { images.size }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val newPaths = uris.mapNotNull { ImageStorage.saveUriToInternal(context, it) }
            onSaveImages(images + newPaths)
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("근무표 삭제") },
            text = { Text("등록된 근무표를 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val pathToRemove = images[pagerState.currentPage]
                        val newList = images.toMutableList().apply { removeAt(pagerState.currentPage) }
                        onSaveImages(newList)
                        ImageStorage.deleteFile(pathToRemove)
                        showDeleteConfirm = false
                    }
                ) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("취소")
                }
            }
        )
    }
    
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            if (images.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.PhotoLibrary, null, tint = Color.White.copy(0.3f), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("등록된 근무표가 없습니다.", color = Color.White.copy(0.5f))
                }
            } else {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    val path = images[page]
                    if (File(path).exists()) {
                        ZoomableAsyncImage(model = File(path))
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("이미지를 찾을 수 없습니다.", color = Color.White)
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).background(Brush.verticalGradient(listOf(Color.Black.copy(0.6f), Color.Transparent))).statusBarsPadding().padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss, modifier = Modifier.background(Color.White.copy(0.1f), CircleShape)) { Icon(Icons.Default.Close, "Close", tint = Color.White) }
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (images.isNotEmpty()) {
                            IconButton(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier.background(Color.Red.copy(0.2f), CircleShape)
                            ) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color.White)
                            }
                        }
                        
                        IconButton(
                            onClick = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.background(Color.White.copy(0.1f), CircleShape)
                        ) { Icon(Icons.Default.PhotoLibrary, "Gallery", tint = Color.White) }
                    }
                }
            }
            
            if (images.size > 1) {
                Row(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 24.dp)) {
                    repeat(images.size) { iteration ->
                        val isSelected = pagerState.currentPage == iteration
                        val width by animateDpAsState(if (isSelected) 20.dp else 8.dp, label = "width")
                        Box(modifier = Modifier.padding(4.dp).height(8.dp).width(width).clip(CircleShape).background(if (isSelected) Color.White else Color.White.copy(0.3f)))
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoomableAsyncImage(model: Any) {
    var scale by remember(model) { mutableFloatStateOf(1f) }
    var offset by remember(model) { mutableStateOf(Offset.Zero) }
    Box(modifier = Modifier.fillMaxSize().pointerInput(model) {
        detectTapGestures(onDoubleTap = {
            if (scale > 1f) { scale = 1f; offset = Offset.Zero } else { scale = 2.5f }
        })
    }.pointerInput(model) {
        awaitEachGesture {
            awaitFirstDown(false)
            do {
                val event = awaitPointerEvent()
                if (scale > 1f || event.changes.size > 1) {
                    val zoomChange = event.calculateZoom()
                    val panChange = event.calculatePan()
                    if (zoomChange != 1f || panChange != Offset.Zero) {
                        scale = (scale * zoomChange).coerceIn(1f, 5f)
                        if (scale > 1f) offset += panChange else offset = Offset.Zero
                        event.changes.forEach { it.consume() }
                    }
                }
            } while (event.changes.any { it.pressed })
        }
    }) {
        AsyncImage(model = model, contentDescription = null, modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = scale; scaleY = scale; translationX = offset.x; translationY = offset.y }, contentScale = ContentScale.Fit)
    }
}

@Composable
fun DutyTableDialog(onDismiss: () -> Unit) {
    var userScrollEnabled by remember { mutableStateOf(true) }
    val pagerState = rememberPagerState { 4 }
    val tableInfo = listOf("주간1 (1, 2번)", "주간1 (3, 4번)", "주간2 (1번)", "주간2 (2, 3번)")
    val images = listOf(R.drawable.duty_ju1_12, R.drawable.duty_ju1_34, R.drawable.duty_ju2_1, R.drawable.duty_ju2_23)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), userScrollEnabled = userScrollEnabled) { page ->
                ZoomableImage(resId = images[page], onZoomChanged = { userScrollEnabled = !it })
            }
            Box(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).background(Brush.verticalGradient(listOf(Color.Black.copy(0.6f), Color.Transparent))).statusBarsPadding().padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss, modifier = Modifier.background(Color.White.copy(0.1f), CircleShape)) { Icon(Icons.Default.Close, "Close", tint = Color.White) }
                    Surface(color = Color.White.copy(0.1f), shape = RoundedCornerShape(20.dp)) {
                        Text(tableInfo[pagerState.currentPage], color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                    Spacer(Modifier.size(48.dp))
                }
            }
            Row(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 24.dp)) {
                repeat(4) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val width by animateDpAsState(if (isSelected) 20.dp else 8.dp, label = "width")
                    Box(modifier = Modifier.padding(4.dp).height(8.dp).width(width).clip(CircleShape).background(if (isSelected) Color.White else Color.White.copy(0.3f)))
                }
            }
        }
    }
}

@Composable
private fun ZoomableImage(resId: Int, onZoomChanged: (Boolean) -> Unit) {
    var scale by remember(resId) { mutableFloatStateOf(1f) }
    var offset by remember(resId) { mutableStateOf(Offset.Zero) }
    Box(modifier = Modifier.fillMaxSize().pointerInput(resId) {
        detectTapGestures(onDoubleTap = {
            if (scale > 1f) { scale = 1f; offset = Offset.Zero; onZoomChanged(false) } else { scale = 2.5f; onZoomChanged(true) }
        })
    }.pointerInput(resId) {
        awaitEachGesture {
            awaitFirstDown(false)
            do {
                val event = awaitPointerEvent()
                if (scale > 1f || event.changes.size > 1) {
                    val zoomChange = event.calculateZoom()
                    val panChange = event.calculatePan()
                    if (zoomChange != 1f || panChange != Offset.Zero) {
                        scale = (scale * zoomChange).coerceIn(1f, 5f)
                        if (scale > 1f) offset += panChange else offset = Offset.Zero
                        onZoomChanged(scale > 1f)
                        event.changes.forEach { it.consume() }
                    }
                }
            } while (event.changes.any { it.pressed })
        }
    }) {
        Image(painter = painterResource(resId), null, modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = scale; scaleY = scale; translationX = offset.x; translationY = offset.y }, contentScale = ContentScale.Fit)
    }
}
