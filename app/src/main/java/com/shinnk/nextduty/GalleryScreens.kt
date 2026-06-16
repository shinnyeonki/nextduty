package com.shinnk.nextduty

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
private fun GalleryScreen(
    images: List<String>,
    onSaveImages: (List<String>) -> Unit,
    emptyText: String
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

    if (showDeleteConfirm && images.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("이미지 삭제") },
            text = { Text("이 이미지를 사진첩에서 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    val pathToRemove = images[pagerState.currentPage]
                    val newList = images.toMutableList().apply { removeAt(pagerState.currentPage) }
                    onSaveImages(newList)
                    if (!pathToRemove.startsWith("res:")) {
                        ImageStorage.deleteFile(context, pathToRemove)
                    }
                    showDeleteConfirm = false
                }) { Text("삭제", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("취소") } }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (images.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.PhotoLibrary, null, tint = Color.White.copy(0.2f), modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(16.dp))
                Text(emptyText, color = Color.White.copy(0.4f))
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("사진 추가", color = Color.White)
                }
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = userScrollEnabled,
                pageSpacing = 0.dp
            ) { page ->
                val path = images[page]
                val model = when {
                    path.startsWith("res:duty_ju1_12") -> R.drawable.duty_ju1_12
                    path.startsWith("res:duty_ju1_34") -> R.drawable.duty_ju1_34
                    path.startsWith("res:duty_ju2_1") -> R.drawable.duty_ju2_1
                    path.startsWith("res:duty_ju2_23") -> R.drawable.duty_ju2_23
                    path.startsWith("/") -> File(path)
                    else -> File(context.filesDir, path).let { if (it.exists()) it else path }
                }
                ZoomableAsyncImage(model = model, onZoomChanged = { userScrollEnabled = !it })
            }

            // Top Control Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.background(Color.Black.copy(0.4f), CircleShape)
                ) {
                    Icon(Icons.Default.Delete, null, tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.background(Color.Black.copy(0.4f), CircleShape)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.White)
                }
            }
            
            // Bottom Indicator
            if (images.size > 1) {
                Row(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                ) {
                    repeat(images.size) { iteration ->
                        val isSelected = pagerState.currentPage == iteration
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White else Color.White.copy(0.3f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkScheduleContent(
    images: List<String>,
    onSaveImages: (List<String>) -> Unit
) {
    GalleryScreen(
        images = images,
        onSaveImages = onSaveImages,
        emptyText = "등록된 근무표가 없습니다."
    )
}

@Composable
fun DutyTableContent(
    images: List<String>,
    onSaveImages: (List<String>) -> Unit
) {
    GalleryScreen(
        images = images,
        onSaveImages = onSaveImages,
        emptyText = "등록된 편성표가 없습니다."
    )
}
