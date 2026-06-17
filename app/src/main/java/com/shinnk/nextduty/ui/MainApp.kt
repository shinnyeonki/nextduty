package com.shinnk.nextduty.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shinnk.nextduty.data.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    dutySettings: DutySettings?,
    allTables: List<DutyTable>,
    shiftPattern: ShiftPattern,
    isAppActive: Boolean,
    workScheduleImages: List<String>,
    dutyTableImages: List<String>,
    alarmLeadTime: Int,
    onSaveSettings: (String, Int) -> Unit,
    onSaveShiftPattern: (ShiftPattern) -> Unit,
    onSaveAppActiveStatus: (Boolean) -> Unit,
    onSaveWorkScheduleImages: (List<String>) -> Unit,
    onSaveDutyTableImages: (List<String>) -> Unit,
    onEdit: () -> Unit,
    onSaveCustomDutyTables: (List<DutyTable>?) -> Unit,
    onSaveAlarmLeadTime: (Int) -> Unit
) {
    var selectedBottomTab by remember { mutableIntStateOf(0) }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(dutySettings) {
        if (dutySettings == null) {
            isEditing = true
        } else {
            isEditing = false
        }
    }
    
    var showPatrolDialog by remember { mutableStateOf(false) }
    var showPlanEditorDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showAppSettingsDialog by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    if (showInfoDialog) InfoDialog(onDismiss = { showInfoDialog = false })
    if (showAppSettingsDialog) AppSettingsDialog(leadTime = alarmLeadTime, onLeadTimeChange = onSaveAlarmLeadTime, onDismiss = { showAppSettingsDialog = false })
    if (showPlanEditorDialog) DutyPlanEditorDialog(initialTables = allTables, onSave = onSaveCustomDutyTables, onDismiss = { showPlanEditorDialog = false })
    if (showPatrolDialog) PatrolDialog(onDismiss = { showPatrolDialog = false })

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surface, drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp), modifier = Modifier.width(300.dp)) {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    Text("더보기", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(40.dp))
                    NavigationDrawerItem(label = { Text("근무 설정", fontWeight = FontWeight.Bold) }, selected = false, onClick = { scope.launch { drawerState.close(); selectedBottomTab = 0; isEditing = true } }, icon = { Icon(Icons.Default.EditNote, null) }, shape = RoundedCornerShape(16.dp))
                    Spacer(Modifier.height(8.dp))
                    NavigationDrawerItem(label = { Text("순찰", fontWeight = FontWeight.Bold) }, selected = false, onClick = { scope.launch { drawerState.close(); showPatrolDialog = true } }, icon = { Icon(Icons.Default.Security, null) }, shape = RoundedCornerShape(16.dp))
                    Spacer(Modifier.height(8.dp))
                    NavigationDrawerItem(label = { Text("알림 설정", fontWeight = FontWeight.Bold) }, selected = false, onClick = { scope.launch { drawerState.close(); showAppSettingsDialog = true } }, icon = { Icon(Icons.Default.NotificationsActive, null) }, shape = RoundedCornerShape(16.dp))
                    Spacer(Modifier.height(8.dp))
                    NavigationDrawerItem(label = { Text("편성표 데이터 수정", fontWeight = FontWeight.Bold) }, selected = false, onClick = { scope.launch { drawerState.close(); showPlanEditorDialog = true } }, icon = { Icon(Icons.Default.EditCalendar, null) }, shape = RoundedCornerShape(16.dp))
                    Spacer(Modifier.height(8.dp))
                    NavigationDrawerItem(label = { Text("정보", fontWeight = FontWeight.Bold) }, selected = false, onClick = { scope.launch { drawerState.close(); showInfoDialog = true } }, icon = { Icon(Icons.Default.Info, null) }, shape = RoundedCornerShape(16.dp))
                    Spacer(Modifier.weight(1f))
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("서비스 알람 ON", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Switch(checked = isAppActive, onCheckedChange = { onSaveAppActiveStatus(it); scope.launch { drawerState.close() } }, modifier = Modifier.scale(0.8f))
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = { CenterAlignedTopAppBar(title = { Text(when(selectedBottomTab) { 1 -> "근무표 사진첩"; 2 -> "편성표 사진첩"; else -> "NEXT DUTY" }, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.primary) }, navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, "Menu") } }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)) },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                    NavigationBarItem(selected = selectedBottomTab == 0, onClick = { selectedBottomTab = 0; isEditing = false }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("홈") })
                    NavigationBarItem(selected = selectedBottomTab == 1, onClick = { selectedBottomTab = 1 }, icon = { Icon(Icons.Default.PhotoLibrary, null) }, label = { Text("근무표 사진첩") })
                    NavigationBarItem(selected = selectedBottomTab == 2, onClick = { selectedBottomTab = 2 }, icon = { Icon(Icons.AutoMirrored.Filled.List, null) }, label = { Text("편성표 사진첩") })
                }
            },
            containerColor = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding).clipToBounds()) {
                AnimatedContent(targetState = selectedBottomTab, transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) }, label = "TabTransition") { tabIndex ->
                    when (tabIndex) {
                        0 -> HomeFeature(
                            dutySettings = dutySettings, 
                            allTables = allTables, 
                            shiftPattern = shiftPattern, 
                            isEditing = isEditing,
                            onSaveSettings = { tableName, number ->
                                onSaveSettings(tableName, number)
                                isEditing = false
                            }, 
                            onSaveShiftPattern = onSaveShiftPattern,
                            onEdit = {
                                isEditing = true
                                onEdit()
                            }
                        )
                        1 -> GalleryFeature(images = workScheduleImages, onSaveImages = onSaveWorkScheduleImages)
                        2 -> GalleryFeature(images = dutyTableImages, onSaveImages = onSaveDutyTableImages)
                    }
                }
                AnimatedVisibility(visible = !isAppActive, enter = fadeIn(), exit = fadeOut()) {
                    Surface(modifier = Modifier.fillMaxSize().clickable(enabled = false) { }, color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.75f)) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp).background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), CircleShape).border(2.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f), CircleShape)) { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp)) }
                                Spacer(Modifier.height(32.dp)); Text("서비스 중지됨", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
                                Spacer(Modifier.height(12.dp)); Text("좌측 메뉴의 '서비스 알람'을 켜주세요.", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    }
}
