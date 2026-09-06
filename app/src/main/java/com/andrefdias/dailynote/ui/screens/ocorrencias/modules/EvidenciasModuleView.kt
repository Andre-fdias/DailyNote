package com.andrefdias.dailynote.ui.screens.ocorrencias.modules

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EvidenciasModuleView(
    ocorrencia: RoomNovaOcorrencia,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    
    val initialFotos = remember {
        try {
            val type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson<List<String>>(ocorrencia.fotosUrisJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    val fotosList = remember { mutableStateListOf(*initialFotos.toTypedArray()) }
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var editingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var viewingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    
    // Configurar launcher da câmera
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                fotosList.add(uri.toString())
            }
        }
    }
    
    // Configurar launcher da galeria
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { sourceUri ->
            // Copiar a imagem da galeria para o armazenamento interno (FilesDir)
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
                if (inputStream != null) {
                    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val imageFile = File(context.filesDir, "evidencia_galeria_${timeStamp}.jpg")
                    val outputStream = FileOutputStream(imageFile)
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                    
                    // Adicionar URI do arquivo interno
                    fotosList.add(Uri.fromFile(imageFile).toString())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (editingPhotoUri != null) {
        ImageEditorScreen(
            imageUri = editingPhotoUri!!,
            onSave = { newUri, isNewImage ->
                if (isNewImage) {
                    fotosList.add(newUri.toString())
                } else {
                    fotosList.remove(editingPhotoUri.toString())
                    fotosList.add(newUri.toString())
                }
                editingPhotoUri = null
            },
            onCancel = { editingPhotoUri = null }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Evidências Fotográficas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }

                Text(
                    "Adicione fotos capturadas pela câmera ou importe da galeria. Imagens importadas serão copiadas para o modo seguro do app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = {
                            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                            val imageFile = File(context.filesDir, "evidencia_camera_${timeStamp}.jpg")
                            val photoURI: Uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                imageFile
                            )
                            currentPhotoUri = photoURI
                            cameraLauncher.launch(photoURI)
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Câmera")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Galeria")
                    }
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(fotosList) { photoUri ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.DarkGray)
                        ) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { viewingPhotoUri = Uri.parse(photoUri) }
                            )
                            
                            // Edit button
                            IconButton(
                                onClick = { editingPhotoUri = Uri.parse(photoUri) },
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(4.dp)
                                    .size(28.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                            
                            // Delete button
                            IconButton(
                                onClick = { fotosList.remove(photoUri) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(28.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remover", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = { onSave(Gson().toJson(fotosList.toList())) },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Salvar")
            }
        }
    }

    if (viewingPhotoUri != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { viewingPhotoUri = null },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { viewingPhotoUri = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = viewingPhotoUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
}
