package com.andrefdias.dailynote.ui.designsystem.components.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.andrefdias.dailynote.ui.designsystem.typography.FireTypography

@Composable
fun FireBottomNavigation(
    content: @Composable RowScope.() -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        content = content
    )
}

@Composable
fun FireTab(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    Tab(
        selected = selected,
        onClick = onClick,
        text = { Text(text = text, style = FireTypography.Title) },
        modifier = modifier
    )
}
