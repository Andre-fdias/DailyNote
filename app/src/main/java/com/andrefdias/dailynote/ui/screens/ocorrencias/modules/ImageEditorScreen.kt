package com.andrefdias.dailynote.ui.screens.ocorrencias.modules

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.io.File
import java.io.FileOutputStream

class DrawingPath(val points: List<Offset>, val color: Color = Color.Red)
class TextDrawing(val text: String, val position: Offset, val color: Color = Color.Yellow)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageEditorScreen(
    imageUri: Uri,
    onSave: (Uri, Boolean) -> Unit, // Boolean: isNewImage
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var editedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    val allPaths = remember { mutableStateListOf<DrawingPath>() }
    val allTexts = remember { mutableStateListOf<TextDrawing>() }
    val currentPath = remember { mutableStateListOf<Offset>() }

    var isDrawingMode by remember { mutableStateOf(false) }
    var isTextMode by remember { mutableStateOf(false) }
    var textToDraw by remember { mutableStateOf("") }
    var showTextInput by remember { mutableStateOf(false) }
    var triggerUpdate by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    
    // WhatsApp Style Crop States
    var isCroppingMode by remember { mutableStateOf(false) }
    var cropLeft by remember { mutableStateOf(0f) }
    var cropTop by remember { mutableStateOf(0f) }
    var cropRight by remember { mutableStateOf(0f) }
    var cropBottom by remember { mutableStateOf(0f) }
    var cropInitialized by remember { mutableStateOf(false) }
    var activeHandle by remember { mutableStateOf<String?>(null) }

    var canvasSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize(0, 0)) }

    LaunchedEffect(imageUri) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bmp = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bmp != null) {
                editedBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao carregar imagem.", Toast.LENGTH_SHORT).show()
            onCancel()
        }
    }

    // Combine paths and text into final bitmap
    val finalBitmapToSave = remember(editedBitmap, allPaths, allTexts, canvasSize) {
        editedBitmap?.let { bmp ->
            if (canvasSize.width > 0 && canvasSize.height > 0) {
                val mutable = bmp.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = android.graphics.Canvas(mutable)
                val scaleX = mutable.width.toFloat() / canvasSize.width
                val scaleY = mutable.height.toFloat() / canvasSize.height
                
                // Draw paths
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.RED
                    strokeWidth = 12f
                    style = android.graphics.Paint.Style.STROKE
                    strokeJoin = android.graphics.Paint.Join.ROUND
                    strokeCap = android.graphics.Paint.Cap.ROUND
                    isAntiAlias = true
                }
                allPaths.forEach { path ->
                    if (path.points.size > 1) {
                        val gPath = android.graphics.Path()
                        val first = path.points.first()
                        gPath.moveTo(first.x * scaleX, first.y * scaleY)
                        for (i in 1 until path.points.size) {
                            val pt = path.points[i]
                            gPath.lineTo(pt.x * scaleX, pt.y * scaleY)
                        }
                        canvas.drawPath(gPath, paint)
                    }
                }
                
                // Draw text
                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.YELLOW
                    textSize = 45f * scaleX
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                }
                allTexts.forEach { textDrawing ->
                    canvas.drawText(
                        textDrawing.text,
                        textDrawing.position.x * scaleX,
                        textDrawing.position.y * scaleY,
                        textPaint
                    )
                }
                mutable
            } else {
                bmp
            }
        }
    }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding() // Use standard padding to avoid system UI overlap
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onCancel) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Cancelar", tint = Color.White)
                    }
                    Text(
                        text = if (isCroppingMode) "Recortar Imagem" else "Edição de Imagem",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { showSaveDialog = true },
                        enabled = editedBitmap != null && !isCroppingMode
                    ) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = "Salvar", tint = if (editedBitmap != null && !isCroppingMode) Color.Green else Color.Gray)
                    }
                }
                
                // Canvas Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    editedBitmap?.let { bmp ->
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .aspectRatio(bmp.width.toFloat() / bmp.height.toFloat())
                                .background(Color.DarkGray)
                                .pointerInput(isDrawingMode, isCroppingMode) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            if (isDrawingMode) {
                                                currentPath.clear()
                                                currentPath.add(offset)
                                            } else if (isCroppingMode) {
                                                val radius = 55f
                                                activeHandle = when {
                                                    (offset - Offset(cropLeft, cropTop)).getDistance() < radius -> "TL"
                                                    (offset - Offset(cropRight, cropTop)).getDistance() < radius -> "TR"
                                                    (offset - Offset(cropLeft, cropBottom)).getDistance() < radius -> "BL"
                                                    (offset - Offset(cropRight, cropBottom)).getDistance() < radius -> "BR"
                                                    offset.x > cropLeft && offset.x < cropRight && offset.y > cropTop && offset.y < cropBottom -> "MOVE"
                                                    else -> null
                                                }
                                            }
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            if (isDrawingMode) {
                                                currentPath.add(change.position)
                                                triggerUpdate = !triggerUpdate
                                            } else if (isCroppingMode && activeHandle != null) {
                                                val delta = dragAmount
                                                val w = size.width.toFloat()
                                                val h = size.height.toFloat()
                                                when (activeHandle) {
                                                    "TL" -> {
                                                        cropLeft = (cropLeft + delta.x).coerceIn(0f, cropRight - 100f)
                                                        cropTop = (cropTop + delta.y).coerceIn(0f, cropBottom - 100f)
                                                    }
                                                    "TR" -> {
                                                        cropRight = (cropRight + delta.x).coerceIn(cropLeft + 100f, w)
                                                        cropTop = (cropTop + delta.y).coerceIn(0f, cropBottom - 100f)
                                                    }
                                                    "BL" -> {
                                                        cropLeft = (cropLeft + delta.x).coerceIn(0f, cropRight - 100f)
                                                        cropBottom = (cropBottom + delta.y).coerceIn(cropTop + 100f, h)
                                                    }
                                                    "BR" -> {
                                                        cropRight = (cropRight + delta.x).coerceIn(cropLeft + 100f, w)
                                                        cropBottom = (cropBottom + delta.y).coerceIn(cropTop + 100f, h)
                                                    }
                                                    "MOVE" -> {
                                                        val rectW = cropRight - cropLeft
                                                        val rectH = cropBottom - cropTop
                                                        val newLeft = (cropLeft + delta.x).coerceIn(0f, w - rectW)
                                                        val newTop = (cropTop + delta.y).coerceIn(0f, h - rectH)
                                                        cropLeft = newLeft
                                                        cropRight = newLeft + rectW
                                                        cropTop = newTop
                                                        cropBottom = newTop + rectH
                                                    }
                                                }
                                                triggerUpdate = !triggerUpdate
                                            }
                                        },
                                        onDragEnd = {
                                            if (isDrawingMode && currentPath.isNotEmpty()) {
                                                allPaths.add(DrawingPath(currentPath.toList()))
                                                currentPath.clear()
                                                triggerUpdate = !triggerUpdate
                                            }
                                            activeHandle = null
                                        }
                                    )
                                }
                                .pointerInput(isTextMode, textToDraw) {
                                    if (isTextMode && textToDraw.isNotEmpty()) {
                                        detectTapGestures { offset ->
                                            allTexts.add(TextDrawing(textToDraw, offset))
                                            isTextMode = false
                                            textToDraw = ""
                                            triggerUpdate = !triggerUpdate
                                        }
                                    }
                                }
                        ) {
                            canvasSize = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt())
                            
                            if (!cropInitialized && size.width > 0f) {
                                cropLeft = size.width * 0.1f
                                cropTop = size.height * 0.1f
                                cropRight = size.width * 0.9f
                                cropBottom = size.height * 0.9f
                                cropInitialized = true
                            }
                            
                            drawContext.canvas.nativeCanvas.drawBitmap(
                                bmp,
                                null,
                                android.graphics.RectF(0f, 0f, size.width, size.height),
                                null
                            )
                            
                            allPaths.forEach { path ->
                                if (path.points.size > 1) {
                                    for (i in 0 until path.points.size - 1) {
                                        drawLine(
                                            color = path.color,
                                            start = path.points[i],
                                            end = path.points[i+1],
                                            strokeWidth = 10f
                                        )
                                    }
                                }
                            }
                            
                            if (isDrawingMode && currentPath.size > 1) {
                                val strokeColor = Color.Red
                                for (i in 0 until currentPath.size - 1) {
                                    drawLine(
                                        color = strokeColor,
                                        start = currentPath[i],
                                        end = currentPath[i+1],
                                        strokeWidth = 10f
                                    )
                                }
                            }
                            
                            allTexts.forEach { textDrawing ->
                                drawContext.canvas.nativeCanvas.drawText(
                                    textDrawing.text,
                                    textDrawing.position.x,
                                    textDrawing.position.y,
                                    android.graphics.Paint().apply {
                                        color = android.graphics.Color.YELLOW
                                        textSize = 45f
                                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                                    }
                                )
                            }

                            if (isCroppingMode) {
                                drawRect(color = Color.Black.copy(alpha = 0.7f), topLeft = Offset(0f, 0f), size = Size(size.width, cropTop))
                                drawRect(color = Color.Black.copy(alpha = 0.7f), topLeft = Offset(0f, cropBottom), size = Size(size.width, size.height - cropBottom))
                                drawRect(color = Color.Black.copy(alpha = 0.7f), topLeft = Offset(0f, cropTop), size = Size(cropLeft, cropBottom - cropTop))
                                drawRect(color = Color.Black.copy(alpha = 0.7f), topLeft = Offset(cropRight, cropTop), size = Size(size.width - cropRight, cropBottom - cropTop))

                                drawRect(
                                    color = Color.White,
                                    topLeft = Offset(cropLeft, cropTop),
                                    size = Size(cropRight - cropLeft, cropBottom - cropTop),
                                    style = Stroke(width = 3f)
                                )

                                val hLen = 40f
                                val hThick = 10f
                                drawLine(color = Color.White, start = Offset(cropLeft, cropTop), end = Offset(cropLeft + hLen, cropTop), strokeWidth = hThick)
                                drawLine(color = Color.White, start = Offset(cropLeft, cropTop), end = Offset(cropLeft, cropTop + hLen), strokeWidth = hThick)
                                drawLine(color = Color.White, start = Offset(cropRight, cropTop), end = Offset(cropRight - hLen, cropTop), strokeWidth = hThick)
                                drawLine(color = Color.White, start = Offset(cropRight, cropTop), end = Offset(cropRight, cropTop + hLen), strokeWidth = hThick)
                                drawLine(color = Color.White, start = Offset(cropLeft, cropBottom), end = Offset(cropLeft + hLen, cropBottom), strokeWidth = hThick)
                                drawLine(color = Color.White, start = Offset(cropLeft, cropBottom), end = Offset(cropLeft, cropBottom - hLen), strokeWidth = hThick)
                                drawLine(color = Color.White, start = Offset(cropRight, cropBottom), end = Offset(cropRight - hLen, cropBottom), strokeWidth = hThick)
                                drawLine(color = Color.White, start = Offset(cropRight, cropBottom), end = Offset(cropRight, cropBottom - hLen), strokeWidth = hThick)
                            }
                        }
                    } ?: CircularProgressIndicator(color = Color.White)
                }
                
                // Bottom Tools Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(top = 12.dp, bottom = 72.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCroppingMode) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    editedBitmap?.let { bmp ->
                                        if (canvasSize.width > 0 && canvasSize.height > 0) {
                                            val scaleX = bmp.width.toFloat() / canvasSize.width
                                            val scaleY = bmp.height.toFloat() / canvasSize.height
                                            val x = (cropLeft * scaleX).toInt().coerceIn(0, bmp.width - 1)
                                            val y = (cropTop * scaleY).toInt().coerceIn(0, bmp.height - 1)
                                            val width = ((cropRight - cropLeft) * scaleX).toInt().coerceIn(1, bmp.width - x)
                                            val height = ((cropBottom - cropTop) * scaleY).toInt().coerceIn(1, bmp.height - y)
                                            val cropped = Bitmap.createBitmap(bmp, x, y, width, height)
                                            
                                            editedBitmap = cropped
                                            allPaths.clear()
                                            allTexts.clear()
                                            isCroppingMode = false
                                            cropInitialized = false
                                        }
                                    }
                                }
                                .padding(8.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Check, contentDescription = "Confirmar", tint = Color.Green, modifier = Modifier.size(28.dp))
                            Text("Confirmar", fontSize = 11.sp, color = Color.White)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    isCroppingMode = false
                                }
                                .padding(8.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Cancelar", tint = Color.Red, modifier = Modifier.size(28.dp))
                            Text("Cancelar", fontSize = 11.sp, color = Color.White)
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    isDrawingMode = !isDrawingMode
                                    isTextMode = false
                                }
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Desenhar",
                                tint = if (isDrawingMode) MaterialTheme.colorScheme.primary else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Desenhar",
                                fontSize = 11.sp,
                                color = if (isDrawingMode) MaterialTheme.colorScheme.primary else Color.White
                            )
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    if (allPaths.isNotEmpty() || allTexts.isNotEmpty()) {
                                        if (allPaths.isNotEmpty()) allPaths.removeLast()
                                        else if (allTexts.isNotEmpty()) allTexts.removeLast()
                                        triggerUpdate = !triggerUpdate
                                    }
                                }
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Desfazer",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Text("Desfazer", fontSize = 11.sp, color = Color.White)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    showTextInput = true
                                    isDrawingMode = false
                                }
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Description,
                                contentDescription = "Texto",
                                tint = if (isTextMode) MaterialTheme.colorScheme.primary else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Texto",
                                fontSize = 11.sp,
                                color = if (isTextMode) MaterialTheme.colorScheme.primary else Color.White
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    isCroppingMode = true
                                    isDrawingMode = false
                                    isTextMode = false
                                    cropInitialized = false
                                }
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FileDownload,
                                contentDescription = "Recortar",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Text("Recortar", fontSize = 11.sp, color = Color.White)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    editedBitmap?.let { bmp ->
                                        val matrix = android.graphics.Matrix().apply { postRotate(90f) }
                                        val rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                                        editedBitmap = rotated
                                        allPaths.clear()
                                        allTexts.clear()
                                        triggerUpdate = !triggerUpdate
                                    }
                                }
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Girar",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Text("Girar", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
            
            if (showTextInput) {
                var enteredText by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showTextInput = false },
                    title = { Text("Adicionar Texto") },
                    text = {
                        OutlinedTextField(
                            value = enteredText,
                            onValueChange = { enteredText = it },
                            placeholder = { Text("Digite o texto...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (enteredText.isNotBlank()) {
                                    textToDraw = enteredText
                                    isTextMode = true
                                    showTextInput = false
                                    Toast.makeText(context, "Toque na imagem para fixar o texto.", Toast.LENGTH_LONG).show()
                                }
                            }
                        ) {
                            Text("Confirmar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTextInput = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
            
            if (showSaveDialog) {
                AlertDialog(
                    onDismissRequest = { showSaveDialog = false },
                    title = { Text("Salvar Imagem") },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Deseja substituir a imagem atual ou salvar as edições como uma nova foto?")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    showSaveDialog = false
                                    val finalBmp = finalBitmapToSave ?: editedBitmap
                                    finalBmp?.let {
                                        val file = File(context.filesDir, "edited_${System.currentTimeMillis()}.jpg")
                                        val out = FileOutputStream(file)
                                        it.compress(Bitmap.CompressFormat.JPEG, 90, out)
                                        out.flush()
                                        out.close()
                                        onSave(Uri.fromFile(file), false) // false = Overwrite
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Sobrescrever Atual")
                            }
                            OutlinedButton(
                                onClick = {
                                    showSaveDialog = false
                                    val finalBmp = finalBitmapToSave ?: editedBitmap
                                    finalBmp?.let {
                                        val file = File(context.filesDir, "edited_${System.currentTimeMillis()}.jpg")
                                        val out = FileOutputStream(file)
                                        it.compress(Bitmap.CompressFormat.JPEG, 90, out)
                                        out.flush()
                                        out.close()
                                        onSave(Uri.fromFile(file), true) // true = Save as New
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Salvar como Nova")
                            }
                            TextButton(
                                onClick = { showSaveDialog = false },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Cancelar")
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = null
                )
            }
        }
    }
}
