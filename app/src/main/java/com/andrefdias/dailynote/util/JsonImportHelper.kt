package com.andrefdias.dailynote.util

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia
import com.andrefdias.dailynote.data.local.entities.RoomNovaVitima
import com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo
import com.andrefdias.dailynote.domain.repository.OcorrenciaRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class OcorrenciaExportModel(
    val header: RoomNovaOcorrencia,
    val pacientes: List<RoomNovaVitima>,
    val veiculos: List<RoomNovoVeiculo>,
    val imagesBase64: Map<String, String>? = null
)

object JsonImportHelper {

    fun importOccurrenceFromJson(
        context: Context,
        jsonStr: String,
        repository: OcorrenciaRepository
    ) {
        val gson = Gson()
        val exportModel: OcorrenciaExportModel? = try {
            gson.fromJson(jsonStr, OcorrenciaExportModel::class.java)
        } catch (e: Exception) {
            null
        }

        if (exportModel == null || exportModel.header.talao.isBlank()) {
            Toast.makeText(context, "Arquivo JSON inválido ou corrompido.", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val existing = repository.getOcorrenciaById(exportModel.header.talao)
            
            CoroutineScope(Dispatchers.Main).launch {
                if (existing != null) {
                    AlertDialog.Builder(context)
                        .setTitle("Ocorrência já existe")
                        .setMessage("O talão ${exportModel.header.talao} já está cadastrado no seu dispositivo. O que deseja fazer?")
                        .setPositiveButton("Sobrepor") { _, _ ->
                            saveImportedOccurrence(context, exportModel, repository)
                        }
                        .setNegativeButton("Ignorar", null)
                        .setNeutralButton("Salvar Cópia") { _, _ ->
                            val newTalao = "${exportModel.header.talao}_copia"
                            val copyModel = exportModel.copy(
                                header = exportModel.header.copy(talao = newTalao),
                                pacientes = exportModel.pacientes.map { it.copy(ocorrenciaId = newTalao) },
                                veiculos = exportModel.veiculos.map { it.copy(ocorrenciaId = newTalao) }
                            )
                            saveImportedOccurrence(context, copyModel, repository)
                        }
                        .show()
                } else {
                    saveImportedOccurrence(context, exportModel, repository)
                }
            }
        }
    }

    private fun saveImportedOccurrence(
        context: Context,
        model: OcorrenciaExportModel,
        repository: OcorrenciaRepository
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Decode images and save them locally
                val newFotosUris = mutableListOf<String>()
                model.imagesBase64?.forEach { (originalPath, base64) ->
                    try {
                        val decodedBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                        val fileName = "imported_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(5)}.jpg"
                        val file = java.io.File(context.filesDir, fileName)
                        java.io.FileOutputStream(file).use { it.write(decodedBytes) }
                        newFotosUris.add(android.net.Uri.fromFile(file).toString())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val finalHeader = if (newFotosUris.isNotEmpty()) {
                    model.header.copy(fotosUrisJson = Gson().toJson(newFotosUris))
                } else {
                    model.header
                }

                repository.insertOcorrenciaComDetalhes(
                    ocorrencia = finalHeader,
                    pacientes = model.pacientes,
                    veiculos = model.veiculos
                )
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Ocorrência importada com sucesso!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Erro ao salvar ocorrência: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
