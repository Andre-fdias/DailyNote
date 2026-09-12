package com.andrefdias.dailynote.ui.screens.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrefdias.dailynote.domain.model.*


import com.andrefdias.dailynote.domain.calendar.NotificationCenter
import com.andrefdias.dailynote.domain.calendar.NotificationScheduler
import com.andrefdias.dailynote.domain.calendar.ScaleEngine
import com.andrefdias.dailynote.domain.repository.CalendarRepository
import com.andrefdias.dailynote.domain.repository.EfetivoRepository
import com.andrefdias.dailynote.domain.repository.SettingsRepository
import com.andrefdias.dailynote.domain.repository.OcorrenciaRepository
import com.andrefdias.dailynote.domain.repository.EquipeServicoRepository
import com.andrefdias.dailynote.domain.repository.ViaturaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

// ──────────────────────────────────────────────
// HOME VIEW MODEL (without Occurrences)
// ──────────────────────────────────────────────
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val efetivoRepository: EfetivoRepository,
    private val settingsRepository: SettingsRepository,
    private val ocorrenciaRepository: OcorrenciaRepository,
    private val equipeServicoRepository: EquipeServicoRepository,
    private val viaturaRepository: ViaturaRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow<LocalDate>(LocalDate.now().withDayOfMonth(1))
    val currentMonth: StateFlow<LocalDate> = _currentMonth.asStateFlow()

    private val _allEventos = MutableStateFlow<List<CalendarEvento>>(emptyList())
    val allEventos: StateFlow<List<CalendarEvento>> = _allEventos.asStateFlow()

    private val _allTarefas = MutableStateFlow<List<CalendarTarefa>>(emptyList())
    val allTarefas: StateFlow<List<CalendarTarefa>> = _allTarefas.asStateFlow()

    private val _notifications = MutableStateFlow<List<CalendarNotificacao>>(emptyList())
    val notifications: StateFlow<List<CalendarNotificacao>> = _notifications.asStateFlow()

    private val _unreadNotificationCount = MutableStateFlow(0)
    val unreadNotificationCount: StateFlow<Int> = _unreadNotificationCount.asStateFlow()

    private val _selectedEscalaFilter = MutableStateFlow<String?>(null)
    val selectedEscalaFilter: StateFlow<String?> = _selectedEscalaFilter.asStateFlow()

    private val _availableEscalas = MutableStateFlow<List<EscalaConfig>>(emptyList())
    val availableEscalas: StateFlow<List<EscalaConfig>> = _availableEscalas.asStateFlow()

    private val _previewDays = MutableStateFlow<Map<LocalDate, Map<Int, List<EquipeConfig>>>>(emptyMap())
    val previewDays: StateFlow<Map<LocalDate, Map<Int, List<EquipeConfig>>>> = _previewDays.asStateFlow()

    private val _occurrencesThisMonth = MutableStateFlow(0)
    val occurrencesThisMonth: StateFlow<Int> = _occurrencesThisMonth.asStateFlow()

    private val _occurrencesTotal = MutableStateFlow(0)
    val occurrencesTotal: StateFlow<Int> = _occurrencesTotal.asStateFlow()

    private val _occurrencesToday = MutableStateFlow(0)
    val occurrencesToday: StateFlow<Int> = _occurrencesToday.asStateFlow()

    private val _viaturasEmProntidao = MutableStateFlow<List<Viatura>>(emptyList())
    val viaturasEmProntidao: StateFlow<List<Viatura>> = _viaturasEmProntidao.asStateFlow()

    private val _evolutionData = MutableStateFlow<Map<String, Int>>(emptyMap())
    val evolutionData: StateFlow<Map<String, Int>> = _evolutionData.asStateFlow()

    private val _natureData = MutableStateFlow<Map<String, Int>>(emptyMap())
    val natureData: StateFlow<Map<String, Int>> = _natureData.asStateFlow()

    private val _hasDismissedAlertsThisSession = MutableStateFlow(false)
    val hasDismissedAlertsThisSession: StateFlow<Boolean> = _hasDismissedAlertsThisSession.asStateFlow()

    private val _showNotificationPopup = MutableStateFlow(false)
    val showNotificationPopup: StateFlow<Boolean> = _showNotificationPopup.asStateFlow()

    private val _novidadesEfetivo = MutableStateFlow<List<NovidadeEfetivo>>(emptyList())
    val novidadesEfetivo: StateFlow<List<NovidadeEfetivo>> = _novidadesEfetivo.asStateFlow()

    init {
        observeData()
        observeEscalaFilter()
        observePreviewDays()
        // Gera notificações direto no ViewModel - 100% confiável
        viewModelScope.launch(Dispatchers.IO) { gerarNotificacoesAgenda() }
        viewModelScope.launch(Dispatchers.IO) { gerarAlertasEfetivo() }
        // Popup reativo: aparece assim que notificações chegarem
        viewModelScope.launch {
            calendarRepository.getNotificacoesFlow().collect { notifs ->
                val unread = notifs.count { !it.lida }
                if (unread > 0 && !_hasDismissedAlertsThisSession.value && !_showNotificationPopup.value) {
                    _showNotificationPopup.value = true
                }
            }
        }
    }

    fun dismissNotificationPopup() {
        _showNotificationPopup.value = false
        _hasDismissedAlertsThisSession.value = true
    }

    private fun observeData() {
        viewModelScope.launch {
            calendarRepository.getAllEventosFlow().collect {
                _allEventos.value = it
            }
        }
        viewModelScope.launch {
            calendarRepository.getAllTarefasFlow().collect {
                _allTarefas.value = it
            }
        }
        viewModelScope.launch {
            calendarRepository.getNotificacoesFlow().collect {
                _notifications.value = it
                _unreadNotificationCount.value = it.count { n -> !n.lida }
            }
        }
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                efetivoRepository.getFolgas(),
                efetivoRepository.getAfastamentos(),
                efetivoRepository.getEfetivo()
            ) { folgas, afastamentos, efetivos ->
                val novidades = mutableListOf<NovidadeEfetivo>()
                val hoje = LocalDate.now()
                val mesAtual = hoje.monthValue
                
                fun extrairFolga(folga: com.andrefdias.dailynote.domain.model.FolgaMensal, mes: Int): String {
                    return when (mes) {
                        1 -> folga.janeiro; 2 -> folga.fevereiro; 3 -> folga.marco; 4 -> folga.abril
                        5 -> folga.maio; 6 -> folga.junho; 7 -> folga.julho; 8 -> folga.agosto
                        9 -> folga.setembro; 10 -> folga.outubro; 11 -> folga.novembro; 12 -> folga.dezembro
                        else -> ""
                    }
                }
                
                fun getFolgasDatas(f: com.andrefdias.dailynote.domain.model.FolgaMensal, dataRef: LocalDate): List<LocalDate> {
                    val str = extrairFolga(f, dataRef.monthValue)
                    if (str.isBlank() || str == "-") return emptyList()
                    return str.split(Regex("[^0-9]+")).mapNotNull { it.trim().toIntOrNull() }.mapNotNull {
                        try { LocalDate.of(dataRef.year, dataRef.monthValue, it) } catch (e: Exception) { null }
                    }
                }
                
                folgas.forEach { f ->
                    val nome = f.nomePadrao.takeIf { it.isNotBlank() } ?: f.re.takeIf { it.isNotBlank() } ?: "Militar"
                    val folgasMesAtual = getFolgasDatas(f, hoje)
                    val folgasProxMes = getFolgasDatas(f, hoje.plusMonths(1))
                    (folgasMesAtual + folgasProxMes).forEach { dataFolga ->
                        if (!dataFolga.isBefore(hoje) && !dataFolga.isAfter(hoje.plusDays(5))) {
                            val formattedDate = dataFolga.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"))
                            novidades.add(NovidadeEfetivo(nome, "Folga ($formattedDate)"))
                        }
                    }
                }
                
                val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val formatterTwoDigitYear = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy")
                fun parseDate(dateStr: String): LocalDate? {
                    if (dateStr.isBlank()) return null
                    val cleaned = dateStr.trim()
                    return try {
                        if (cleaned.length == 8) LocalDate.parse(cleaned, formatterTwoDigitYear)
                        else LocalDate.parse(cleaned, dateFormatter)
                    } catch (e: Exception) { null }
                }

                afastamentos.forEach { af ->
                    val inicio = parseDate(af.dataInicio)
                    val fim = parseDate(af.dataTermino)
                    if (inicio != null && fim != null) {
                        val limite = hoje.plusDays(5)
                        if (!fim.isBefore(hoje) && !inicio.isAfter(limite)) {
                            val formatterOut = java.time.format.DateTimeFormatter.ofPattern("dd/MM")
                            val strDatas = if (inicio.isEqual(fim)) inicio.format(formatterOut) else "${inicio.format(formatterOut)} a ${fim.format(formatterOut)}"
                            novidades.add(NovidadeEfetivo(af.militar, "${af.tipoAfastamento} ($strDatas)"))
                        }
                    }
                }
                
                efetivos.forEach { militar ->
                    val nome = militar.nomeDeGuerra.ifBlank { militar.nomeCompleto }
                    fun verificar(campo: String, label: String, dataStr: String) {
                        val data = parseDate(dataStr) ?: return
                        val dias = ChronoUnit.DAYS.between(hoje, data)
                        if (dias <= 30) {
                            val alertMsg = if (dias < 0) "$label VENCIDO há ${-dias}d" else "$label vence em ${dias}d"
                            novidades.add(NovidadeEfetivo(nome, alertMsg))
                        }
                    }
                    verificar("CNH", "CNH", militar.validadeCnh)
                    verificar("TOX", "Toxicológico", militar.validadeToxicologico)
                    verificar("IAS", "IAS", militar.validadeIas)
                }
                
                novidades
            }
            .catch { e ->
                com.andrefdias.dailynote.util.LogHelper.e(TAG, "Erro ao combinar efetivo: ${e.localizedMessage}", e)
                _novidadesEfetivo.value = listOf(NovidadeEfetivo("Erro", "Falha ao sincronizar dados do Efetivo."))
            }
            .collect {
                _novidadesEfetivo.value = it
            }
        }
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                ocorrenciaRepository.getAllLocalOcorrenciasFlow(),
                ocorrenciaRepository.getAllVitimasFlow()
            ) { ocorrencias, vitimas ->
                val today = LocalDate.now()
                val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                
                var totalVitimasMes = 0
                var countToday = 0
                var countMonth = 0
                
                val evolutionMap = mutableMapOf<String, Int>()
                val ocorrenciasDoMes = mutableSetOf<String>()

                ocorrencias.forEach { occ ->
                    try {
                        // Tenta extrair a data de diversas formas
                        var date: LocalDate? = null
                        val str = occ.data.trim()
                        
                        // Tenta d/M/yyyy, dd/MM/yyyy etc
                        val formatterBr = java.time.format.DateTimeFormatter.ofPattern("d/M/yyyy")
                        date = try {
                            LocalDate.parse(str, formatterBr)
                        } catch(e: Exception) {
                            try {
                                val fmt2 = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                LocalDate.parse(str, fmt2)
                            } catch(e2: Exception) {
                                try {
                                    LocalDate.parse(str) // YYYY-MM-DD
                                } catch(e3: Exception) {
                                    null
                                }
                            }
                        }
                        
                        if (date == null && str.isNotEmpty()) {
                            // Ultima tentativa: extrair os números se tiver no formato misto "04-09-2026"
                            try {
                                val parts = str.split(Regex("[/\\-]"))
                                if (parts.size == 3) {
                                    if (parts[0].length == 4) { // yyyy-mm-dd
                                        date = LocalDate.of(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
                                    } else { // dd-mm-yyyy
                                        date = LocalDate.of(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
                                    }
                                }
                            } catch (e: Exception) {}
                        }
                        
                        if (date != null) {
                            if (date.isEqual(today)) countToday++
                            
                            val thirtyDaysAgo = today.minusDays(30)
                            if (!date.isBefore(thirtyDaysAgo) && !date.isAfter(today)) {
                                countMonth++
                                ocorrenciasDoMes.add(occ.id)
                            }
                            
                            val dayKey = date.format(formatter)
                            evolutionMap[dayKey] = (evolutionMap[dayKey] ?: 0) + 1
                        }
                    } catch(e: Exception) {}
                }

                totalVitimasMes = vitimas.count { it.ocorrenciaId in ocorrenciasDoMes }
                
                _occurrencesToday.value = countToday
                _occurrencesThisMonth.value = countMonth
                _occurrencesTotal.value = totalVitimasMes 
                
                val nMap = ocorrencias.groupingBy { it.natureza }.eachCount()
                if (nMap.isNotEmpty()) {
                    val top5 = nMap.entries.sortedByDescending { it.value }.take(5)
                    _natureData.value = top5.associate { it.key to it.value }
                } else {
                    _natureData.value = emptyMap()
                }
                
                _evolutionData.value = evolutionMap.toSortedMap()
            }.collect { }
        }
        
        viewModelScope.launch {
            combine(
                equipeServicoRepository.getAllEquipesServico(),
                viaturaRepository.getAll()
            ) { equipes, viaturas ->
                val today = LocalDate.now()
                val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val todayStrIso = today.toString()
                val todayStrBr = today.format(formatter)
                val equipesHoje = equipes.filter { it.data == todayStrIso || it.data == todayStrBr }
                if (equipesHoje.isNotEmpty()) {
                    val viaturasHojeIds = equipesHoje.flatMap { eq -> eq.viaturas.map { it.viaturaId } }.toSet()
                    viaturas.filter { it.id in viaturasHojeIds }
                } else {
                    emptyList()
                }
            }.collect {
                _viaturasEmProntidao.value = it
            }
        }
    }

    private fun observeEscalaFilter() {
        viewModelScope.launch {
            settingsRepository.activeCalendarFilterFlow.collect { filter ->
                _selectedEscalaFilter.value = if (filter == "Todos") null else filter
            }
        }
    }

    private fun observePreviewDays() {
        viewModelScope.launch {
            combine(
                _selectedEscalaFilter,
                _currentMonth,
                calendarRepository.getEscalasFlow(),
                calendarRepository.getEquipesFlow()
            ) { filter, month, scales, teams ->
                _availableEscalas.value = scales
                if (filter == "NONE") {
                    emptyMap()
                } else {
                    val filteredScales = if (filter == null) scales else scales.filter { it.id == filter }
                    val filteredTeams = if (filter == null) teams else teams.filter { it.escalaId == filter }
                    ScaleEngine.getPrecomputedMonthScales(month, filteredScales, filteredTeams)
                }
            }.collect { _previewDays.value = it }
        }
    }

    // ── Navigation ──────────────────────────────
    fun selectDate(date: LocalDate) { _selectedDate.value = date }
    fun nextMonth() { _currentMonth.value = _currentMonth.value.plusMonths(1) }
    fun previousMonth() { _currentMonth.value = _currentMonth.value.minusMonths(1) }
    fun dismissAlertsPermanently() { _hasDismissedAlertsThisSession.value = true }

    fun setEscalaFilter(filter: String?) {
        viewModelScope.launch { settingsRepository.setActiveCalendarFilter(filter ?: "Todos") }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _isRefreshing.value = true
            kotlinx.coroutines.delay(800)
            _isRefreshing.value = false
        }
    }

    // ── Eventos ──────────────────────────────────
    fun addEvento(
        titulo: String, descricao: String = "", data: LocalDate,
        hora: String? = null, local: String? = null, cor: String = "#3B82F6",
        escalaId: String? = null, lembreteMinutos: Int? = null
    ) {
        if (titulo.isBlank()) return
        viewModelScope.launch {
            calendarRepository.saveEvento(
                CalendarEvento(
                    id = UUID.randomUUID().toString(),
                    titulo = titulo.trim(),
                    descricao = descricao,
                    data = data.toString(),
                    hora = hora,
                    local = local,
                    categoria = CategoriaEvento.PERSONALIZADO,
                    cor = cor,
                    recorrencia = RecorrenciaTipo.NUNCA,
                    lembreteMinutos = lembreteMinutos ?: 0,
                    escalaId = escalaId
                )
            )
        }
    }

    fun updateEvento(evento: CalendarEvento) {
        viewModelScope.launch { calendarRepository.saveEvento(evento) }
    }

    fun deleteEvento(id: String) {
        viewModelScope.launch { calendarRepository.deleteEvento(id) }
    }

    // ── Tarefas ──────────────────────────────────
    fun addTarefa(
        titulo: String, descricao: String = "", data: LocalDate,
        hora: String? = null, prioridade: PrioridadeTarefa = PrioridadeTarefa.MEDIA,
        categoria: String = "Operacional", cor: String = "#10B981", escalaId: String? = null,
        subtarefas: List<SubtarefaInput> = emptyList()
    ) {
        if (titulo.isBlank()) return
        viewModelScope.launch {
            calendarRepository.saveTarefa(
                CalendarTarefa(
                    id = UUID.randomUUID().toString(),
                    titulo = titulo.trim(),
                    descricao = descricao,
                    data = data.toString(),
                    hora = hora,
                    prioridade = prioridade,
                    status = StatusTarefa.PENDENTE,
                    categoria = categoria,
                    responsavel = null,
                    escalaId = escalaId,
                    checklist = subtarefas.map { 
                        ChecklistItem(id = it.id, titulo = it.titulo, concluido = it.concluida, level = it.level)
                    }
                )
            )
        }
    }

    fun toggleTarefa(tarefa: CalendarTarefa) {
        viewModelScope.launch {
            val newStatus = if (tarefa.status == StatusTarefa.CONCLUIDA) StatusTarefa.PENDENTE else StatusTarefa.CONCLUIDA
            calendarRepository.saveTarefa(tarefa.copy(status = newStatus))
        }
    }

    fun updateTarefa(tarefa: CalendarTarefa) {
        viewModelScope.launch { calendarRepository.saveTarefa(tarefa) }
    }

    fun deleteTarefa(id: String) {
        viewModelScope.launch { calendarRepository.deleteTarefa(id) }
    }

    // ── Notifications ────────────────────────────
    fun markAllNotificacoesAsRead() {
        viewModelScope.launch { calendarRepository.markAllAsRead() }
    }

    fun deleteNotificacao(id: String) {
        viewModelScope.launch { calendarRepository.deleteNotificacao(id) }
    }

    fun clearAllNotificacoes() {
        viewModelScope.launch { calendarRepository.clearAllNotificacoes() }
    }

    // ── Geração direta de notificações ───────────────────────────────────────

    private suspend fun gerarNotificacoesAgenda() {
        try {
            val hoje = LocalDate.now()
            val amanha = hoje.plusDays(1)
            val dateFmt = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
            val timeFmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
            val agora = java.time.LocalTime.now()

            val jaGeradas = calendarRepository.getNotificacoes()
                .filter { it.data == hoje.toString() }
                .map { it.titulo }.toSet()

            fun criarNotif(titulo: String, desc: String, cat: CategoriaNotificacao, prio: PrioridadeTarefa) =
                CalendarNotificacao(
                    id = UUID.randomUUID().toString(), categoria = cat, titulo = titulo,
                    descricao = desc, data = hoje.toString(), hora = agora.format(timeFmt),
                    prioridade = prio, lida = false, origem = "AGENDA"
                )

            // Eventos de hoje
            calendarRepository.getEventosForDay(hoje.format(dateFmt)).forEach { ev ->
                val titulo = "📅 Hoje: ${ev.titulo}"
                if (titulo !in jaGeradas) {
                    val hora = ev.hora?.let { runCatching { java.time.LocalTime.parse(it) }.getOrNull() }
                    val horaStr = hora?.format(timeFmt) ?: "Dia todo"
                    val mins = hora?.let { ChronoUnit.MINUTES.between(agora, it) }
                    val prio = if (mins != null && mins in 0..120) PrioridadeTarefa.ALTA else PrioridadeTarefa.MEDIA
                    val desc = when {
                        mins != null && mins <= 60 -> "⚡ Em ${mins}min! ${ev.titulo} às $horaStr"
                        else -> "${ev.titulo} às $horaStr. ${ev.descricao}"
                    }
                    val notif = criarNotif(titulo, desc, CategoriaNotificacao.AGENDA, prio)
                    calendarRepository.saveNotificacao(notif)
                    NotificationCenter.triggerSystemNotification(context, notif)
                    NotificationScheduler.scheduleForEvento(context, ev)
                    Log.i(TAG, "🔔 Evento hoje: $titulo")
                }
            }

            // Tarefas de hoje
            calendarRepository.getTarefasForDay(hoje.format(dateFmt))
                .filter { it.status != StatusTarefa.CONCLUIDA }.forEach { tarefa ->
                    val titulo = "✅ Tarefa: ${tarefa.titulo}"
                    if (titulo !in jaGeradas) {
                        val hora = tarefa.hora?.let { runCatching { java.time.LocalTime.parse(it) }.getOrNull() }
                        val horaStr = hora?.format(timeFmt) ?: "Dia todo"
                        val notif = criarNotif(titulo, "Tarefa pendente hoje às $horaStr.", CategoriaNotificacao.TAREFAS, PrioridadeTarefa.MEDIA)
                        calendarRepository.saveNotificacao(notif)
                        NotificationCenter.triggerSystemNotification(context, notif)
                        NotificationScheduler.scheduleForTarefa(context, tarefa)
                    }
                }

            // Eventos de amanhã
            calendarRepository.getEventosForDay(amanha.format(dateFmt)).forEach { ev ->
                val titulo = "⏰ Amanhã: ${ev.titulo}"
                if (titulo !in jaGeradas) {
                    val hora = ev.hora?.let { runCatching { java.time.LocalTime.parse(it) }.getOrNull() }
                    val horaStr = hora?.format(timeFmt) ?: "Dia todo"
                    val notif = criarNotif(titulo, "Lembrete: ${ev.titulo} amanhã às $horaStr.", CategoriaNotificacao.AGENDA, PrioridadeTarefa.MEDIA)
                    calendarRepository.saveNotificacao(notif)
                    NotificationCenter.triggerSystemNotification(context, notif)
                    NotificationScheduler.scheduleForEvento(context, ev)
                }
            }
        } catch (e: Exception) { Log.e(TAG, "Erro ao gerar notificações da agenda", e) }
    }

    private suspend fun gerarAlertasEfetivo() {
        try {
            val hoje = LocalDate.now()
            val timeFmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
            val agora = java.time.LocalTime.now()
            val dateFmt2d = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val dateFmt2s = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy")

            fun parseDate(s: String): LocalDate? {
                if (s.isBlank()) return null
                return runCatching { LocalDate.parse(s.trim(), dateFmt2d) }.getOrNull()
                    ?: runCatching { LocalDate.parse(s.trim(), dateFmt2s) }.getOrNull()
            }

            val jaGeradas = calendarRepository.getNotificacoes()
                .filter { it.data == hoje.toString() && it.categoria == CategoriaNotificacao.EFETIVO }
                .map { it.titulo }.toSet()

            val efetivos = try {
                efetivoRepository.getEfetivo().first()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao obter efetivo para alertas: ${e.localizedMessage}")
                emptyList()
            }
            Log.d(TAG, "👮 Efetivos para checar: ${efetivos.size}")

            efetivos.forEach { militar ->
                val nome = militar.nomeDeGuerra.ifBlank { militar.nomeCompleto }

                suspend fun verificar(campo: String, label: String, dataStr: String) {
                    val data = parseDate(dataStr) ?: return
                    val dias = ChronoUnit.DAYS.between(hoje, data)
                    val (titulo, desc, prio) = when {
                        dias < 0   -> Triple("🔴 $label VENCIDO: $nome", "$label venceu há ${-dias}d ($dataStr). Regularize!", PrioridadeTarefa.ALTA)
                        dias <= 30 -> Triple("🟡 $label vence em ${dias}d: $nome", "$label vence em $dias dias ($dataStr).", PrioridadeTarefa.ALTA)
                        dias <= 90 -> Triple("🔵 $label: $nome", "$label vence em $dias dias ($dataStr).", PrioridadeTarefa.MEDIA)
                        else -> return
                    }
                    if (titulo !in jaGeradas) {
                        val notif = CalendarNotificacao(
                            id = UUID.randomUUID().toString(), categoria = CategoriaNotificacao.EFETIVO,
                            titulo = titulo, descricao = desc, data = hoje.toString(),
                            hora = agora.format(timeFmt), prioridade = prio, lida = false, origem = "EFETIVO"
                        )
                        calendarRepository.saveNotificacao(notif)
                        NotificationCenter.triggerSystemNotification(context, notif)
                        Log.i(TAG, "👮 Alerta efetivo: $titulo")
                    }
                }

                verificar("CNH", "CNH", militar.validadeCnh)
                verificar("TOX", "Tox. Sanguíneo", militar.validadeToxicologico)
                verificar("IAS", "IAS", militar.validadeIas)
            }
        } catch (e: Exception) { Log.e(TAG, "Erro ao gerar alertas do efetivo", e) }
    }
}

data class NovidadeEfetivo(val militar: String, val motivo: String)

