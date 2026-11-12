package come.asg.ominoy.app.mobile.feature.main

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

@Composable
internal fun ArtworkEditorDialog(
    sourceUri: Uri,
    onDismiss: () -> Unit,
    onArtworkReady: (Uri) -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()
    val transformState = remember(sourceUri) { CropTransformState() }
    val animationProgress = remember(sourceUri) { Animatable(0f) }
    val animationJob = remember(sourceUri) { AtomicReference<Job?>(null) }
    val velocityTracker = remember(sourceUri) { VelocityTracker() }
    var preview by remember(sourceUri) { mutableStateOf<ArtworkPreview?>(null) }
    var loading by remember(sourceUri) { mutableStateOf(true) }
    var saving by remember(sourceUri) { mutableStateOf(false) }
    var error by remember(sourceUri) { mutableStateOf<String?>(null) }
    val latestPreview by rememberUpdatedState(preview)
    val previewDecodeEdgePx = remember(configuration.screenWidthDp, density.density) {
        (configuration.screenWidthDp * density.density * 0.78f).toInt().coerceIn(512, MAX_PREVIEW_EDGE)
    }
    val previewImage = remember(preview) { preview?.bitmap?.asImageBitmap() }

    LaunchedEffect(sourceUri, previewDecodeEdgePx) {
        loading = true
        preview = try {
            withContext(Dispatchers.IO) { decodeArtworkPreview(context, sourceUri, previewDecodeEdgePx) }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
        loading = false
        error = if (preview == null) "This image could not be opened. Choose a different image." else null
        preview?.let {
            clampCropTransformInto(
                state = transformState,
                sourceWidth = it.sourceWidth,
                sourceHeight = it.sourceHeight,
                viewportSizePx = previewDecodeEdgePx.toFloat(),
                requestedScale = 1f,
                requestedOffsetX = 0f,
                requestedOffsetY = 0f,
                requestedRotationDegrees = 0f,
            )
        }
    }

    DisposableEffect(sourceUri) {
        onDispose {
            animationJob.getAndSet(null)?.cancel()
            latestPreview?.bitmap?.takeIf { !it.isRecycled }?.recycle()
        }
    }

    Dialog(
        onDismissRequest = { if (!saving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.94f).heightIn(max = maxHeight * 0.94f),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
            ) {
                BoxWithConstraints(Modifier.fillMaxWidth().padding(20.dp)) {
                    val cropViewportPx = with(density) { (maxWidth * CROP_WINDOW_FRACTION).toPx() }
                    val activePreview = preview
                    val sourceWidth = activePreview?.sourceWidth ?: 1
                    val sourceHeight = activePreview?.sourceHeight ?: 1
                    val displayScale = activePreview?.let {
                        CROP_WINDOW_FRACTION * max(it.bitmap.width, it.bitmap.height).toFloat() /
                            max(1, min(it.bitmap.width, it.bitmap.height)).toFloat()
                    } ?: 1f

                    fun animateTransformTo(
                        requestedTarget: CropTransform,
                        shortestRotation: Boolean = false,
                        durationMillis: Int = TRANSFORM_ANIMATION_MS,
                    ) {
                        val loaded = activePreview ?: return
                        animationJob.getAndSet(null)?.cancel()
                        val startScale = transformState.scale
                        val startX = transformState.offsetX
                        val startY = transformState.offsetY
                        val startRotation = transformState.rotationDegrees
                        val rotationDelta = if (shortestRotation) {
                            shortestAngleDelta(startRotation, requestedTarget.rotationDegrees)
                        } else {
                            requestedTarget.rotationDegrees - startRotation
                        }
                        val endRotation = startRotation + rotationDelta
                        val target = clampCropTransform(
                            sourceWidth = loaded.sourceWidth,
                            sourceHeight = loaded.sourceHeight,
                            viewportSizePx = cropViewportPx,
                            requested = requestedTarget.copy(rotationDegrees = endRotation),
                        )
                        val job = scope.launch {
                            animationProgress.snapTo(0f)
                            animationProgress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
                            ) {
                                val fraction = value
                                clampCropTransformInto(
                                    state = transformState,
                                    sourceWidth = loaded.sourceWidth,
                                    sourceHeight = loaded.sourceHeight,
                                    viewportSizePx = cropViewportPx,
                                    requestedScale = lerp(startScale, target.scale, fraction),
                                    requestedOffsetX = lerp(startX, target.offsetX, fraction),
                                    requestedOffsetY = lerp(startY, target.offsetY, fraction),
                                    requestedRotationDegrees = startRotation + rotationDelta * fraction,
                                )
                            }
                            clampCropTransformInto(
                                state = transformState,
                                sourceWidth = loaded.sourceWidth,
                                sourceHeight = loaded.sourceHeight,
                                viewportSizePx = cropViewportPx,
                                requestedScale = target.scale,
                                requestedOffsetX = target.offsetX,
                                requestedOffsetY = target.offsetY,
                                requestedRotationDegrees = requestedTarget.rotationDegrees,
                            )
                        }
                        animationJob.set(job)
                    }

                    Column(Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                "Edit artwork",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                            )
                            Text(
                                "Frame the square cover directly. Pinch to zoom, drag to move, or twist two fingers to rotate.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )

                            BoxWithConstraints(
                                modifier = Modifier.fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(Color(0xFF090A0D))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(22.dp))
                                    .pointerInput(sourceUri, activePreview, saving, cropViewportPx) {
                                        val loaded = activePreview
                                        if (loaded != null && !saving) {
                                            awaitEachGesture {
                                                animationJob.getAndSet(null)?.cancel()
                                                velocityTracker.resetTracking()
                                                val firstDown = awaitFirstDown(requireUnconsumed = false)
                                                velocityTracker.addPosition(firstDown.uptimeMillis, firstDown.position)
                                                var previousPointerCount = 1
                                                var pressedCount: Int
                                                var hadMultiplePointers = false
                                                var hitCropEdge = false
                                                var eventTime = firstDown.uptimeMillis

                                                do {
                                                    val event = awaitPointerEvent()
                                                    pressedCount = 0
                                                    for (change in event.changes) {
                                                        eventTime = change.uptimeMillis
                                                        if (change.pressed) pressedCount += 1
                                                    }

                                                    if (pressedCount > 0) {
                                                        if (pressedCount > 1) hadMultiplePointers = true
                                                        val previousCentroid = event.calculateCentroid(useCurrent = false)
                                                        val currentCentroid = event.calculateCentroid(useCurrent = true)
                                                        if (pressedCount == previousPointerCount) {
                                                            val centerX = size.width * 0.5f
                                                            val centerY = size.height * 0.5f
                                                            hitCropEdge = applyCropGestureInto(
                                                                state = transformState,
                                                                sourceWidth = loaded.sourceWidth,
                                                                sourceHeight = loaded.sourceHeight,
                                                                viewportSizePx = cropViewportPx,
                                                                focalStartX = previousCentroid.x - centerX,
                                                                focalStartY = previousCentroid.y - centerY,
                                                                focalEndX = currentCentroid.x - centerX,
                                                                focalEndY = currentCentroid.y - centerY,
                                                                zoomChange = event.calculateZoom(),
                                                                rotationChangeDegrees = event.calculateRotation(),
                                                                edgeResistancePx = EDGE_RESISTANCE_PX,
                                                            ) || hitCropEdge
                                                            if (pressedCount == 1 && !hadMultiplePointers) {
                                                                velocityTracker.addPosition(eventTime, currentCentroid)
                                                            }
                                                        }
                                                        previousPointerCount = pressedCount
                                                    }

                                                    for (change in event.changes) {
                                                        if (change.positionChanged()) change.consume()
                                                    }
                                                } while (pressedCount > 0)

                                                if (hitCropEdge) {
                                                    animateTransformTo(
                                                        requestedTarget = settleCropTransformToNearestEdge(
                                                            sourceWidth = loaded.sourceWidth,
                                                            sourceHeight = loaded.sourceHeight,
                                                            viewportSizePx = cropViewportPx,
                                                            current = transformState.snapshot(),
                                                        ),
                                                        durationMillis = EDGE_SETTLE_ANIMATION_MS,
                                                    )
                                                } else if (!hadMultiplePointers) {
                                                    val velocity = velocityTracker.calculateVelocity()
                                                    if (velocity.x * velocity.x + velocity.y * velocity.y > MIN_FLING_SPEED_SQUARED) {
                                                        val job = scope.launch {
                                                            flingCropTransform(
                                                                state = transformState,
                                                                sourceWidth = loaded.sourceWidth,
                                                                sourceHeight = loaded.sourceHeight,
                                                                viewportSizePx = cropViewportPx,
                                                                velocityX = velocity.x,
                                                                velocityY = velocity.y,
                                                            )
                                                        }
                                                        animationJob.set(job)
                                                    }
                                                }
                                            }
                                        }
                                    },
                            ) {
                                if (previewImage != null && activePreview != null) {
                                    Image(
                                        bitmap = previewImage,
                                        contentDescription = "Artwork under square crop frame",
                                        modifier = Modifier.fillMaxSize().graphicsLayer {
                                            // Reading this revision invalidates only the layer, not the composable/layout.
                                            transformState.graphicsRevision
                                            scaleX = displayScale * transformState.scale
                                            scaleY = displayScale * transformState.scale
                                            rotationZ = transformState.rotationDegrees
                                            translationX = transformState.offsetX
                                            translationY = transformState.offsetY
                                        },
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                    )
                                } else if (loading) {
                                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                                } else {
                                    Text(
                                        "Image preview unavailable",
                                        modifier = Modifier.align(Alignment.Center),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                val cropWindow = maxWidth * CROP_WINDOW_FRACTION
                                val dimBand = (maxWidth - cropWindow) * 0.5f
                                val scrim = Color.Black.copy(alpha = 0.58f)
                                Box(
                                    Modifier.align(Alignment.TopCenter).fillMaxWidth().height(dimBand).background(scrim),
                                )
                                Box(
                                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(dimBand).background(scrim),
                                )
                                Box(
                                    Modifier.align(Alignment.CenterStart).width(dimBand).fillMaxHeight().background(scrim),
                                )
                                Box(
                                    Modifier.align(Alignment.CenterEnd).width(dimBand).fillMaxHeight().background(scrim),
                                )
                                androidx.compose.foundation.Canvas(
                                    modifier = Modifier.size(cropWindow).align(Alignment.Center),
                                ) {
                                    val thirdX = size.width / 3f
                                    val thirdY = size.height / 3f
                                    val gridColor = Color.White.copy(alpha = 0.48f)
                                    drawLine(gridColor, androidx.compose.ui.geometry.Offset(thirdX, 0f), androidx.compose.ui.geometry.Offset(thirdX, size.height), 1f)
                                    drawLine(gridColor, androidx.compose.ui.geometry.Offset(thirdX * 2f, 0f), androidx.compose.ui.geometry.Offset(thirdX * 2f, size.height), 1f)
                                    drawLine(gridColor, androidx.compose.ui.geometry.Offset(0f, thirdY), androidx.compose.ui.geometry.Offset(size.width, thirdY), 1f)
                                    drawLine(gridColor, androidx.compose.ui.geometry.Offset(0f, thirdY * 2f), androidx.compose.ui.geometry.Offset(size.width, thirdY * 2f), 1f)
                                }
                                Box(
                                    Modifier.size(cropWindow).align(Alignment.Center)
                                        .border(1.5.dp, Color.White.copy(alpha = 0.94f), RoundedCornerShape(3.dp)),
                                )
                            }

                            Text(
                                "The shaded area is outside the saved square.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium,
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                OutlinedButton(
                                    modifier = Modifier.weight(1f),
                                    enabled = activePreview != null && !saving,
                                    onClick = {
                                        val current = transformState.snapshot()
                                        val target = clampCropTransform(
                                            sourceWidth = sourceWidth,
                                            sourceHeight = sourceHeight,
                                            viewportSizePx = cropViewportPx,
                                            requested = current.copy(rotationDegrees = current.rotationDegrees - 90f),
                                        )
                                        animateTransformTo(target)
                                    },
                                ) { Text("Rotate left") }
                                OutlinedButton(
                                    modifier = Modifier.weight(1f),
                                    enabled = activePreview != null && !saving,
                                    onClick = {
                                        val current = transformState.snapshot()
                                        val target = clampCropTransform(
                                            sourceWidth = sourceWidth,
                                            sourceHeight = sourceHeight,
                                            viewportSizePx = cropViewportPx,
                                            requested = current.copy(rotationDegrees = current.rotationDegrees + 90f),
                                        )
                                        animateTransformTo(target)
                                    },
                                ) { Text("Rotate right") }
                            }

                            TextButton(
                                onClick = animateReset@{
                                    if (activePreview == null) return@animateReset
                                    animateTransformTo(CropTransform(), shortestRotation = true)
                                },
                                enabled = activePreview != null && !saving,
                                modifier = Modifier.align(Alignment.End),
                            ) { Text("Reset crop") }

                            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancel") }
                            Button(
                                onClick = {
                                    val loaded = activePreview ?: return@Button
                                    val transform = transformState.snapshot()
                                    saving = true
                                    error = null
                                    scope.launch {
                                        try {
                                            val savedUri = withContext(Dispatchers.IO) {
                                                saveEditedArtwork(context, sourceUri, transform, cropViewportPx)
                                            }
                                            saving = false
                                            onArtworkReady(savedUri)
                                        } catch (e: CancellationException) {
                                            throw e
                                        } catch (e: Exception) {
                                            saving = false
                                            error = e.message ?: "Could not save this artwork."
                                        }
                                    }
                                },
                                enabled = activePreview != null && !saving,
                            ) {
                                if (saving) {
                                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Use artwork")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun flingCropTransform(
    state: CropTransformState,
    sourceWidth: Int,
    sourceHeight: Int,
    viewportSizePx: Float,
    velocityX: Float,
    velocityY: Float,
) {
    var speedX = velocityX.coerceIn(-MAX_FLING_SPEED, MAX_FLING_SPEED) * FLING_DAMPING
    var speedY = velocityY.coerceIn(-MAX_FLING_SPEED, MAX_FLING_SPEED) * FLING_DAMPING
    var previousFrameNanos = 0L
    while (speedX * speedX + speedY * speedY > MIN_FLING_SPEED_SQUARED) {
        val frameNanos = withFrameNanos { it }
        if (previousFrameNanos == 0L) {
            previousFrameNanos = frameNanos
            continue
        }
        val deltaSeconds = ((frameNanos - previousFrameNanos).toFloat() / NANOS_PER_SECOND)
            .coerceIn(0f, MAX_FLING_FRAME_SECONDS)
        previousFrameNanos = frameNanos
        val oldX = state.offsetX
        val oldY = state.offsetY
        val requestedX = oldX + speedX * deltaSeconds
        val requestedY = oldY + speedY * deltaSeconds
        val rotation = state.rotationDegrees
        clampCropTransformInto(
            state = state,
            sourceWidth = sourceWidth,
            sourceHeight = sourceHeight,
            viewportSizePx = viewportSizePx,
            requestedScale = state.scale,
            requestedOffsetX = requestedX,
            requestedOffsetY = requestedY,
            requestedRotationDegrees = rotation,
        )
        if (abs(state.offsetX - requestedX) > FLING_BOUNDARY_EPSILON_PX) speedX = 0f
        if (abs(state.offsetY - requestedY) > FLING_BOUNDARY_EPSILON_PX) speedY = 0f
        val friction = exp(-FLING_FRICTION_PER_SECOND * deltaSeconds)
        speedX *= friction
        speedY *= friction
    }
}

private fun shortestAngleDelta(from: Float, to: Float): Float {
    var delta = (to - from) % 360f
    if (delta > 180f) delta -= 360f
    if (delta < -180f) delta += 360f
    return delta
}

private fun lerp(start: Float, end: Float, fraction: Float): Float = start + (end - start) * fraction

private data class ArtworkPreview(
    val bitmap: Bitmap,
    val sourceWidth: Int,
    val sourceHeight: Int,
)

private fun decodeArtworkPreview(context: Context, uri: Uri, targetEdgePx: Int): ArtworkPreview? {
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    val boundsStream = resolver.openInputStream(uri) ?: return null
    boundsStream.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (max(bounds.outWidth, bounds.outHeight) / sampleSize > targetEdgePx) sampleSize *= 2
    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) } ?: return null
    return ArtworkPreview(bitmap, bounds.outWidth, bounds.outHeight)
}

/** Decode full resolution only for export; the editor retains only its viewport-sized preview. */
private fun decodeFullResolution(context: Context, uri: Uri): Bitmap {
    val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
    return context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, options)
    } ?: error("This image could not be decoded at full resolution.")
}

/** Render the transformed source directly to a 1024-square output without using the preview bitmap. */
internal fun exportSquareArtworkBitmap(
    source: Bitmap,
    transform: CropTransform,
    viewportSizePx: Float,
    outputEdge: Int = ARTWORK_EXPORT_EDGE,
): Bitmap {
    require(source.width > 0 && source.height > 0) { "Source image is empty." }
    require(viewportSizePx > 0f && viewportSizePx.isFinite()) { "Crop viewport is invalid." }
    require(outputEdge > 0) { "Output size must be positive." }

    val output = Bitmap.createBitmap(outputEdge, outputEdge, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    canvas.drawColor(android.graphics.Color.BLACK)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
    val outputPerViewportPx = outputEdge / viewportSizePx
    val sourceToOutputScale = outputEdge / min(source.width, source.height).toFloat() * transform.scale

    canvas.translate(
        outputEdge * 0.5f + transform.offsetX * outputPerViewportPx,
        outputEdge * 0.5f + transform.offsetY * outputPerViewportPx,
    )
    canvas.rotate(transform.rotationDegrees)
    canvas.scale(sourceToOutputScale, sourceToOutputScale)
    canvas.drawBitmap(source, -source.width * 0.5f, -source.height * 0.5f, paint)
    return output
}

private fun saveEditedArtwork(
    context: Context,
    sourceUri: Uri,
    transform: CropTransform,
    viewportSizePx: Float,
): Uri {
    val source = decodeFullResolution(context, sourceUri)
    val processed = try {
        exportSquareArtworkBitmap(source, transform, viewportSizePx)
    } finally {
        source.recycle()
    }
    val directory = File(context.cacheDir, "artwork-editor").apply { check(mkdirs() || isDirectory) }
    val output = File(directory, "cover_${System.currentTimeMillis()}.jpg")
    val temporary = File(directory, "${output.name}.tmp")
    try {
        FileOutputStream(temporary).use { stream ->
            check(processed.compress(Bitmap.CompressFormat.JPEG, 92, stream)) { "Could not encode artwork." }
            stream.fd.sync()
        }
        check(temporary.renameTo(output)) { "Could not finish saving artwork." }
        return Uri.fromFile(output)
    } finally {
        processed.recycle()
        temporary.delete()
    }
}

private const val CROP_WINDOW_FRACTION = 0.9f
private const val MAX_PREVIEW_EDGE = 1024
private const val ARTWORK_EXPORT_EDGE = 1024
private const val EDGE_RESISTANCE_PX = 5f
private const val TRANSFORM_ANIMATION_MS = 240
private const val EDGE_SETTLE_ANIMATION_MS = 170
private const val MIN_FLING_SPEED_SQUARED = 8_000f
private const val MAX_FLING_SPEED = 3_600f
private const val FLING_DAMPING = 0.35f
private const val FLING_FRICTION_PER_SECOND = 5.2f
private const val FLING_BOUNDARY_EPSILON_PX = 0.5f
private const val MAX_FLING_FRAME_SECONDS = 0.032f
private const val NANOS_PER_SECOND = 1_000_000_000f
