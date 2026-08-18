package com.andrefdias.dailynote.ui.designsystem.components.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andrefdias.dailynote.ui.designsystem.colors.FireColor
import com.andrefdias.dailynote.ui.designsystem.spacing.FireSpacing
import com.andrefdias.dailynote.ui.designsystem.typography.FireTypography

@Composable
fun FireTimeline(
    events: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = FireSpacing.Small),
        verticalArrangement = Arrangement.spacedBy(FireSpacing.Small)
    ) {
        events.forEachIndexed { index, event ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(FireColor.Primary, CircleShape)
                )
                Spacer(modifier = Modifier.width(FireSpacing.Small))
                Text(text = event, style = FireTypography.Body)
            }
        }
    }
}
