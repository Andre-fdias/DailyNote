package com.andrefdias.dailynote.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia
import com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo
import com.andrefdias.dailynote.data.local.entities.RoomNovaVitima
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream

object OcorrenciaPdfGenerator {

    fun generatePdf(
        context: Context,
        ocorrencia: RoomNovaOcorrencia,
        veiculos: List<RoomNovoVeiculo>,
        vitimas: List<RoomNovaVitima>
    ): Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val headerBgPaint = Paint().apply {
            color = Color.parseColor("#1A365D") // Dark Blue
            style = Paint.Style.FILL
        }
        val headerTitlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 24f
            color = Color.WHITE
        }
        val headerSubTitlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 14f
            color = Color.WHITE
        }
        val sectionTitleBgPaint = Paint().apply {
            color = Color.parseColor("#E3F2FD") // Light Blue background for sections
            style = Paint.Style.FILL
        }
        val sectionTitlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 16f
            color = Color.parseColor("#0D47A1") // Dark Blue text
        }
        val labelPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
            color = Color.DKGRAY
        }
        val textPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 12f
            color = Color.BLACK
        }

        var currentY = 0f
        val marginX = 40f
        val pageHeight = 842f
        val pageWidth = 595f
        val lineSpacing = 20f

        fun checkPageBreak(requiredSpace: Float) {
            if (currentY + requiredSpace > pageHeight - 50f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = 40f
            }
        }

        fun drawTextWrapped(text: String, x: Float, y: Float, paint: Paint, maxWidth: Float): Float {
            val words = text.split(" ")
            var line = ""
            var currentYLine = y
            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(testLine) > maxWidth) {
                    canvas.drawText(line, x, currentYLine, paint)
                    line = word
                    currentYLine += lineSpacing
                } else {
                    line = testLine
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, x, currentYLine, paint)
            }
            return currentYLine + lineSpacing
        }
        
        fun drawSectionTitle(title: String) {
            checkPageBreak(40f)
            val rect = android.graphics.RectF(marginX, currentY, pageWidth - marginX, currentY + 30f)
            canvas.drawRoundRect(rect, 4f, 4f, sectionTitleBgPaint)
            canvas.drawText(title, marginX + 10f, currentY + 20f, sectionTitlePaint)
            currentY += 40f
        }

        // --- DRAW HEADER ---
        canvas.drawRect(0f, 0f, pageWidth, 100f, headerBgPaint)
        canvas.drawText("RELATÓRIO DE OCORRÊNCIA", marginX, 45f, headerTitlePaint)
        canvas.drawText("Corpo de Bombeiros / SIATE - Resumo Oficial", marginX, 70f, headerSubTitlePaint)
        currentY = 130f
        
        // Dados Principais
        canvas.drawText("Talão:", marginX, currentY, labelPaint)
        canvas.drawText(ocorrencia.talao, marginX + 50f, currentY, textPaint)
        
        val dateStr = "${ocorrencia.data} às ${ocorrencia.hora}"
        canvas.drawText("Data:", marginX + 250f, currentY, labelPaint)
        canvas.drawText(dateStr, marginX + 290f, currentY, textPaint)
        currentY += lineSpacing
        
        canvas.drawText("Natureza:", marginX, currentY, labelPaint)
        canvas.drawText(ocorrencia.natureza, marginX + 70f, currentY, textPaint)
        currentY += lineSpacing
        
        val status = if (ocorrencia.isConcluida) "Encerrada" else "Em Andamento"
        canvas.drawText("Status:", marginX, currentY, labelPaint)
        canvas.drawText(status, marginX + 50f, currentY, textPaint)
        currentY += 40f

        // Endereço
        drawSectionTitle("1. LOCAL DA OCORRÊNCIA")
        val endereco = listOfNotNull(
            ocorrencia.rua.takeIf { !it.isNullOrBlank() },
            ocorrencia.numero.takeIf { !it.isNullOrBlank() },
            ocorrencia.bairro.takeIf { !it.isNullOrBlank() },
            ocorrencia.cidade.takeIf { !it.isNullOrBlank() }
        ).joinToString(", ").ifEmpty { "Não informado" }
        currentY = drawTextWrapped(endereco, marginX, currentY, textPaint, 500f)
        currentY += 10f

        // Recursos (Simplificado)
        drawSectionTitle("2. RECURSOS EMPREGADOS")
        
        val gson = Gson()
        try {
            val vtrType = object : TypeToken<List<Map<String, String>>>() {}.type
            val viaturasList: List<Map<String, String>> = gson.fromJson(ocorrencia.viatura, vtrType) ?: emptyList()
            if (viaturasList.isEmpty()) {
                canvas.drawText("Nenhuma viatura informada.", marginX, currentY, textPaint)
                currentY += lineSpacing
            } else {
                viaturasList.forEach { vtr ->
                    checkPageBreak(60f)
                    val prefixo = vtr["prefixo"] ?: "Desconhecido"
                    val kmSaida = vtr["kmSaida"] ?: "N/I"
                    val kmQuartel = vtr["kmQuartel"] ?: "N/I"
                    canvas.drawText("• VTR $prefixo (Km S: $kmSaida | Km F: $kmQuartel)", marginX, currentY, labelPaint)
                    currentY += lineSpacing
                }
            }
        } catch (e: Exception) {
            canvas.drawText("Erro ao ler recursos.", marginX, currentY, textPaint)
            currentY += lineSpacing
        }
        currentY += 10f

        // Veículos
        drawSectionTitle("3. VEÍCULOS ENVOLVIDOS")
        if (veiculos.isEmpty()) {
            canvas.drawText("Nenhum veículo registrado.", marginX, currentY, textPaint)
            currentY += lineSpacing
        } else {
            veiculos.forEach { v ->
                checkPageBreak(40f)
                canvas.drawText("• ${v.marca} ${v.modelo} - Placa: ${v.placa.ifEmpty { "N/A" }}", marginX, currentY, textPaint)
                currentY += lineSpacing
            }
        }
        currentY += 10f

        // Vítimas
        drawSectionTitle("4. VÍTIMAS E ENVOLVIDOS")
        if (vitimas.isEmpty()) {
            canvas.drawText("Nenhuma vítima registrada.", marginX, currentY, textPaint)
            currentY += lineSpacing
        } else {
            vitimas.forEach { v ->
                checkPageBreak(80f)
                canvas.drawText("• VÍTIMA: ${v.nome} (${v.idade ?: "N/I"} anos)", marginX, currentY, labelPaint)
                currentY += lineSpacing
                
                canvas.drawText("  Socorrido por: ${v.quemSocorreu}", marginX, currentY, textPaint)
                currentY += lineSpacing
                
                if (!v.lesoes.isNullOrEmpty()) {
                    currentY = drawTextWrapped("  Histórico/Lesões: ${v.lesoes}", marginX, currentY, textPaint, 500f)
                }
                

                
                if (!v.hospitalDestino.isNullOrEmpty()) {
                    canvas.drawText("  Destino: ${v.hospitalDestino}", marginX, currentY, textPaint)
                    currentY += lineSpacing
                }
                currentY += 10f
            }
        }
        currentY += 10f

        // Histórico
        drawSectionTitle("5. HISTÓRICO DA OCORRÊNCIA")
        val historico = ocorrencia.historico.takeIf { !it.isNullOrBlank() } ?: "Nenhum histórico relatado."
        currentY = drawTextWrapped(historico, marginX, currentY, textPaint, 500f)

        pdfDocument.finishPage(page)

        // Save to file
        val file = File(context.cacheDir, "Relatorio_Ocorrencia_${ocorrencia.talao}.pdf")
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}
