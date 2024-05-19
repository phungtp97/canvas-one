package com.joshta.canvasone.ui.canvas

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
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.BottomNavigation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.BottomAppBar
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.joshta.canvasone.R
import com.joshta.canvasone.ui.composable.CurveBottomNav
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
fun CanvasScreen() {
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
        bottomBar = {
            BottomAppBar(
                modifier = Modifier
                    .fillMaxWidth(1F)
                    .height(118.dp)
                    .background(color = AppTheme.color.darkBackground),
            ) {
                BottomNavigation(
                    modifier = Modifier
                        .fillMaxSize(1F)
                        .clip(CurveBottomNav())
                ) {}
            }
        }
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

    BoxWithConstraints {
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height
        val pointNodes = remember { mutableStateListOf<PointNode>() }

        val tempPoints = remember {
            mutableStateListOf<Offset>()
        }
        val bitmapFitHeight = (maxWidth / maxHeight) > (bitmapWidth / bitmapHeight)
        val contentHeight =
            if (bitmapFitHeight) this@BoxWithConstraints.maxHeight else (this@BoxWithConstraints.maxWidth / (bitmapWidth.toFloat() / bitmapHeight.toFloat()));
        val contentWidth =
            if (bitmapFitHeight) (contentHeight / (bitmapHeight.toFloat() / bitmapWidth.toFloat())) else this@BoxWithConstraints.maxWidth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Gray)
        ) {

            Canvas(modifier = Modifier
                .width(contentWidth)
                .height(contentHeight)
                .background(Color.Transparent)
                .align(Alignment.Center)
                .border(4.dp, Color.Blue)
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            tempPoints.add(startOffset)
                        },
                        onDrag = { change, dragAmount ->
                            Log.e("onDrag", "onDrag: ${change.position}")
                            tempPoints.add(change.position)// Add the current point to the list
                            change.consume()
                        },
                        onDragEnd = {
                            pointNodes.add(
                                PointNode(
                                    pointNodes.size,
                                    mutableListOf<Offset>().apply { addAll(tempPoints) })
                            )
                            tempPoints.clear();
                        },
                        onDragCancel = {
                            tempPoints.clear();
                        }
                    )
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
                // Get the width and height of the canvas
                val canvasWidth = size.width.toInt()
                val canvasHeight = size.height.toInt()
                // Check if the image should be fitted by height
                val fitHeight = (canvasWidth / canvasHeight) > (bitmapWidth / bitmapHeight)

                // Draw the image on the canvas based on the scale condition
                if (fitHeight) {
                    // If the condition is true, scale the image to fit the width of the canvas
                    val scaledHeight =
                        (canvasWidth * (bitmapHeight.toFloat() / bitmapWidth.toFloat())).toInt()
                    val topOffset = (canvasHeight - scaledHeight) / 2

                    drawImage(
                        bitmap,
                        dstOffset = IntOffset(0, topOffset),
                        dstSize = IntSize(
                            canvasWidth,
                            (canvasWidth * (bitmapHeight.toFloat() / bitmapWidth.toFloat())).toInt()
                        )
                    )
                } else {
                    // If the condition is false, scale the image to fit the height of the canvas
                    val scaledWidth =
                        (canvasHeight * (bitmapWidth.toFloat() / bitmapHeight.toFloat())).toInt()
                    val leftOffset = (canvasWidth - scaledWidth) / 2
                    drawImage(
                        bitmap,
                        dstOffset = IntOffset(leftOffset, 0),
                        dstSize = IntSize(
                            (canvasHeight * (bitmapWidth.toFloat() / bitmapHeight.toFloat())).toInt(),
                            canvasHeight
                        )
                    )
                }

                val temp = pointNodes.toMutableList()

                if (tempPoints.isNotEmpty()) {
                    temp.add(PointNode(-1, tempPoints))
                }

                for (node in temp) {
                    var lastPoint: Offset? = null

                    val task: (i: Int, path: Path) -> Unit = { i, path ->
                        val newPoint: Offset = node.points[i]
                        if (lastPoint == null) {
                            path.moveTo(newPoint.x, newPoint.y)
                        } else {
                            val midPoint = calculateMidPoint(lastPoint!!, newPoint)
                            path.quadraticBezierTo(
                                lastPoint!!.x, lastPoint!!.y,
                                midPoint.x, midPoint.y
                            )
                        }
                        lastPoint = newPoint
                    }
                    val path = Path().apply {
                        var lastPrintedIndex = -1

                        for (i in node.points.indices step 8) {
                            task(i, this)
                        }
                        if (lastPrintedIndex != node.points.lastIndex) {
                            task(node.points.lastIndex, this)
                        }
                    }
                    drawPath(path = path, Color.Black, style = Stroke(width = 10f))
                }
            }
        }
    }
}

private fun calculateMidPoint(p1: Offset, p2: Offset): Offset {
    return Offset((p1.x + p2.x) / 2, (p1.y + p2.y) / 2)
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

data class PointNode(val index: Int, val points: MutableList<Offset>)