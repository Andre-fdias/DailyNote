package com.andrefdias.dailynote.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.andrefdias.dailynote.domain.calendar.NotificationCenter
import com.andrefdias.dailynote.domain.model.CalendarNotificacao
import com.andrefdias.dailynote.domain.model.CategoriaNotificacao
import com.andrefdias.dailynote.domain.model.PrioridadeTarefa
import com.andrefdias.dailynote.domain.repository.EfetivoRepository
import com.andrefdias.dailynote.domain.repository.CalendarRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

@HiltWorker
class EfetivoAlertWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val efetivoRepository: EfetivoRepository,
    private val calendarRepository: CalendarRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "EfetivoAlertWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "▶️ Iniciando checagem de vencimentos do Efetivo...")
            val efetivos = efetivoRepository.getEfetivo().first()
            if (efetivos.isEmpty()) {
                Log.w(TAG, "⚠️ Nenhum efetivo encontrado na planilha.")
                return@withContext Result.success()
            }

            val hoje = LocalDate.now()
            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val formatterTwoDigitYear = DateTimeFormatter.ofPattern("dd/MM/yy")

            fun parseDate(dateStr: String): LocalDate? {
                if (dateStr.isBlank()) return null
                val cleaned = dateStr.trim()
                return try {
                    if (cleaned.length == 8) LocalDate.parse(cleaned, formatterTwoDigitYear)
                    else LocalDate.parse(cleaned, dateFormatter)
                } catch (e: Exception) { null }
            }

            // Busca notificações já existentes hoje para não duplicar
            val notificacoesHoje = calendarRepository.getNotificacoes()
                .filter { it.data == hoje.toString() && it.categoria == CategoriaNotificacao.EFETIVO }
                .map { it.titulo }
                .toSet()

            var numAlertas = 0

            efetivos.forEach { militar ->
                val nome = militar.nomeDeGuerra.ifBlank { militar.nomeCompleto }

                // Checar CNH
                val dataCnh = parseDate(militar.validadeCnh)
                if (dataCnh != null) {
                    val dias = ChronoUnit.DAYS.between(hoje, dataCnh)
                    val (titulo, descricao, prioridade) = when {
                        dias < 0 -> Triple(
                            "🔴 CNH VENCIDA: $nome",
                            "CNH venceu há ${-dias} dias (${militar.validadeCnh}). Regularize urgente!",
                            PrioridadeTarefa.ALTA
                        )
                        dias <= 30 -> Triple(
                            "🟡 CNH a vencer: $nome",
                            "CNH vence em $dias dias (${militar.validadeCnh}). Providencie renovação.",
                            PrioridadeTarefa.ALTA
                        )
                        dias <= 90 -> Triple(
                            "🔵 CNH: $nome",
                            "CNH vence em $dias dias (${militar.validadeCnh}).",
                            PrioridadeTarefa.MEDIA
                        )
                        else -> Triple("", "", PrioridadeTarefa.BAIXA)
                    }
                    if (titulo.isNotBlank() && titulo !in notificacoesHoje) {
                        salvarEDisparar(titulo, descricao, prioridade)
                        numAlertas++
                    }
                }

                // Checar Exame Toxicológico
                val dataTox = parseDate(militar.validadeToxicologico)
                if (dataTox != null) {
                    val dias = ChronoUnit.DAYS.between(hoje, dataTox)
                    val (titulo, descricao, prioridade) = when {
                        dias < 0 -> Triple(
                            "🔴 TOX VENCIDO: $nome",
                            "Exame Toxicológico venceu há ${-dias} dias (${militar.validadeToxicologico}). Regularize urgente!",
                            PrioridadeTarefa.ALTA
                        )
                        dias <= 30 -> Triple(
                            "🟡 Tox a vencer: $nome",
                            "Exame Toxicológico vence em $dias dias (${militar.validadeToxicologico}).",
                            PrioridadeTarefa.ALTA
                        )
                        dias <= 90 -> Triple(
                            "🔵 Tox: $nome",
                            "Exame Toxicológico vence em $dias dias (${militar.validadeToxicologico}).",
                            PrioridadeTarefa.MEDIA
                        )
                        else -> Triple("", "", PrioridadeTarefa.BAIXA)
                    }
                    if (titulo.isNotBlank() && titulo !in notificacoesHoje) {
                        salvarEDisparar(titulo, descricao, prioridade)
                        numAlertas++
                    }
                }

                // Checar IAS
                val dataIas = parseDate(militar.validadeIas)
                if (dataIas != null) {
                    val dias = ChronoUnit.DAYS.between(hoje, dataIas)
                    val (titulo, descricao, prioridade) = when {
                        dias < 0 -> Triple(
                            "🔴 IAS VENCIDO: $nome",
                            "IAS venceu há ${-dias} dias (${militar.validadeIas}). Regularize urgente!",
                            PrioridadeTarefa.ALTA
                        )
                        dias <= 30 -> Triple(
                            "🟡 IAS a vencer: $nome",
                            "IAS vence em $dias dias (${militar.validadeIas}).",
                            PrioridadeTarefa.ALTA
                        )
                        dias <= 90 -> Triple(
                            "🔵 IAS: $nome",
                            "IAS vence em $dias dias (${militar.validadeIas}).",
                            PrioridadeTarefa.MEDIA
                        )
                        else -> Triple("", "", PrioridadeTarefa.BAIXA)
                    }
                    if (titulo.isNotBlank() && titulo !in notificacoesHoje) {
                        salvarEDisparar(titulo, descricao, prioridade)
                        numAlertas++
                    }
                }
            }

            Log.i(TAG, "✅ Checagem concluída. $numAlertas alertas gerados.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao executar checagem de vencimentos", e)
            Result.retry()
        }
    }

    private suspend fun salvarEDisparar(titulo: String, descricao: String, prioridade: PrioridadeTarefa) {
        val notificacao = CalendarNotificacao(
            id = UUID.randomUUID().toString(),
            categoria = CategoriaNotificacao.EFETIVO,
            titulo = titulo,
            descricao = descricao,
            data = LocalDate.now().toString(),
            hora = java.time.LocalTime.now().toString().take(5),
            prioridade = prioridade,
            lida = false,
            origem = "EFETIVO"
        )
        calendarRepository.saveNotificacao(notificacao)
        NotificationCenter.triggerSystemNotification(appContext, notificacao)
    }
}
