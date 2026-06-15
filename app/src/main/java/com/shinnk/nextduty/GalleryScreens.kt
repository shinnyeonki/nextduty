package com.shinnk.nextduty

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun WorkScheduleContent(
    images: List<String>,
    onSaveImages: (List<String>) -> Unit
) {
    val context = LocalContext.current
    var userScrollEnabled by remember { mutableStateOf(true) }
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
            text = { Text("이 이미지를 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    val pathToRemove = images[pagerState.currentPage]
                    val newList = images.toMutableList().apply { removeAt(pagerState.currentPage) }
                    onSaveImages(newList)
                    ImageStorage.deleteFile(context, pathToRemove)
                    showDeleteConfirm = false
                }) { Text("삭제", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("취소") } }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        if (images.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.PhotoLibrary, null, tint = Color.White.copy(0.2f), modifier = Modifier.size(80.dp))
                Spacer(Modifier.height(16.dp))
                Text("등록된 근무표가 없습니다.", color = Color.White.copy(0.4f))
                Spacer(Modifier.height(24.dp))
                Button(onClick = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("이미지 추가")
                }
            }
        } else {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), userScrollEnabled = userScrollEnabled) { page ->
                val path = images[page]
                val file = if (path.startsWith("/")) File(path) else File(context.filesDir, path)
                ZoomableAsyncImage(model = if (file.exists()) file else path, onZoomChanged = { userScrollEnabled = !it })
            }
            
            Box(modifier = Modifier.fillMaxWidth().align(Alignment.TopEnd).padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.background(Color.Black.copy(0.4f), CircleShape)) {
                        Icon(Icons.Default.Delete, null, tint = Color.White)
                    }
                    IconButton(onClick = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.background(Color.Black.copy(0.4f), CircleShape)) {
                        Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.White)
                    }
                }
            }
            
            if (images.size > 1) {
                Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)) {
                    repeat(images.size) { iteration ->
                        val isSelected = pagerState.currentPage == iteration
                        Box(modifier = Modifier.padding(4.dp).size(8.dp).clip(CircleShape).background(if (isSelected) Color.White else Color.White.copy(0.3f)))
                    }
                }
            }
        }
    }
}

@Composable
fun DutyTableContent() {
    var userScrollEnabled by remember { mutableStateOf(true) }
    val pagerState = rememberPagerState { 4 }
    val tableInfo = listOf("주간1 (1, 2번)", "주간1 (3, 4번)", "주간2 (1번)", "주간2 (2, 3번)")
    val images = listOf(R.drawable.duty_ju1_12, R.drawable.duty_ju1_34, R.drawable.duty_ju2_1, R.drawable.duty_ju2_23)

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), userScrollEnabled = userScrollEnabled) { page ->
            ZoomableImage(resId = images[page], onZoomChanged = { userScrollEnabled = !it })
        }
        
        Surface(
            modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
            color = Color.Black.copy(alpha = 0.5f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(tableInfo[pagerState.currentPage], color = Color.White, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }

        Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)) {
            repeat(4) { iteration ->
                val isSelected = pagerState.currentPage == iteration
                Box(modifier = Modifier.padding(4.dp).size(8.dp).clip(CircleShape).background(if (isSelected) Color.White else Color.White.copy(0.3f)))
            }
        }
    }
}
