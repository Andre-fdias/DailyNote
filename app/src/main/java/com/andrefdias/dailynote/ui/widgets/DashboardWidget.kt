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
import androidx.glance.appwidget.cornerRadius
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DashboardWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        
        val dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val numTarefas = entryPoint.calendarRepository().getTarefasForDayFlow(dateStr).first().filter { it.status != com.andrefdias.dailynote.domain.model.StatusTarefa.CONCLUIDA }.size
        
        val dateBr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val numOcorrencias = entryPoint.ocorrenciaRepository().getAllLocalOcorrenciasFlow().first().filter { it.data == dateBr }.size
        val numEfetivo = entryPoint.efetivoRepository().getEfetivo().first().size

        provideContent {
            GlanceTheme {
                WidgetContent(numTarefas, numOcorrencias, numEfetivo)
            }
        }
    }

    @Composable
    private fun WidgetContent(tarefas: Int, ocorrencias: Int, efetivo: Int) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .padding(12.dp)
        ) {
            Text(
                text = "Dashboard",
                style = TextStyle(color = GlanceTheme.colors.primary, fontWeight = FontWeight.Bold),
                modifier = GlanceModifier.padding(bottom = 12.dp)
            )

            Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                MetricCard(
                    title = "Ocorrências\nHoje",
                    value = ocorrencias.toString(),
                    color = GlanceTheme.colors.errorContainer,
                    textColor = GlanceTheme.colors.onErrorContainer,
                    modifier = GlanceModifier.defaultWeight()
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                MetricCard(
                    title = "Tarefas\nPendentes",
                    value = tarefas.toString(),
                    color = GlanceTheme.colors.secondaryContainer,
                    textColor = GlanceTheme.colors.onSecondaryContainer,
                    modifier = GlanceModifier.defaultWeight()
                )
            }
            Spacer(modifier = GlanceModifier.height(8.dp))
            Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                MetricCard(
                    title = "Efetivo\nTotal",
                    value = efetivo.toString(),
                    color = GlanceTheme.colors.primaryContainer,
                    textColor = GlanceTheme.colors.onPrimaryContainer,
                    modifier = GlanceModifier.defaultWeight()
                )
            }
        }
    }

    @Composable
    private fun MetricCard(
        title: String,
        value: String,
        color: androidx.glance.unit.ColorProvider,
        textColor: androidx.glance.unit.ColorProvider,
        modifier: GlanceModifier
    ) {
        Column(
            modifier = modifier
                .fillMaxHeight()
                .background(color)
                .cornerRadius(12.dp)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = TextStyle(color = textColor, fontWeight = FontWeight.Bold),
                modifier = GlanceModifier.padding(bottom = 4.dp)
            )
            Text(
                text = title,
                style = TextStyle(color = textColor),
            )
        }
    }
}

class DashboardWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DashboardWidget()
}
