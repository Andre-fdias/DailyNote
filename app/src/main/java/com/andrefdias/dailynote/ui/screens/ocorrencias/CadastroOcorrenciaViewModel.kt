package com.andrefdias.dailynote.ui.screens.ocorrencias

import android.location.Address
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia
import com.andrefdias.dailynote.domain.repository.CalendarRepository
import com.andrefdias.dailynote.domain.repository.EquipeServicoRepository
import com.andrefdias.dailynote.domain.repository.OcorrenciaRepository
import com.andrefdias.dailynote.domain.repository.ViaturaRepository
import com.andrefdias.dailynote.util.LocationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class CadastroOcorrenciaState(
    val isLoading: Boolean = true,
    // Automáticos
    val data: String = "",
    val hora: String = "",
    val equipeServicoId: String = "",
    val equipeServicoNome: String = "",
    
    // Lista de Viaturas ativas hoje
    val viaturasDisponiveis: List<com.andrefdias.dailynote.domain.model.EquipeViatura> = emptyList(),

    
    // Inputs do usuário
    val talao: String = "",
    val naturezaSelecionada: String = "", // código + descrição
    val viaturaSelecionadaId: String = "",
    val guarnicaoNomes: List<String> = emptyList(),
    
    // GPS
    val latitude: Double? = null,
    val longitude: Double? = null,
    val rua: String = "",
    val numero: String = "",
    val bairro: String = "",
    val cidade: String = "",
    val gpsTravado: Boolean = false,
    
    // Status Final
    val savedSuccess: Boolean = false,
    val savedOcorrenciaId: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class CadastroOcorrenciaViewModel @Inject constructor(
    private val ocorrenciaRepository: OcorrenciaRepository,
    private val calendarRepository: CalendarRepository,
    private val equipeRepository: EquipeServicoRepository,
    private val viaturaRepository: ViaturaRepository,
    private val locationHelper: LocationHelper,
    private val calendarDao: com.andrefdias.dailynote.data.local.dao.CalendarDao
) : ViewModel() {

    private val _state = MutableStateFlow(CadastroOcorrenciaState())
    val state: StateFlow<CadastroOcorrenciaState> = _state.asStateFlow()
    private var initParamsDone = false
    private val _todasViaturas = MutableStateFlow<List<com.andrefdias.dailynote.domain.model.Viatura>>(emptyList())

    init {
        preencherDadosAutomaticos()
        
        viewModelScope.launch {
            viaturaRepository.getAll().collect { viaturas ->
                _todasViaturas.value = viaturas
                if (initParamsDone) {
                    val vtrs = viaturas.map { v ->
                        com.andrefdias.dailynote.domain.model.EquipeViatura(
                            id = v.id ?: java.util.UUID.randomUUID().toString(),
                            equipeServicoId = "",
                            viaturaId = v.id ?: "",
                            militaresEscalados = emptyList(),
                            viatura = v
                        )
                    }
                    _state.value = _state.value.copy(viaturasDisponiveis = vtrs)
                }
            }
        }
    }

    private fun preencherDadosAutomaticos() {
        viewModelScope.launch {
            val locale = java.util.Locale("pt", "BR")
            val dataAtual = java.text.SimpleDateFormat("dd/MM/yyyy", locale).format(java.util.Date())
            val horaAtual = java.text.SimpleDateFormat("HH:mm", locale).format(java.util.Date())
            val dataDbFormat = java.text.SimpleDateFormat("yyyy-MM-dd", locale).format(java.util.Date())

            // Look up today's active equipe/prontidao from the calendar
            var equipeNome = ""
            try {
                val now = java.time.LocalDateTime.now()
                val dbEscalas = calendarDao.getEscalas().map {
                    com.andrefdias.dailynote.domain.model.EscalaConfig(it.id, it.nome, it.trabalhoHoras, it.descansoHoras, it.quantidadeTurnos, it.ativa, it.descricao)
                }
                val dbEquipes = calendarDao.getEquipes().map {
                    com.andrefdias.dailynote.domain.model.EquipeConfig(it.id, it.nome, it.sigla, it.corFundo, it.corTexto, it.corBorda, it.escalaId, it.dataInicial, it.ordemTurno, it.ativa, it.horaInicio, it.horaTermino)
                }
                
                val activeTeams = com.andrefdias.dailynote.domain.calendar.ScaleEngine.getActiveTeamsRightNow(now, dbEscalas, dbEquipes)
                val flatActive = activeTeams.values.flatten()
                
                if (flatActive.isNotEmpty()) {
                    val equipe = flatActive.first()
                    // Save name and color separated by pipe to persist the exact color
                    equipeNome = "${equipe.nome}|${equipe.corFundo}"
                }
            } catch (e: Exception) {
                equipeNome = ""
            }

            _state.value = _state.value.copy(
                data = dataAtual,
                hora = horaAtual,
                equipeServicoNome = equipeNome
            )
            
            initParamsDone = true
            
            if (_todasViaturas.value.isNotEmpty()) {
                val vtrs = _todasViaturas.value.map { v ->
                    com.andrefdias.dailynote.domain.model.EquipeViatura(
                        id = v.id ?: java.util.UUID.randomUUID().toString(),
                        equipeServicoId = "",
                        viaturaId = v.id ?: "",
                        militaresEscalados = emptyList(),
                        viatura = v
                    )
                }
                _state.value = _state.value.copy(viaturasDisponiveis = vtrs)
            }
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun updateTalao(talao: String) {
        _state.value = _state.value.copy(talao = talao)
    }

    fun updateNatureza(natureza: String) {
        _state.value = _state.value.copy(naturezaSelecionada = natureza)
    }

    fun updateViatura(viaturaId: String) {
        val selectedVtr = _state.value.viaturasDisponiveis.find { it.viatura?.id == viaturaId }
        val prefixo = selectedVtr?.viatura?.prefixo ?: viaturaId
        val militaresNomes = selectedVtr?.militaresEscalados?.mapNotNull { militarEscalado ->
            val gradNome = militarEscalado.militar?.let { "${it.graduacao} ${it.nomeGuerra}" } ?: ""
            val funcao = militarEscalado.funcao
            if (gradNome.isNotBlank()) "$prefixo - $funcao - $gradNome" else null
        } ?: emptyList()
        
        _state.value = _state.value.copy(
            viaturaSelecionadaId = viaturaId,
            guarnicaoNomes = militaresNomes
        )
    }

    fun travarGps(isTravado: Boolean) {
        _state.value = _state.value.copy(gpsTravado = isTravado)
    }
    
    fun updateEndereco(rua: String, numero: String, bairro: String, cidade: String) {
        _state.value = _state.value.copy(
            rua = rua,
            numero = numero,
            bairro = bairro,
            cidade = cidade
        )
    }

    fun buscarLocalizacao(context: android.content.Context) {
        if (_state.value.gpsTravado) return
        
        viewModelScope.launch {
            val location = locationHelper.getCurrentLocation(context)
            if (location != null) {
                val address = locationHelper.getAddressFromLocation(context, location.latitude, location.longitude)
                
                _state.value = _state.value.copy(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    rua = address?.thoroughfare ?: "",
                    numero = address?.subThoroughfare ?: "",
                    bairro = address?.subLocality ?: "",
                    cidade = address?.subAdminArea ?: address?.locality ?: ""
                )
            }
        }
    }

    fun salvarOcorrencia() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                val currentState = _state.value
                
                val viaturaSelected = currentState.viaturasDisponiveis.find { it.viaturaId == currentState.viaturaSelecionadaId }
                val viaturaPrefixo = viaturaSelected?.viatura?.prefixo ?: currentState.viaturaSelecionadaId

                val newOcorrencia = RoomNovaOcorrencia(
                    id = java.util.UUID.randomUUID().toString(),
                    talao = currentState.talao,
                    data = currentState.data,
                    hora = currentState.hora,
                    equipe = currentState.equipeServicoNome,
                    natureza = currentState.naturezaSelecionada,
                    viatura = viaturaPrefixo,
                    guarnicaoJson = com.google.gson.Gson().toJson(currentState.guarnicaoNomes),
                    latitude = currentState.latitude,
                    longitude = currentState.longitude,
                    rua = currentState.rua,
                    numero = currentState.numero,
                    bairro = currentState.bairro,
                    cidade = currentState.cidade,
                    fotosUrisJson = "[]",
                    createdAt = System.currentTimeMillis()
                )
                
                ocorrenciaRepository.createOcorrenciaLocal(newOcorrencia)
                _state.value = _state.value.copy(
                    isLoading = false,
                    savedSuccess = true,
                    savedOcorrenciaId = newOcorrencia.id
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
}
