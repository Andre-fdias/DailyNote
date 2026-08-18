package com.andrefdias.dailynote.ui.screens.resumo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrefdias.dailynote.domain.model.EquipeServico
import com.andrefdias.dailynote.domain.repository.EquipeServicoRepository
import com.andrefdias.dailynote.domain.repository.MilitarRepository
import com.andrefdias.dailynote.domain.repository.ViaturaRepository
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
    militarRepository: MilitarRepository
) : ViewModel() {

    val equipesHistorico: StateFlow<List<EquipeServico>> = combine(
        equipeServicoRepository.getAllEquipesServico(),
        viaturaRepository.getAll(),
        militarRepository.getAll()
    ) { equipes, viaturas, militares ->
        equipes.map { eq ->
            val viaturasCompletas = eq.viaturas.map { ev ->
                ev.copy(
                    viatura = viaturas.find { it.id == ev.viaturaId },
                    motorista = militares.find { it.id == ev.motoristaId },
                    comandante = militares.find { it.id == ev.comandanteId },
                    auxiliares = ev.auxiliaresIds.mapNotNull { aid -> militares.find { it.id == aid } }
                )
            }
            eq.copy(viaturas = viaturasCompletas)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
}
