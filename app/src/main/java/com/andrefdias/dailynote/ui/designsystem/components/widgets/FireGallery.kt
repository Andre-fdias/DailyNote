package com.andrefdias.dailynote.ui.designsystem.components.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.andrefdias.dailynote.ui.designsystem.colors.FireColors
import com.andrefdias.dailynote.ui.designsystem.icons.FireIcons
import com.andrefdias.dailynote.ui.designsystem.shapes.FireShapes
import com.andrefdias.dailynote.ui.designsystem.typography.FireTypography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class GalleryImage(
    val id: String,
    val path: String,
    val title: String,
    val category: String, // "Documento", "Veículo", "Evidência", "Anexo"
    val date: String,
    val origin: String
)

@Composable
fun LocalImage(
    filePath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    isThumbnail: Boolean = false
) {
    val context = LocalContext.current
    var bitmap by remember(filePath) { mutableStateOf<Bitmap?>(null) }
    var hasError by remember(filePath) { mutableStateOf(false) }

    LaunchedEffect(filePath) {
        withContext(Dispatchers.IO) {
            try {
                val resolvedBitmap = if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
                    try {
                        val connection = java.net.URL(filePath).openConnection() as java.net.HttpURLConnection
                        connection.connectTimeout = 5000
                        connection.readTimeout = 5000
                        connection.inputStream.use { stream ->
                            if (isThumbnail) {
                                val bytes = stream.readBytes()
                                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                                options.inSampleSize = calculateInSampleSize(options, 200, 200)
                                options.inJustDecodeBounds = false
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                            } else {
                                BitmapFactory.decodeStream(stream)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                } else if (filePath.startsWith("content://") || filePath.startsWith("file://")) {
                    val uri = Uri.parse(filePath)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        if (isThumbnail) {
                            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                            BitmapFactory.decodeStream(stream, null, options)
                            options.inSampleSize = calculateInSampleSize(options, 200, 200)
                            options.inJustDecodeBounds = false
                            
                            // Re-open stream since it was read
                            context.contentResolver.openInputStream(uri)?.use { s2 ->
                                BitmapFactory.decodeStream(s2, null, options)
                            }
                        } else {
                            BitmapFactory.decodeStream(stream)
                        }
                    }
                } else {
                    val file = File(filePath)
                    if (file.exists()) {
                        if (isThumbnail) {
                            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                            BitmapFactory.decodeFile(file.absolutePath, options)
                            options.inSampleSize = calculateInSampleSize(options, 200, 200)
                            options.inJustDecodeBounds = false
                            BitmapFactory.decodeFile(file.absolutePath, options)
                        } else {
                            BitmapFactory.decodeFile(file.absolutePath)
                        }
                    } else {
                        null
                    }
                }
                if (resolvedBitmap != null) {
                    bitmap = resolvedBitmap
                } else {
                    hasError = true
                }
            } catch (e: Exception) {
                hasError = true
                e.printStackTrace()
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(
            modifier = modifier.background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (hasError) FireIcons.Close else FireIcons.PhotoCamera,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.outHeight to options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

@Composable
fun FireGalleryCard(
    title: String,
    category: String,
    images: List<GalleryImage>,
    onImageClick: (GalleryImage) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = FireColors.Surface),
        shape = FireShapes.Medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = FireTypography.Title,
                color = FireColors.Primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val filtered = images.filter { it.category == category }
            if (filtered.isEmpty()) {
                Text(
                    text = "Nenhuma imagem cadastrada nesta categoria.",
                    style = FireTypography.BodyMedium,
                    color = Color.Gray
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(80.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    items(filtered, key = { it.id }) { img ->
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(FireShapes.Small)
                                .border(1.dp, Color.LightGray, FireShapes.Small)
                                .clickable { onImageClick(img) }
                        ) {
                            LocalImage(
                                filePath = img.path,
                                contentDescription = img.title,
                                modifier = Modifier.fillMaxSize(),
                                isThumbnail = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ImageViewerDialog(
    initialImageId: String,
    imagesList: List<GalleryImage>,
    onDismiss: () -> Unit,
    onDeleteImage: (GalleryImage) -> Unit,
    onShareImage: (GalleryImage) -> Unit,
    onDownloadImage: ((GalleryImage) -> Unit)? = null
) {
    val context = LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current
    DisposableEffect(view) {
        val window = (view.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
        if (window != null) {
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        }
        onDispose {}
    }
    val scope = rememberCoroutineScope()
    val initialIndex = remember(initialImageId, imagesList) {
        imagesList.indexOfFirst { it.id == initialImageId }.coerceAtLeast(0)
    }

    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { imagesList.size }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Scaffold(
            modifier = Modifier.systemBarsPadding(),
            topBar = {
                val currentImage = imagesList.getOrNull(pagerState.currentPage)
                TopAppBar(
                    title = {
                        Text(
                            text = currentImage?.title ?: "Visualizar Imagem",
                            style = FireTypography.Title,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                        }
                    },
                    actions = {
                        currentImage?.let { img ->
                            if (onDownloadImage != null) {
                                IconButton(onClick = { onDownloadImage(img) }) {
                                    Icon(imageVector = FireIcons.FileDownload, contentDescription = "Baixar", tint = Color.White)
                                }
                            }
                            IconButton(onClick = { onShareImage(img) }) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "Compartilhar", tint = Color.White)
                            }
                            IconButton(onClick = {
                                onDeleteImage(img)
                                if (imagesList.size <= 1) {
                                    onDismiss()
                                }
                            }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Excluir", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.8f)
                    )
                )
            },
            containerColor = Color.Black
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val img = imagesList.getOrNull(page)
                    if (img != null) {
                        var scale by remember { mutableStateOf(1f) }
                        var offset by remember { mutableStateOf(Offset.Zero) }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(1f, 5f)
                                        val maxOffsetX = (size.width * (scale - 1f)) / 2f
                                        val maxOffsetY = (size.height * (scale - 1f)) / 2f
                                        offset = Offset(
                                            x = (offset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX),
                                            y = (offset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                                        )
                                    }
                                }
                                .clickable(enabled = scale > 1f) {
                                    // Reset scale on double click simulator
                                    scale = 1f
                                    offset = Offset.Zero
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            LocalImage(
                                filePath = img.path,
                                contentDescription = img.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.8f)
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offset.x,
                                        translationY = offset.y
                                    ),
                                contentScale = ContentScale.Fit
                            )

                            // Metadata info display card at bottom
                            Card(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.8f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Categoria: ${img.category}", style = FireTypography.BodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("Origem: ${img.origin}", style = FireTypography.BodyMedium, color = Color.LightGray)
                                    Text("Data: ${img.date}", style = FireTypography.Caption, color = Color.LightGray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
