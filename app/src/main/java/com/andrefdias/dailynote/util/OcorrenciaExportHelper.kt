package com.andrefdias.dailynote.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia
import com.andrefdias.dailynote.data.local.entities.RoomNovaVitima
import com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object OcorrenciaExportHelper {

    fun shareTextWhatsApp(
        context: Context,
        ocorrencia: RoomNovaOcorrencia,
        vitimas: List<RoomNovaVitima>,
        veiculos: List<RoomNovoVeiculo>
    ) {
        val sb = StringBuilder()
        sb.append("*RELATÓRIO DE OCORRÊNCIA* - Talão: ${ocorrencia.talao}\n")
        sb.append("Data: ${ocorrencia.data} às ${ocorrencia.hora}\n")
        sb.append("Natureza: ${ocorrencia.natureza}\n")
        
        val endereco = listOfNotNull(
            ocorrencia.rua.takeIf { !it.isNullOrBlank() },
            ocorrencia.numero.takeIf { !it.isNullOrBlank() },
            ocorrencia.bairro.takeIf { !it.isNullOrBlank() },
            ocorrencia.cidade.takeIf { !it.isNullOrBlank() }
        ).joinToString(", ")
        
        sb.append("\n📍 *Local:* $endereco\n")
        
        if (vitimas.isNotEmpty()) {
            sb.append("\n🚑 *Vítimas (${vitimas.size}):*\n")
            vitimas.forEach { v ->
                sb.append(" - ${v.nome ?: "N/I"} (${v.idade ?: "N/I"} anos). Destino: ${v.hospitalDestino ?: "N/I"}\n")
            }
        }
        
        if (veiculos.isNotEmpty()) {
            sb.append("\n🚗 *Veículos (${veiculos.size}):*\n")
            veiculos.forEach { v ->
                sb.append(" - ${v.marca} ${v.modelo} (Placa: ${v.placa})\n")
            }
        }
        
        if (!ocorrencia.historico.isNullOrBlank()) {
            sb.append("\n📝 *Histórico:*\n${ocorrencia.historico}\n")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            setPackage("com.whatsapp")
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Se o WhatsApp não estiver instalado, mostra o chooser genérico
            val chooser = Intent.createChooser(intent, "Compartilhar ocorrência via...")
            context.startActivity(chooser)
        }
    }

    fun shareReportPdf(
        context: Context,
        ocorrencia: RoomNovaOcorrencia,
        veiculos: List<RoomNovoVeiculo>,
        vitimas: List<RoomNovaVitima>
    ) {
        val pdfUri = OcorrenciaPdfGenerator.generatePdf(context, ocorrencia, veiculos, vitimas)
        if (pdfUri != null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Compartilhar PDF"))
        }
    }

    fun shareReportAndImages(
        context: Context,
        ocorrencia: RoomNovaOcorrencia,
        veiculos: List<RoomNovoVeiculo>,
        vitimas: List<RoomNovaVitima>
    ) {
        val pdfUri = OcorrenciaPdfGenerator.generatePdf(context, ocorrencia, veiculos, vitimas)
        val uris = ArrayList<Uri>()
        if (pdfUri != null) uris.add(pdfUri)

        // Parse imagens
        val listType = object : TypeToken<List<String>>() {}.type
        val imagePaths: List<String> = try {
            Gson().fromJson(ocorrencia.fotosUrisJson, listType) ?: emptyList()
        } catch (e: Exception) { emptyList() }

        imagePaths.forEach { path ->
            val uri = Uri.parse(path)
            if (uri.scheme == "file") {
                val file = File(uri.path!!)
                if (file.exists()) {
                    val contentUri = FileProvider.getUriForFile(
                        context, 
                        "${context.packageName}.fileprovider", 
                        file
                    )
                    uris.add(contentUri)
                }
            } else if (uri.scheme == "content") {
                uris.add(uri)
            }
        }

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*" // Múltiplos tipos (PDF + Imagens)
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartilhar PDF e Imagens"))
    }

    fun exportToJson(
        context: Context,
        ocorrencia: RoomNovaOcorrencia,
        veiculos: List<RoomNovoVeiculo>,
        vitimas: List<RoomNovaVitima>
    ) {
        // Prepare base64 images to embed
        val listType = object : TypeToken<List<String>>() {}.type
        val imagePaths: List<String> = try {
            Gson().fromJson(ocorrencia.fotosUrisJson, listType) ?: emptyList()
        } catch (e: Exception) { emptyList() }

        val imageBase64Map = mutableMapOf<String, String>()
        
        imagePaths.forEach { path ->
            try {
                val uri = Uri.parse(path)
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    val outputStream = ByteArrayOutputStream()
                    // Compress to JPEG to reduce size
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                    val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                    imageBase64Map[path] = base64
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val exportModel = OcorrenciaExportModel(
            header = ocorrencia,
            pacientes = vitimas,
            veiculos = veiculos,
            imagesBase64 = imageBase64Map
        )

        val jsonStr = Gson().toJson(exportModel)
        
        try {
            val file = File(context.cacheDir, "Ocorrencia_${ocorrencia.talao}.json")
            FileOutputStream(file).use { it.write(jsonStr.toByteArray()) }
            
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Exportar JSON"))
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
