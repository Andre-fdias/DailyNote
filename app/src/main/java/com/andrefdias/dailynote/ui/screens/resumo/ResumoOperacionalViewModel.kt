package com.andrefdias.dailynote.ui.screens.resumo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrefdias.dailynote.domain.model.EquipeServico
import com.andrefdias.dailynote.domain.repository.EquipeServicoRepository
import com.andrefdias.dailynote.domain.repository.MilitarRepository
import com.andrefdias.dailynote.domain.repository.ViaturaRepository
import com.andrefdias.dailynote.domain.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ResumoOperacionalViewModel @Inject constructor(
    equipeServicoRepository: EquipeServicoRepository,
    viaturaRepository: ViaturaRepository,
    militarRepository: MilitarRepository,
    calendarRepository: CalendarRepository
) : ViewModel() {

    val equipesHistorico: StateFlow<List<EquipeServico>> = combine(
        equipeServicoRepository.getAllEquipesServico(),
        viaturaRepository.getAll(),
        militarRepository.getAll(),
        calendarRepository.getEquipesFlow()
    ) { equipes, viaturas, militares, equipesConfig ->
        equipes.map { eq ->
            val viaturasCompletas = eq.viaturas.map { ev ->
                val escaladosCompletos = ev.militaresEscalados.map { me ->
                    me.copy(militar = militares.find { it.id == me.militarId })
                }
                ev.copy(
                    viatura = viaturas.find { it.id == ev.viaturaId },
                    militaresEscalados = escaladosCompletos
                )
            }
            eq.copy(
                viaturas = viaturasCompletas,
                equipeConfig = equipesConfig.find { it.id == eq.escalaId }
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
}
