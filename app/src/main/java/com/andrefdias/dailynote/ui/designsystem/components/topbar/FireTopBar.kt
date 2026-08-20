package com.andrefdias.dailynote.ui.designsystem.components.topbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.shadow
import com.andrefdias.dailynote.ui.designsystem.typography.FireTypography
import com.andrefdias.dailynote.ui.designsystem.icons.FireIcons
import com.andrefdias.dailynote.ui.designsystem.components.buttons.FireIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FireTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    elevation: Dp = 0.dp,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(text = title, style = FireTypography.Headline, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            if (onBackClick != null) {
                FireIconButton(icon = FireIcons.ArrowBack, onClick = onBackClick)
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        modifier = Modifier.shadow(elevation)
    )
}
