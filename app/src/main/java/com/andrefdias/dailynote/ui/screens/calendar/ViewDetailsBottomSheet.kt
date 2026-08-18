package com.andrefdias.dailynote.ui.screens.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import com.andrefdias.dailynote.ui.designsystem.colors.FireColors
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewDetailsBottomSheet(
    title: String,
    subtitle: String,
    description: String? = null,
    details: List<Pair<String, String>>,
    canEdit: Boolean = true,
    subtarefas: List<com.andrefdias.dailynote.data.local.entities.RoomSubtarefa> = emptyList(),
    onToggleSubtarefa: ((com.andrefdias.dailynote.data.local.entities.RoomSubtarefa) -> Unit)? = null,
    onEditSubtarefaTitle: ((com.andrefdias.dailynote.data.local.entities.RoomSubtarefa, String) -> Unit)? = null,
    onDeleteSubtarefa: ((com.andrefdias.dailynote.data.local.entities.RoomSubtarefa) -> Unit)? = null,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var editingSubtarefa by remember { mutableStateOf<com.andrefdias.dailynote.data.local.entities.RoomSubtarefa?>(null) }
    var editingText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column {
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = FireColors.OnBackground
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = FireColors.Primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider()

            // Description
            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    fontSize = 15.sp,
                    color = FireColors.OnSurfaceVariant
                )
                HorizontalDivider()
            }

            // Details List
            if (details.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    details.forEach { (label, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = FireColors.OnSurfaceVariant
                            )
                            if (label == "Local" && value.isNotBlank()) {
                                val context = LocalContext.current
                                Text(
                                    text = value,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FireColors.Primary,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier.clickable {
                                        val uri = Uri.parse("geo:0,0?q=${Uri.encode(value)}")
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(value)}")
                                            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                        }
                                    }
                                )
                            } else {
                                Text(
                                    text = value,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FireColors.OnBackground
                                )
                            }
                        }
                    }
                }
                HorizontalDivider()
            }

            // Subtarefas Checklist section
            if (subtarefas.isNotEmpty()) {
                Text(
                    text = "Checklist de Subtarefas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = FireColors.Primary
                )

                val completedCount = subtarefas.count { it.concluida }
                val totalCount = subtarefas.size
                val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progresso: $completedCount/$totalCount",
                            fontSize = 13.sp,
                            color = FireColors.OnSurfaceVariant
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = FireColors.Primary
                        )
                    }

                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(3.dp)),
                        color = FireColors.Primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                // Render tree of subtarefas
                var collapsedIds by remember { mutableStateOf(emptySet<String>()) }
                val sortedList = remember(subtarefas) { buildTreeSortedList(subtarefas) }
                val visibleList = sortedList.filter { (node, _) ->
                    var parentId = node.parentId
                    var visible = true
                    while (parentId != null) {
                        if (collapsedIds.contains(parentId)) {
                            visible = false
                            break
                        }
                        val parentNode = subtarefas.find { it.id == parentId }
                        parentId = parentNode?.parentId
                    }
                    visible
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    visibleList.forEach { (node, level) ->
                        val hasChildren = subtarefas.any { it.parentId == node.id }
                        val isCollapsed = collapsedIds.contains(node.id)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = (level * 20).dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (hasChildren) {
                                Icon(
                                    imageVector = if (isCollapsed) Icons.Default.ArrowRight else Icons.Default.ArrowDropDown,
                                    contentDescription = "Expandir/Recolher",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            collapsedIds = if (isCollapsed) {
                                                collapsedIds - node.id
                                            } else {
                                                collapsedIds + node.id
                                            }
                                        }
                                )
                            } else {
                                Spacer(modifier = Modifier.size(24.dp))
                            }

                            Checkbox(
                                checked = node.concluida,
                                onCheckedChange = { onToggleSubtarefa?.invoke(node) }
                            )

                            Text(
                                text = node.titulo,
                                fontSize = 14.sp,
                                textDecoration = if (node.concluida) TextDecoration.LineThrough else null,
                                color = if (node.concluida) FireColors.OnSurfaceVariant.copy(alpha = 0.6f) else FireColors.OnBackground,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onToggleSubtarefa?.invoke(node) }
                            )

                            IconButton(
                                onClick = {
                                    editingSubtarefa = node
                                    editingText = node.titulo
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar título",
                                    tint = FireColors.Primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = { onDeleteSubtarefa?.invoke(node) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Excluir subtarefa",
                                    tint = FireColors.Error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()
            }

            if (editingSubtarefa != null) {
                AlertDialog(
                    onDismissRequest = { editingSubtarefa = null },
                    title = { Text("Editar Subtarefa") },
                    text = {
                        OutlinedTextField(
                            value = editingText,
                            onValueChange = { editingText = it },
                            label = { Text("Título da Subtarefa") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (editingText.isNotBlank()) {
                                    onEditSubtarefaTitle?.invoke(editingSubtarefa!!, editingText.trim())
                                }
                                editingSubtarefa = null
                            }
                        ) {
                            Text("Salvar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { editingSubtarefa = null }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = FireColors.Error
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FireColors.Error)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Excluir")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Excluir")
                }

                if (canEdit) {
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = FireColors.Primary)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun buildTreeSortedList(subtarefas: List<com.andrefdias.dailynote.data.local.entities.RoomSubtarefa>): List<Pair<com.andrefdias.dailynote.data.local.entities.RoomSubtarefa, Int>> {
    val result = mutableListOf<Pair<com.andrefdias.dailynote.data.local.entities.RoomSubtarefa, Int>>()
    val roots = subtarefas.filter { it.parentId == null }
    
    fun dfs(node: com.andrefdias.dailynote.data.local.entities.RoomSubtarefa, level: Int) {
        result.add(node to level)
        val children = subtarefas.filter { it.parentId == node.id }
        children.forEach { child ->
            dfs(child, level + 1)
        }
    }
    
    roots.forEach { root ->
        dfs(root, 0)
    }
    
    // Add any orphaned elements as safety
    val processedIds = result.map { it.first.id }.toSet()
    subtarefas.filter { it.id !in processedIds }.forEach { orphaned ->
        result.add(orphaned to 0)
    }
    
    return result
}
