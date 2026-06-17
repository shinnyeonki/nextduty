package com.shinnk.nextduty.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.File
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.shinnk.nextduty.system.ImageStorage

@Composable
fun GalleryFeature(
    images: List<String>,
    onSaveImages: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { images.size })
    var isZoomed by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        isZoomed = false
    }
    
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        if (uris.isNotEmpty()) {
            val savedPaths = uris.mapNotNull { ImageStorage.saveImageToInternal(context, it) }
            if (savedPaths.isNotEmpty()) onSaveImages(images + savedPaths)
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("사진 삭제", fontWeight = FontWeight.Bold) },
            text = { Text("현재 보고 있는 사진을 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    val currentPath = images.getOrNull(pagerState.currentPage)
                    if (currentPath != null) onSaveImages(images.filter { it != currentPath })
                    showDeleteConfirm = false
                }) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("취소") }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (images.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("등록된 사진이 없습니다.", color = Color.White.copy(alpha = 0.5f))
                Spacer(Modifier.height(16.dp))
                Button(onClick = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Text("사진 추가")
                }
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageSpacing = 16.dp,
                userScrollEnabled = !isZoomed,
                beyondViewportPageCount = 1
            ) { page ->
                val path = images[page]
                ZoomableAsyncImage(
                    model = path,
                    onZoomChanged = { isZoomed = it }
                )
            }

            // Top Overlay Actions
            if (!isZoomed) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentPath = images.getOrNull(pagerState.currentPage)
                    if (currentPath != null && !currentPath.startsWith("res:")) {
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    
                    IconButton(
                        onClick = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, "Add", tint = Color.White)
                    }
                }

                // Bottom Page Indicator
                Surface(
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                        .align(Alignment.BottomCenter),
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${images.size}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ZoomableAsyncImage(model: String, onZoomChanged: (Boolean) -> Unit) {
    val context = LocalContext.current
    var scale by remember(model) { mutableFloatStateOf(1f) }
    var offset by remember(model) { mutableStateOf(Offset.Zero) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    // State to keep track of resource loading
    val isResource = model.startsWith("res:")
    val resId = remember(model, context) {
        if (isResource) {
            val resName = model.substringAfter("res:")
            // Directly reference R.drawable constants to prevent the resource shrinker 
            // from removing these resources in release builds.
            when (resName) {
                "duty_ju1_12" -> com.shinnk.nextduty.R.drawable.duty_ju1_12
                "duty_ju1_34" -> com.shinnk.nextduty.R.drawable.duty_ju1_34
                "duty_ju2_1" -> com.shinnk.nextduty.R.drawable.duty_ju2_1
                "duty_ju2_23" -> com.shinnk.nextduty.R.drawable.duty_ju2_23
                else -> context.resources.getIdentifier(resName, "drawable", context.packageName)
            }
        } else 0
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(model) {
                awaitEachGesture {
                    awaitFirstDown()
                    do {
                        val event = awaitPointerEvent()
                        val zoomChange = event.calculateZoom()
                        val panChange = event.calculatePan()

                        if (scale > 1f || zoomChange != 1f) {
                            val newScale = (scale * zoomChange).coerceIn(1f, 5f)
                            if (newScale != scale || panChange != Offset.Zero) {
                                scale = newScale
                                offset = if (scale > 1f) offset + panChange else Offset.Zero
                                onZoomChanged(scale > 1f)
                                event.changes.forEach { it.consume() }
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (isResource) {
            if (resId != 0) {
                Image(
                    painter = painterResource(resId),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        ),
                    contentScale = ContentScale.Fit
                )
                // painterResource is synchronous and usually doesn't need a loader
                SideEffect {
                    isLoading = false
                    isError = false
                }
            } else {
                SideEffect {
                    isLoading = false
                    isError = true
                }
            }
        } else {
            val file = remember(model) { File(model) }
            if (file.exists()) {
                AsyncImage(
                    model = file,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        ),
                    contentScale = ContentScale.Fit,
                    onState = { state ->
                        isLoading = state is AsyncImagePainter.State.Loading
                        isError = state is AsyncImagePainter.State.Error
                    }
                )
            } else {
                SideEffect {
                    isLoading = false
                    isError = true
                }
            }
        }
        
        if (isLoading && !isError) {
            CircularProgressIndicator(color = Color.White.copy(alpha = 0.5f))
        }

        if (isError) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ErrorOutline, "Error", tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text(
                    "이미지를 불러올 수 없습니다.",
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
