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
import com.andrefdias.dailynote.domain.repository.CalendarRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.first

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun calendarRepository(): CalendarRepository
    fun efetivoRepository(): com.andrefdias.dailynote.domain.repository.EfetivoRepository
    fun ocorrenciaRepository(): com.andrefdias.dailynote.domain.repository.OcorrenciaRepository
}

class AgendaWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val repository = entryPoint.calendarRepository()
        
        val dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val eventos = repository.getEventosForDayFlow(dateStr).first()
        val tarefas = repository.getTarefasForDayFlow(dateStr).first().filter { it.status != com.andrefdias.dailynote.domain.model.StatusTarefa.CONCLUIDA }

        provideContent {
            GlanceTheme {
                WidgetContent(eventos, tarefas)
            }
        }
    }

    @Composable
    private fun WidgetContent(
        eventos: List<com.andrefdias.dailynote.domain.model.CalendarEvento>,
        tarefas: List<com.andrefdias.dailynote.domain.model.CalendarTarefa>
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .padding(12.dp)
        ) {
            Row(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Agenda de Hoje",
                    style = TextStyle(color = GlanceTheme.colors.primary, fontWeight = FontWeight.Bold),
                    modifier = GlanceModifier.defaultWeight()
                )
            }

            if (eventos.isEmpty() && tarefas.isEmpty()) {
                Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum compromisso pendente.", style = TextStyle(color = GlanceTheme.colors.onBackground))
                }
            } else {
                eventos.take(2).forEach { evento ->
                    Text(
                        text = "📅 ${evento.titulo} (${evento.hora ?: "O dia todo"})",
                        style = TextStyle(color = GlanceTheme.colors.onBackground, fontWeight = FontWeight.Medium),
                        modifier = GlanceModifier.padding(vertical = 4.dp),
                        maxLines = 1
                    )
                }
                tarefas.take(2).forEach { tarefa ->
                    Text(
                        text = "☑ ${tarefa.titulo}",
                        style = TextStyle(color = GlanceTheme.colors.onBackground),
                        modifier = GlanceModifier.padding(vertical = 4.dp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

class AgendaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AgendaWidget()
}
