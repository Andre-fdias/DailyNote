package com.andrefdias.dailynote.ui.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.andrefdias.dailynote.MainActivity
import androidx.glance.appwidget.cornerRadius

class AcoesRapidasWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent(context)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .padding(12.dp)
        ) {
            Text(
                text = "Nova Ocorrência",
                style = TextStyle(
                    color = GlanceTheme.colors.onBackground,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.padding(bottom = 8.dp)
            )

            Row(modifier = GlanceModifier.fillMaxWidth().height(60.dp)) {
                ButtonWidget(
                    text = "Incêndio",
                    color = GlanceTheme.colors.errorContainer,
                    textColor = GlanceTheme.colors.onErrorContainer,
                    action = "Incêndio",
                    modifier = GlanceModifier.defaultWeight()
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                ButtonWidget(
                    text = "Resgate",
                    color = GlanceTheme.colors.primaryContainer,
                    textColor = GlanceTheme.colors.onPrimaryContainer,
                    action = "Resgate",
                    modifier = GlanceModifier.defaultWeight()
                )
            }
        }
    }

    @Composable
    private fun ButtonWidget(
        text: String,
        color: androidx.glance.unit.ColorProvider,
        textColor: androidx.glance.unit.ColorProvider,
        action: String,
        modifier: GlanceModifier
    ) {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .background(color)
                .cornerRadius(12.dp)
                .clickable(
                    actionStartActivity(
                        Intent(
                            androidx.glance.LocalContext.current,
                            MainActivity::class.java
                        ).apply {
                            this.action = "ACTION_NOVA_OCORRENCIA"
                            putExtra("CATEGORIA", action)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = TextStyle(color = textColor, fontWeight = FontWeight.Bold)
            )
        }
    }
}

class AcoesRapidasWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AcoesRapidasWidget()
}
