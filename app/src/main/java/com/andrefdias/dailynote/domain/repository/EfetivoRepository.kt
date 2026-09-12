package com.andrefdias.dailynote.domain.repository

import com.andrefdias.dailynote.data.service.GoogleSheetsService
import com.andrefdias.dailynote.domain.model.EfetivoMilitar
import com.andrefdias.dailynote.domain.model.FolgaMensal
import com.andrefdias.dailynote.util.LogHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EfetivoRepository @Inject constructor(
    private val sheetsService: GoogleSheetsService
) {
    private val spreadsheetId = "1cPKOZFAvsy0igBs3eSdxpWHuRbGIyqtcsMVNEH-CzEM"
    private val rangeEfetivo = "'Efetivo Operacional'!B6:CQ"
    private val rangeFolgas = "'Folga mensal'!B6:N"
    private val rangeAfastamentos = "'Afastamentos'!A2:F"

    fun getEfetivo(): Flow<List<EfetivoMilitar>> = flow {
        LogHelper.d("EfetivoRepository", "Iniciando download da aba Efetivo...")
        val result = sheetsService.getSpreadsheetValues(spreadsheetId, rangeEfetivo)
        result.onSuccess { valueRange ->
            val rows = valueRange.values ?: emptyList()
            val efetivoList = rows.mapNotNull { row ->
                if (row.isEmpty() || row.getOrNull(0).isNullOrBlank()) return@mapNotNull null
                
                // Helper function to safely get cell value
                fun getCell(index: Int): String = row.getOrNull(index)?.trim() ?: ""

                val cursosMap = mutableMapOf<String, String>()
                val cursoColNames = listOf(
                    "OVB PESADO", "OVB LEVE", "CMAUT", "EANX", "CSALT", "TERRESTRE", "AQUÁTICO",
                    "SAI", "ICIE", "BREC", "REM", "DREM", "AEPP", "RIT", "ATTS", "USAR", "OBE",
                    "OAE", "VEICULAR LEVE", "VEICULAR PESADO", "DRONE", "GV", "RH", "NEOPRENE",
                    "GUARTELÁ", "MOCHILA HIDRATAÇÃO", "LANTERNA TÁTICA", "MACACÃO"
                )
                
                val cursoStartIndex = 36 // Assuming OBSERVAÇÃO is index 35 (verify later)
                for (i in cursoColNames.indices) {
                    cursosMap[cursoColNames[i]] = getCell(cursoStartIndex + i)
                }

                EfetivoMilitar(
                    id = getCell(0),
                    graduacao = getCell(1),
                    re = getCell(2),
                    digito = getCell(3),
                    nomeCompleto = getCell(4),
                    nomeDeGuerra = getCell(5),
                    nomePadrao = getCell(6),
                    eb = getCell(7),
                    prontidao = getCell(8),
                    admissao = getCell(9),
                    ultimaPromocao = getCell(10),
                    emailFuncional = getCell(11),
                    emailParticular = getCell(12),
                    contato = getCell(13),
                    endereco = getCell(14),
                    cpf = getCell(15),
                    categoria = getCell(16),
                    numeroCnh = getCell(17),
                    validadeCnh = getCell(18),
                    validadeToxicologico = getCell(19),
                    voucher = getCell(20),
                    ferias1Quinzena = getCell(21),
                    ferias2Quinzena = getCell(22),
                    aniversario = getCell(23),
                    validadeIas = getCell(24),
                    ultimoEap = getCell(25),
                    turma2026 = getCell(26),
                    statusMvm = getCell(27),
                    mvm = getCell(28),
                    status = getCell(29),
                    grau5 = getCell(30),
                    grau4 = getCell(31),
                    grau3 = getCell(32),
                    grau2 = getCell(33),
                    grau1 = getCell(34),
                    statusAdicional = getCell(35),
                    observacao = getCell(36), // Adjust if index shifts
                    cursos = cursosMap
                )
            }
            LogHelper.i("EfetivoRepository", "Efetivo parseado: ${efetivoList.size} militares encontrados.")
            emit(efetivoList)
        }.onFailure {
            LogHelper.e("EfetivoRepository", "Falha ao baixar Efetivo: ${it.localizedMessage}", it)
            throw it
        }
    }

    fun getFolgas(): Flow<List<FolgaMensal>> = flow {
        LogHelper.d("EfetivoRepository", "Iniciando download da aba Folgas...")
        val result = sheetsService.getSpreadsheetValues(spreadsheetId, rangeFolgas)
        result.onSuccess { valueRange ->
            val rows = valueRange.values ?: emptyList()
            val folgasList = rows.mapNotNull { row ->
                if (row.isEmpty() || row.getOrNull(0).isNullOrBlank()) return@mapNotNull null
                
                fun getCell(index: Int): String = row.getOrNull(index)?.trim() ?: ""

                FolgaMensal(
                    re = getCell(0),
                    nomePadrao = getCell(1),
                    janeiro = getCell(2),
                    fevereiro = getCell(3),
                    marco = getCell(4),
                    abril = getCell(5),
                    maio = getCell(6),
                    junho = getCell(7),
                    julho = getCell(8),
                    agosto = getCell(9),
                    setembro = getCell(10),
                    outubro = getCell(11),
                    novembro = getCell(12),
                    dezembro = getCell(13)
                )
            }
            LogHelper.i("EfetivoRepository", "Folgas parseadas: ${folgasList.size} registros encontrados.")
            emit(folgasList)
        }.onFailure {
            LogHelper.e("EfetivoRepository", "Falha ao baixar Folgas: ${it.localizedMessage}", it)
            throw it
        }
    }

    fun getAfastamentos(): Flow<List<com.andrefdias.dailynote.domain.model.AfastamentoMilitar>> = flow {
        LogHelper.d("EfetivoRepository", "Iniciando download da aba Afastamentos...")
        val result = sheetsService.getSpreadsheetValues(spreadsheetId, rangeAfastamentos)
        result.onSuccess { valueRange ->
            val rows = valueRange.values ?: emptyList()
            val afastamentosList = rows.mapNotNull { row ->
                if (row.isEmpty() || row.getOrNull(0).isNullOrBlank()) return@mapNotNull null
                
                fun getCell(index: Int): String = row.getOrNull(index)?.trim() ?: ""

                com.andrefdias.dailynote.domain.model.AfastamentoMilitar(
                    militar = getCell(0),
                    tipoAfastamento = getCell(1),
                    diasAfastamento = getCell(2),
                    dataInicio = getCell(3),
                    dataTermino = getCell(4),
                    observacoes = getCell(5)
                )
            }
            LogHelper.i("EfetivoRepository", "Afastamentos parseados: ${afastamentosList.size} registros encontrados.")
            emit(afastamentosList)
        }.onFailure {
            LogHelper.e("EfetivoRepository", "Falha ao baixar Afastamentos: ${it.localizedMessage}", it)
            throw it
        }
    }
}
