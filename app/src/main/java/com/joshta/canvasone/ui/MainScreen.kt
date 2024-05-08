package com.joshta.canvasone.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.Intent.createChooser
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Picture
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.joshta.canvasone.R
import com.joshta.canvasone.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume


@OptIn(ExperimentalPermissionsApi::class)
@Preview
@ExperimentalMaterial3Api
@Composable
fun MainScreen() {
    var uri by remember {
        mutableStateOf<Uri?>(null)
    }
    val context = LocalContext.current

    var maxSelectionCount = 1;

    val coroutineScope = rememberCoroutineScope()

    val picture = remember { Picture() }

    val snackbarHostState = remember { SnackbarHostState() }

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri = it }
    )

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(
            maxItems = if (maxSelectionCount > 1) {
                maxSelectionCount
            } else {
                2
            }
        ),
        onResult = { uri = it.first() }
    )

    fun launchPhotoPicker() {
        if (maxSelectionCount > 1) {
            multiplePhotoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            singlePhotoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    val permissionAccessState = rememberMultiplePermissionsState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // No permissions are needed on Android 10+ to add files in the shared storage
            emptyList()
        } else {
            listOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    )

    LaunchedEffect(key1 = true) {
        if (permissionAccessState.shouldShowRationale) {
            coroutineScope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "The storage permission is needed to save the image",
                    actionLabel = "Grant Access"
                )

                if (result == SnackbarResult.ActionPerformed) {
                    permissionAccessState.launchMultiplePermissionRequest()
                }
            }
        } else {
            permissionAccessState.launchMultiplePermissionRequest()
        }
    }

    fun shareBitmapFromComposable() {
        if (permissionAccessState.allPermissionsGranted) {
            coroutineScope.launch(Dispatchers.IO) {
                val bitmap = createBitmapFromPicture(picture)
                val uri = bitmap.saveToDisk(context)
                shareBitmap(context, uri)
            }
        } else if (permissionAccessState.shouldShowRationale) {
            coroutineScope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "The storage permission is needed to save the image",
                    actionLabel = "Grant Access"
                )

                if (result == SnackbarResult.ActionPerformed) {
                    permissionAccessState.launchMultiplePermissionRequest()
                }
            }
        } else {
            permissionAccessState.launchMultiplePermissionRequest()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(color = AppTheme.color.background),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = AppTheme.typo.h1.copy(color = AppTheme.color.surface)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.color.darkBackground),
                actions = {
                    if (permissionAccessState.allPermissionsGranted) {
                        SaveImageIcon {
                            shareBitmapFromComposable()
                        }
                        AddImageIcon(onClick = {
                            launchPhotoPicker()
                        })
                    }
                }
            )
        },
    ) { paddingValues ->
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uri != null) {
                val mBitmap: Bitmap
                if (Build.VERSION.SDK_INT < 28) {
                    mBitmap = MediaStore.Images
                        .Media.getBitmap(context.contentResolver, uri)

                } else {
                    val source = ImageDecoder
                        .createSource(context.contentResolver, uri!!)
                    mBitmap = ImageDecoder.decodeBitmap(source)
                }
                ImageLayoutView(
                    mBitmap.copy(Bitmap.Config.ARGB_8888, true).asImageBitmap(),
                    picture
                )
            }
        }
    }
}

@Composable
fun ImageLayoutView(bitmap: ImageBitmap, picture: Picture) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {
        DrawableCanvas(bitmap, picture = picture)
    }
}

@Composable
fun DrawableCanvas(bitmap: ImageBitmap, picture: Picture) {
    val deviceWidth = LocalContext.current.resources.displayMetrics.widthPixels
    val bitmapWidth = bitmap.width
    val bitmapHeight = bitmap.height
    val points = remember { mutableStateListOf<Offset>() } // List to store points

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height((deviceWidth * (bitmapHeight.toFloat() / bitmapWidth.toFloat())).dp)
        .background(Color.Transparent)
        .border(4.dp, Color.Blue)
        .fillMaxSize()
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { startOffset ->
                    points.add(startOffset) // Add the start point to the list
                },
                onDrag = { change, dragAmount ->
                    points.add(change.position) // Add the current point to the list
                    change.consume()
                })
        }
        .drawWithCache {
            val width = this.size.width.toInt()
            val height = this.size.height.toInt()
            onDrawWithContent {
                val pictureCanvas = Canvas(
                    picture.beginRecording(
                        width,
                        height
                    )
                )
                draw(this, this.layoutDirection, pictureCanvas, this.size) {
                    this@onDrawWithContent.drawContent()
                }
                picture.endRecording()

                drawIntoCanvas { canvas -> canvas.nativeCanvas.drawPicture(picture) }
            }
        }) {
        val path = Path().apply {
            var first = true

            for (i in points.indices step 2) {
                val point: Offset = points[i]
                if (first) {
                    first = false
                    moveTo(point.x, point.y)
                } else if (i < points.size - 1) {
                    val next: Offset = points[i + 1]
                    quadraticBezierTo(point.x, point.y, next.x, next.y)
                } else {
                    lineTo(point.x, point.y)
                }
            }
        }
        drawImage(bitmap)
        drawPath(path = path, Color.Black, style = Stroke(width = 10f))
    }
}

private fun createBitmapFromPicture(picture: Picture): Bitmap {
    val bitmap = Bitmap.createBitmap(
        picture.width,
        picture.height,
        Bitmap.Config.ARGB_8888
    )

    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    canvas.drawPicture(picture)
    return bitmap
}

private suspend fun Bitmap.saveToDisk(context: Context): Uri {
    val file = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        "screenshot-${System.currentTimeMillis()}.png"
    )

    file.writeBitmap(this, Bitmap.CompressFormat.PNG, 100)

    return scanFilePath(context, file.path) ?: throw Exception("File could not be saved")
}

private suspend fun scanFilePath(context: Context, filePath: String): Uri? {
    return suspendCancellableCoroutine { continuation ->
        MediaScannerConnection.scanFile(
            context,
            arrayOf(filePath),
            arrayOf("image/png")
        ) { _, scannedUri ->
            if (scannedUri == null) {
                continuation.cancel(Exception("File $filePath could not be scanned"))
            } else {
                continuation.resume(scannedUri)
            }
        }
    }
}

private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
    outputStream().use { out ->
        bitmap.compress(format, quality, out)
        out.flush()
    }
}

@Composable
fun AddImageIcon(onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.padding(8.dp)) {
        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
    }
}

@Composable
fun SaveImageIcon(onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.padding(8.dp)) {
        Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
    }
}

private fun shareBitmap(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(context, createChooser(intent, "Share your image"), null)
}

data class PointNode(val index: Int, val points: List<Offset>)