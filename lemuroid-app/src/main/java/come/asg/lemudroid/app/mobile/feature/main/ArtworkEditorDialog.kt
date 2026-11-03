package come.asg.lemudroid.app.mobile.feature.main

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

@Composable
internal fun ArtworkEditorDialog(
    sourceUri: Uri,
    onDismiss: () -> Unit,
    onArtworkReady: (Uri) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sourceBitmap by remember(sourceUri) { mutableStateOf<Bitmap?>(null) }
    var previewBitmap by remember(sourceUri) { mutableStateOf<Bitmap?>(null) }
    var rotation by remember(sourceUri) { mutableStateOf(0) }
    var cropZoom by remember(sourceUri) { mutableStateOf(1f) }
    var loading by remember(sourceUri) { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember(sourceUri) { mutableStateOf<String?>(null) }

    LaunchedEffect(sourceUri) {
        loading = true
        sourceBitmap = try {
            withContext(Dispatchers.IO) { decodeSampledBitmap(context, sourceUri) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        loading = false
        error = if (sourceBitmap == null) "This image could not be opened. Choose a different image." else null
    }

    LaunchedEffect(sourceBitmap, rotation, cropZoom) {
        val source = sourceBitmap ?: return@LaunchedEffect
        val nextPreview = withContext(Dispatchers.Default) { makeSquareCover(source, rotation, cropZoom) }
        previewBitmap?.takeIf { it !== nextPreview }?.recycle()
        previewBitmap = nextPreview
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
                modifier = Modifier.fillMaxWidth(0.92f).heightIn(max = maxHeight * 0.9f),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                Text("Edit artwork", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Text("Crop a square cover, zoom to frame it, then rotate if needed.", color = MaterialTheme.colorScheme.onSurfaceVariant)

                Box(
                    modifier = Modifier.size(220.dp).align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        loading -> CircularProgressIndicator()
                        previewBitmap != null -> Image(
                            bitmap = previewBitmap!!.asImageBitmap(),
                            contentDescription = "Artwork crop preview",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Fit,
                        )
                        else -> Text("Image preview unavailable", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Text("Crop / zoom", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Slider(
                    value = cropZoom,
                    onValueChange = { cropZoom = it },
                    valueRange = 1f..2.4f,
                    steps = 5,
                    enabled = sourceBitmap != null && !saving,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = sourceBitmap != null && !saving,
                        onClick = { rotation = (rotation - 90 + 360) % 360 },
                    ) { Text("Rotate left") }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = sourceBitmap != null && !saving,
                        onClick = { rotation = (rotation + 90) % 360 },
                    ) { Text("Rotate right") }
                }
                TextButton(
                    onClick = { rotation = 0; cropZoom = 1f },
                    enabled = !saving && (rotation != 0 || cropZoom != 1f),
                    modifier = Modifier.align(Alignment.End),
                ) { Text("Reset crop") }

                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                    }
                    Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancel") }
                    Button(
                        onClick = {
                            val source = sourceBitmap ?: return@Button
                            saving = true
                            error = null
                            scope.launch {
                                try {
                                    val savedUri = withContext(Dispatchers.IO) { saveEditedArtwork(context, source, rotation, cropZoom) }
                                    saving = false
                                    onArtworkReady(savedUri)
                                } catch (e: Exception) {
                                    saving = false
                                    error = e.message ?: "Could not save this artwork."
                                }
                            }
                        },
                        enabled = previewBitmap != null && !saving,
                    ) {
                        if (saving) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) else Text("Use artwork")
                    }
                }
            }
        }
    }
}
}

private fun decodeSampledBitmap(context: Context, uri: Uri): Bitmap? {
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    val boundsStream = resolver.openInputStream(uri) ?: return null
    boundsStream.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (max(bounds.outWidth, bounds.outHeight) / sampleSize > MAX_DECODE_EDGE) sampleSize *= 2
    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
}

private fun makeSquareCover(source: Bitmap, rotation: Int, zoom: Float): Bitmap {
    val rotated =
        if (rotation % 360 == 0) {
            source
        } else {
            Bitmap.createBitmap(source, 0, 0, source.width, source.height, Matrix().apply { postRotate(rotation.toFloat()) }, true)
        }

    val side = (min(rotated.width, rotated.height) / zoom.coerceIn(1f, 2.4f)).toInt().coerceAtLeast(1)
    val left = (rotated.width - side) / 2
    val top = (rotated.height - side) / 2
    val square = Bitmap.createBitmap(rotated, left, top, side, side)
    val output =
        if (square.width == OUTPUT_EDGE) square else Bitmap.createScaledBitmap(square, OUTPUT_EDGE, OUTPUT_EDGE, true)

    if (rotated !== source && rotated !== square && rotated !== output) rotated.recycle()
    if (square !== source && square !== rotated && square !== output) square.recycle()
    return output
}

private fun saveEditedArtwork(context: Context, source: Bitmap, rotation: Int, zoom: Float): Uri {
    val processed = makeSquareCover(source, rotation, zoom)
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

private const val MAX_DECODE_EDGE = 2048
private const val OUTPUT_EDGE = 512
