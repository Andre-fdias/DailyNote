package com.andrefdias.dailynote.ui.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class AlertasWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val repository = entryPoint.efetivoRepository()
        
        val efetivo = repository.getEfetivo().first()
        val alertas = mutableListOf<String>()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val today = LocalDate.now()
        
        efetivo.forEach { mil ->
            try {
                if (mil.validadeCnh.isNotBlank()) {
                    val dt = LocalDate.parse(mil.validadeCnh, formatter)
                    val days = ChronoUnit.DAYS.between(today, dt)
                    if (days < 0) alertas.add("🔴 ${mil.nomeDeGuerra} - CNH Vencida")
                    else if (days <= 30) alertas.add("🟡 ${mil.nomeDeGuerra} - CNH vence em $days dias")
                }
                if (mil.validadeToxicologico.isNotBlank()) {
                    val dt = LocalDate.parse(mil.validadeToxicologico, formatter)
                    val days = ChronoUnit.DAYS.between(today, dt)
                    if (days < 0) alertas.add("🔴 ${mil.nomeDeGuerra} - Tox. Vencido")
                    else if (days <= 30) alertas.add("🟡 ${mil.nomeDeGuerra} - Tox. em $days dias")
                }
            } catch (e: Exception) {}
        }

        provideContent {
            GlanceTheme {
                WidgetContent(alertas)
            }
        }
    }

    @Composable
    private fun WidgetContent(alertas: List<String>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .padding(12.dp)
        ) {
            Text(
                text = "Alertas Efetivo",
                style = TextStyle(color = GlanceTheme.colors.error, fontWeight = FontWeight.Bold),
                modifier = GlanceModifier.padding(bottom = 8.dp)
            )

            if (alertas.isEmpty()) {
                Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tudo em dia!", style = TextStyle(color = GlanceTheme.colors.onBackground))
                }
            } else {
                alertas.take(4).forEach { alerta ->
                    Text(
                        text = alerta,
                        style = TextStyle(color = GlanceTheme.colors.onBackground),
                        modifier = GlanceModifier.padding(vertical = 2.dp),
                        maxLines = 1
                    )
                }
                if (alertas.size > 4) {
                    Text(
                        text = "+ ${alertas.size - 4} alertas",
                        style = TextStyle(color = GlanceTheme.colors.primary, fontWeight = FontWeight.Medium),
                        modifier = GlanceModifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

class AlertasWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AlertasWidget()
}
