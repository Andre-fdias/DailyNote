package com.andrefdias.dailynote.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.andrefdias.dailynote.domain.model.OcorrenciaComMilitares
import com.google.gson.GsonBuilder
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {

    private fun getSharedFileUri(context: Context, file: File): android.net.Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun shareFile(context: Context, file: File, mimeType: String, title: String = "Compartilhar") {
        val uri = getSharedFileUri(context, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    fun exportToJsonAndShare(context: Context, data: List<OcorrenciaComMilitares>) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonString = gson.toJson(data)
        
        val fileName = "dailynote_export_${System.currentTimeMillis()}.json"
        val file = File(context.cacheDir, fileName)
        file.writeText(jsonString)
        
        shareFile(context, file, "application/json", "Compartilhar Exportação JSON")
    }

    fun exportToPdfAndShare(context: Context, data: List<OcorrenciaComMilitares>) {
        val document = PdfDocument()
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            isFakeBoldText = true
        }

        var yPos = 50f
        val xPos = 50f
        val lineHeight = 20f

        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Relatório Mapa Força - DailyNotes ($dateStr)", xPos, yPos, titlePaint)
        yPos += lineHeight * 2
        
        canvas.drawText("Total de Registros: ${data.size}", xPos, yPos, paint)
        yPos += lineHeight * 2

        data.forEach { item ->
            if (yPos > 800) {
                document.finishPage(page)
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, document.pages.size + 1).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPos = 50f
            }
            
            val occ = item.ocorrencia
            canvas.drawText("Data: ${occ.data} | Talão: ${occ.talao} | VTR: ${occ.vtr}", xPos, yPos, titlePaint)
            yPos += lineHeight
            canvas.drawText("Natureza: ${occ.natureza} | Endereço: ${occ.endereco}, ${occ.cidade}", xPos, yPos, paint)
            yPos += lineHeight
            canvas.drawText("CMT: ${occ.cmtVtr} | Vítimas: ${occ.vitimas} | Fatais: ${occ.vitimasFatais}", xPos, yPos, paint)
            yPos += lineHeight * 1.5f
        }

        document.finishPage(page)

        val fileName = "Relatorio_Forca_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)
        try {
            document.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        document.close()

        shareFile(context, file, "application/pdf", "Compartilhar Relatório PDF")
    }

    fun exportToExcelAndShare(context: Context, data: List<OcorrenciaComMilitares>) {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Mapa Forca")

            // Headers
            val headerRow = sheet.createRow(0)
            val headers = listOf("Data", "Talão", "Natureza", "Viatura", "Comandante", "Endereço", "Cidade", "Vítimas", "Fatais", "Equipe")
            headers.forEachIndexed { i, title ->
                val cell = headerRow.createCell(i)
                cell.setCellValue(title)
            }

            data.forEachIndexed { index, item ->
                val row = sheet.createRow(index + 1)
                val occ = item.ocorrencia
                row.createCell(0).setCellValue(occ.data)
                row.createCell(1).setCellValue(occ.talao)
                row.createCell(2).setCellValue(occ.natureza)
                row.createCell(3).setCellValue(occ.vtr)
                row.createCell(4).setCellValue(occ.cmtVtr)
                row.createCell(5).setCellValue(occ.endereco)
                row.createCell(6).setCellValue(occ.cidade)
                row.createCell(7).setCellValue(occ.vitimas.toDouble())
                row.createCell(8).setCellValue(occ.vitimasFatais.toDouble())
                
                val militaresStr = item.militares.joinToString(", ") { it.nomeGuerra }
                row.createCell(9).setCellValue(militaresStr)
            }

            val fileName = "Relatorio_Forca_${System.currentTimeMillis()}.xlsx"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { out ->
                workbook.write(out)
            }
            workbook.close()

            shareFile(context, file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Compartilhar Excel")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exportEquipesToJsonAndShare(context: Context, data: List<com.andrefdias.dailynote.domain.model.EquipeServico>) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonString = gson.toJson(data)
        
        val fileName = "dailynote_mapa_forca_${System.currentTimeMillis()}.json"
        val file = File(context.cacheDir, fileName)
        file.writeText(jsonString)
        
        shareFile(context, file, "application/json", "Compartilhar Mapa Força JSON")
    }

    fun exportEquipesToPdfAndShare(context: Context, data: List<com.andrefdias.dailynote.domain.model.EquipeServico>) {
        val document = PdfDocument()
        val pageWidth = 842
        val pageHeight = 595
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 20f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val headerPaint = Paint().apply {
            color = Color.WHITE
            textSize = 12f
            isFakeBoldText = true
        }
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
        }
        val bgPaint = Paint().apply { color = Color.rgb(238, 242, 255) } // Light Indigo for Zebra
        val headerBgPaint = Paint().apply { color = Color.rgb(67, 56, 202) } // Indigo 700

        var yPos = 50f
        val startX = 30f
        
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Relatório Mapa Força - DailyNotes ($dateStr)", pageWidth / 2f, yPos, titlePaint)
        yPos += 30f
        
        val textTotalPaint = Paint(textPaint).apply { isFakeBoldText = true; textSize = 13f }
        canvas.drawText("Total de Equipes: ${data.size}", startX, yPos, textTotalPaint)
        yPos += 30f

        val colWidths = floatArrayOf(80f, 130f, 120f, 70f, 80f, 70f, 70f, 90f)
        val colHeaders = arrayOf("Graduação", "Nome Completo", "Função", "RE", "Mergulhador", "OVB", "Escala", "Dejem Horário")

        fun drawTableHeader(y: Float) {
            canvas.drawRect(startX, y - 15f, pageWidth - 30f, y + 10f, headerBgPaint)
            var currentX = startX + 5f
            for (i in colHeaders.indices) {
                canvas.drawText(colHeaders[i], currentX, y, headerPaint)
                currentX += colWidths[i]
            }
        }
        
        fun checkNewPage(neededSpace: Float) {
            if (yPos + neededSpace > pageHeight - 30f) {
                document.finishPage(page)
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, document.pages.size + 1).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPos = 50f
            }
        }

        data.forEach { equipe ->
            checkNewPage(60f)
            val eqHeaderPaint = Paint(headerBgPaint).apply { color = Color.rgb(30, 58, 138) } // Dark Blue
            canvas.drawRect(startX, yPos - 15f, pageWidth - 30f, yPos + 10f, eqHeaderPaint)
            canvas.drawText("DATA: ${equipe.data}   |   UNIDADE: ${equipe.unidade}   |   POSTO: ${equipe.posto}", startX + 5f, yPos, headerPaint)
            yPos += 30f
            
            drawTableHeader(yPos)
            yPos += 25f

            equipe.viaturas.forEach { ev ->
                checkNewPage(40f)
                val vtrPrefix = ev.viatura?.prefixo ?: "N/A"
                val vtrBgPaint = Paint(headerBgPaint).apply { color = Color.rgb(224, 231, 255) } // Light Indigo
                val vtrTextPaint = Paint(headerPaint).apply { color = Color.rgb(30, 41, 59); isFakeBoldText = true }
                
                canvas.drawRect(startX, yPos - 12f, pageWidth - 30f, yPos + 8f, vtrBgPaint)
                canvas.drawText("VIATURA: $vtrPrefix", startX + 5f, yPos, vtrTextPaint)
                yPos += 20f

                var isZebra = false
                ev.militaresEscalados.forEach { mil ->
                    checkNewPage(25f)
                    if (isZebra) canvas.drawRect(startX, yPos - 12f, pageWidth - 30f, yPos + 6f, bgPaint)
                    
                    val m = mil.militar
                    var cx = startX + 5f
                    canvas.drawText(m?.graduacao ?: "N/I", cx, yPos, textPaint); cx += colWidths[0]
                    canvas.drawText(m?.nomeCompleto?.take(22) ?: "Desconhecido", cx, yPos, textPaint); cx += colWidths[1]
                    canvas.drawText(mil.funcao, cx, yPos, textPaint); cx += colWidths[2]
                    canvas.drawText(m?.re ?: "N/I", cx, yPos, textPaint); cx += colWidths[3]
                    canvas.drawText(if (m?.mergulhador == true) "Sim" else "Não", cx, yPos, textPaint); cx += colWidths[4]
                    canvas.drawText(m?.ovb ?: "N/I", cx, yPos, textPaint); cx += colWidths[5]
                    canvas.drawText(mil.tipoEscala, cx, yPos, textPaint); cx += colWidths[6]
                    val hr = if (mil.dejemHorarioInicio != null) "${mil.dejemHorarioInicio} - ${mil.dejemHorarioFim}" else "-"
                    canvas.drawText(hr, cx, yPos, textPaint); cx += colWidths[7]

                    yPos += 18f
                    isZebra = !isZebra
                }
                yPos += 10f // Space between viaturas
            }
            yPos += 15f // Space between equipes
        }

        document.finishPage(page)

        val fileName = "Relatorio_MapaForca_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)
        try { document.writeTo(FileOutputStream(file)) } catch (e: Exception) { e.printStackTrace() }
        document.close()

        shareFile(context, file, "application/pdf", "Compartilhar Relatório Mapa Força PDF")
    }

    fun exportEquipesToExcelAndShare(context: Context, data: List<com.andrefdias.dailynote.domain.model.EquipeServico>) {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Mapa Forca Equipes")

            // Headers
            val headerRow = sheet.createRow(0)
            val headers = listOf("Data", "Unidade", "Posto", "Tipo Escala Geral", "Viatura", "Graduação", "Nome Completo", "RE", "Mergulhador", "OVB", "Função", "Escala Militar", "Horário Escala")
            headers.forEachIndexed { i, title ->
                val cell = headerRow.createCell(i)
                cell.setCellValue(title)
            }

            var rowIndex = 1
            data.forEach { equipe ->
                if (equipe.viaturas.isEmpty()) {
                    val row = sheet.createRow(rowIndex++)
                    row.createCell(0).setCellValue(equipe.data)
                    row.createCell(1).setCellValue(equipe.unidade)
                    row.createCell(2).setCellValue(equipe.posto)
                    row.createCell(3).setCellValue(equipe.tipoEscala)
                } else {
                    equipe.viaturas.forEach { ev ->
                        if (ev.militaresEscalados.isEmpty()) {
                            val row = sheet.createRow(rowIndex++)
                            row.createCell(0).setCellValue(equipe.data)
                            row.createCell(1).setCellValue(equipe.unidade)
                            row.createCell(2).setCellValue(equipe.posto)
                            row.createCell(3).setCellValue(equipe.tipoEscala)
                            row.createCell(4).setCellValue(ev.viatura?.prefixo ?: "N/I")
                        } else {
                            ev.militaresEscalados.forEach { mil ->
                                val m = mil.militar
                                val row = sheet.createRow(rowIndex++)
                                row.createCell(0).setCellValue(equipe.data)
                                row.createCell(1).setCellValue(equipe.unidade)
                                row.createCell(2).setCellValue(equipe.posto)
                                row.createCell(3).setCellValue(equipe.tipoEscala)
                                row.createCell(4).setCellValue(ev.viatura?.prefixo ?: "N/I")
                                row.createCell(5).setCellValue(m?.graduacao ?: "N/I")
                                row.createCell(6).setCellValue(m?.nomeCompleto ?: "Desconhecido")
                                row.createCell(7).setCellValue(m?.re ?: "N/I")
                                row.createCell(8).setCellValue(if (m?.mergulhador == true) "Sim" else "Não")
                                row.createCell(9).setCellValue(m?.ovb ?: "N/I")
                                row.createCell(10).setCellValue(mil.funcao)
                                row.createCell(11).setCellValue(mil.tipoEscala)
                                val hr = if (mil.dejemHorarioInicio != null) "${mil.dejemHorarioInicio}-${mil.dejemHorarioFim}" else "-"
                                row.createCell(12).setCellValue(hr)
                            }
                        }
                    }
                }
            }

            val fileName = "Relatorio_MapaForca_${System.currentTimeMillis()}.xlsx"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { workbook.write(it) }
            workbook.close()
            
            shareFile(context, file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Compartilhar Relatório Mapa Força Excel")
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "Erro ao exportar Excel", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
