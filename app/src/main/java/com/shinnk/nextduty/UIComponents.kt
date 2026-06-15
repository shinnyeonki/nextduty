package com.shinnk.nextduty

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun PremiumDutyCard(
    title: String,
    location: String,
    range: String,
    isActive: Boolean,
    icon: ImageVector
) {
    val alpha by rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = ""
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
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
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
                    Box(modifier = Modifier.fillMaxSize().border(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = alpha), CircleShape))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold, 
                    color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else Color.Gray
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = location, 
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black), 
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (range.isNotEmpty()) {
                    Text(
                        text = range, 
                        style = MaterialTheme.typography.titleSmall, 
                        color = Color.Gray.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumSelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(if (selected) 1.03f else 1f, label = "")
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

@Composable
fun ZoomableAsyncImage(model: Any, onZoomChanged: (Boolean) -> Unit) {
    var scale by remember(model) { mutableFloatStateOf(1f) }
    var offset by remember(model) { mutableStateOf(Offset.Zero) }
    Box(modifier = Modifier.fillMaxSize().pointerInput(model) {
        detectTapGestures(onDoubleTap = { if (scale > 1f) { scale = 1f; offset = Offset.Zero; onZoomChanged(false) } else { scale = 2.5f; onZoomChanged(true) } })
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
                        onZoomChanged(scale > 1f)
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
fun ZoomableImage(resId: Int, onZoomChanged: (Boolean) -> Unit) {
    var scale by remember(resId) { mutableFloatStateOf(1f) }
    var offset by remember(resId) { mutableStateOf(Offset.Zero) }
    Box(modifier = Modifier.fillMaxSize().pointerInput(resId) {
        detectTapGestures(onDoubleTap = { if (scale > 1f) { scale = 1f; offset = Offset.Zero; onZoomChanged(false) } else { scale = 2.5f; onZoomChanged(true) } })
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
        Image(painter = painterResource(resId), contentDescription = null, modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = scale; scaleY = scale; translationX = offset.x; translationY = offset.y }, contentScale = ContentScale.Fit)
    }
}
