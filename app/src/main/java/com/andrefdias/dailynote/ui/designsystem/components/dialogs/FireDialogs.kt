package com.andrefdias.dailynote.ui.designsystem.components.dialogs

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.andrefdias.dailynote.ui.designsystem.dimensions.FireShapes
import com.andrefdias.dailynote.ui.designsystem.typography.FireTypography
import com.andrefdias.dailynote.ui.designsystem.components.buttons.FireButton
import com.andrefdias.dailynote.ui.designsystem.components.buttons.FireTextButton

@Composable
fun FireDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    properties: androidx.compose.ui.window.DialogProperties = androidx.compose.ui.window.DialogProperties(),
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = title, style = FireTypography.Title, fontWeight = FontWeight.Bold) },
        text = content,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        shape = FireShapes.Large,
        properties = properties
    )
}

@Composable
fun FireConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "Confirmar",
    dismissText: String = "Cancelar"
) {
    FireDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        confirmButton = {
            FireButton(text = confirmText, onClick = onConfirm)
        },
        dismissButton = {
            FireTextButton(text = dismissText, onClick = onDismissRequest)
        }
    ) {
        Text(text = message, style = FireTypography.Body)
    }
}

@Composable
fun FirePermissionDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    permissionName: String,
    message: String
) {
    FireConfirmationDialog(
        onDismissRequest = onDismissRequest,
        onConfirm = onConfirm,
        title = "Permissão Necessária",
        message = message,
        confirmText = "Conceder",
        dismissText = "Não permitir"
    )
}
