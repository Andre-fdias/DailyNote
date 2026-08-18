package com.andrefdias.dailynote.data.local.entities

import androidx.room.*

@Entity(
    tableName = "ocorrencias",
    indices = [Index(value = ["protocolo"], unique = true)]
)
data class RoomOcorrencia(
    @PrimaryKey val id: String,
    val protocolo: String,
    val natureza: String,
    val latitude: Double?,
    val longitude: Double?,
    val dataHora: String, // ISO-8601 representation
    val historico: String?,
    val fotos: String, // JSON list or comma-separated paths
    val status: String = "ABERTA"
)

@Entity(
    tableName = "enderecos",
    foreignKeys = [
        ForeignKey(
            entity = RoomOcorrencia::class,
            parentColumns = ["id"],
            childColumns = ["ocorrenciaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ocorrenciaId"])]
)
data class RoomEndereco(
    @PrimaryKey val id: String,
    val ocorrenciaId: String,
    val rua: String?,
    val numero: String?,
    val bairro: String?,
    val cidade: String?,
    val uf: String?,
    val cep: String?
)

@Entity(
    tableName = "pessoas",
    indices = [Index(value = ["cpf"], unique = true)]
)
data class RoomPessoa(
    @PrimaryKey val id: String,
    val nome: String,
    val nomeSocial: String?,
    val cpf: String?,
    val rg: String?,
    val rgOrgaoEmissor: String?,
    val rgUf: String?,
    val nascimento: String?,
    val naturalidade: String?,
    val nacionalidade: String?,
    val filiacao: String?,
    val sexo: String?,
    val telefone: String?,
    val email: String?,
    val logradouro: String?,
    val numero: String?,
    val bairro: String?,
    val cidade: String?,
    val uf: String?,
    val cep: String?
)

@Entity(
    tableName = "documentos",
    foreignKeys = [
        ForeignKey(
            entity = RoomOcorrencia::class,
            parentColumns = ["id"],
            childColumns = ["ocorrenciaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RoomPessoa::class,
            parentColumns = ["id"],
            childColumns = ["pessoaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ocorrenciaId"]), Index(value = ["pessoaId"])]
)
data class RoomDocumento(
    @PrimaryKey val id: String,
    val ocorrenciaId: String,
    val pessoaId: String?,
    val tipo: String,
    val numero: String?,
    val urlImagem: String?,
    val textoOcr: String?,
    val dadosEstruturados: String, // JSON serialization of Map
    val hashArquivo: String?,
    val dataUpload: String?,
    val usuario: String?
)

@Entity(
    tableName = "veiculos_master",
    foreignKeys = [
        ForeignKey(
            entity = RoomPessoa::class,
            parentColumns = ["id"],
            childColumns = ["proprietarioId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["placa"], unique = true), Index(value = ["proprietarioId"])]
)
data class RoomVeiculoMaster(
    @PrimaryKey val id: String,
    val placa: String,
    val renavam: String?,
    val chassi: String?,
    val marca: String?,
    val modelo: String?,
    val versao: String?,
    val tipo: String?,
    val categoria: String?,
    val cor: String?,
    val anoFabricacao: Int?,
    val anoModelo: Int?,
    val proprietarioId: String?,
    val status: String
)

@Entity(
    tableName = "veiculos_ocorrencia",
    foreignKeys = [
        ForeignKey(
            entity = RoomOcorrencia::class,
            parentColumns = ["id"],
            childColumns = ["ocorrenciaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RoomVeiculoMaster::class,
            parentColumns = ["id"],
            childColumns = ["veiculoMasterId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = RoomPessoa::class,
            parentColumns = ["id"],
            childColumns = ["condutorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["ocorrenciaId"]), Index(value = ["veiculoMasterId"]), Index(value = ["condutorId"])]
)
data class RoomVeiculoOcorrencia(
    @PrimaryKey val id: String,
    val ocorrenciaId: String,
    val veiculoMasterId: String?,
    val condutorId: String?,
    val placa: String?,
    val cor: String?,
    val chassi: String?,
    val modelo: String?,
    val ano: Int?,
    val renavam: String?,
    val monobloco: String?,
    val especie: String?,
    val tipoVeiculo: String?,
    val carroceria: String?,
    val marca: String?,
    val versao: String?,
    val anoFabricacao: Int?,
    val anoModelo: Int?,
    val categoriaVeiculo: String?,
    val exercicio: String?,
    val urlCrlv: String?,
    val ocrTextoCrlv: String?,
    val ocrDadosEstruturados: String, // JSON serialization of Map
    val condutorNome: String?,
    val condutorCnh: String?,
    val condutorCategoriaCnh: String?,
    val condutorDataNascimento: String?,
    val condutorTelefone: String?
)

@Entity(
    tableName = "viaturas_master",
    indices = [Index(value = ["prefixo"], unique = true)]
)
data class RoomViaturaMaster(
    @PrimaryKey val id: String,
    val prefixo: String,
    val placa: String?,
    val tipo: String,
    val marca: String?,
    val modelo: String?,
    val quartel: String?,
    val status: String,
    val capacidade: Int?,
    val equipamentos: String // Comma separated list
)

@Entity(
    tableName = "militares_master",
    indices = [Index(value = ["re"], unique = true)]
)
data class RoomMilitarMaster(
    @PrimaryKey val id: String,
    val re: String,
    val nome: String,
    val nomeGuerra: String,
    val graduacao: String,
    val funcao: String?,
    val lotacao: String?,
    val situacao: String,
    val telefone: String?,
    val email: String?
)

@Entity(
    tableName = "viaturas_ocorrencia",
    foreignKeys = [
        ForeignKey(
            entity = RoomOcorrencia::class,
            parentColumns = ["id"],
            childColumns = ["ocorrenciaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RoomViaturaMaster::class,
            parentColumns = ["id"],
            childColumns = ["viaturaMasterId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["ocorrenciaId"]), Index(value = ["viaturaMasterId"])]
)
data class RoomViaturaOcorrencia(
    @PrimaryKey val id: String,
    val ocorrenciaId: String,
    val viaturaMasterId: String?,
    val prefixo: String,
    val tipo: String,
    val unidade: String?,
    val kmSaida: Int?,
    val kmLocal: Int?,
    val kmRetorno: Int?,
    val horaDespacho: String?,
    val horaSaida: String?,
    val horaChegada: String?,
    val horaRetorno: String?,
    val observacoes: String?
)

@Entity(
    tableName = "militares_viatura",
    foreignKeys = [
        ForeignKey(
            entity = RoomViaturaOcorrencia::class,
            parentColumns = ["id"],
            childColumns = ["viaturaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RoomMilitarMaster::class,
            parentColumns = ["id"],
            childColumns = ["militarMasterId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["viaturaId"]), Index(value = ["militarMasterId"])]
)
data class RoomMilitarViatura(
    @PrimaryKey val id: String,
    val viaturaId: String,
    val militarMasterId: String?,
    val re: String,
    val nomeGuerra: String,
    val graduacao: String,
    val funcao: String?
)

@Entity(
    tableName = "vitimas",
    foreignKeys = [
        ForeignKey(
            entity = RoomOcorrencia::class,
            parentColumns = ["id"],
            childColumns = ["ocorrenciaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RoomPessoa::class,
            parentColumns = ["id"],
            childColumns = ["pessoaId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = RoomViaturaOcorrencia::class,
            parentColumns = ["id"],
            childColumns = ["viaturaSocorroId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["ocorrenciaId"]), Index(value = ["pessoaId"]), Index(value = ["viaturaSocorroId"])]
)
data class RoomVitima(
    @PrimaryKey val id: String,
    val ocorrenciaId: String,
    val nome: String?,
    val idade: Int?,
    val lesoesAparentes: String?,
    val lesoesJson: String? = null,           // JSON serialization of List<Lesao>
    val destinoSocorro: String?,
    val quemSocorreu: String?,
    val resultadoOcorrencia: String?,
    val pessoaId: String?,
    val viaturaSocorroId: String?,
    val hospitalDestino: String?,
    val transportadoPor: String?,
    val pulso: Int?,
    val pressaoArterial: String?,
    val saturacaoO2: Int?,
    val temperatura: Double?,
    val escalaGCS: Int?,
    val gcsAberturaOcular: Int? = null,       // Abertura Ocular (1-4)
    val gcsRespostaVerbal: Int? = null,       // Resposta Verbal (1-5)
    val gcsRespostaMotora: Int? = null,       // Resposta Motora (1-6)
    val respiracao: Int? = null,              // Movimentos respiratórios por minuto
    val observacoesMedicas: String?
)

@Entity(
    tableName = "orgaos_apoio",
    indices = [Index(value = ["sigla"], unique = true)]
)
data class RoomOrgaoApoio(
    @PrimaryKey val id: String,
    val nome: String,
    val sigla: String
)

@Entity(
    tableName = "apoio_ocorrencia",
    foreignKeys = [
        ForeignKey(
            entity = RoomOcorrencia::class,
            parentColumns = ["id"],
            childColumns = ["ocorrenciaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RoomOrgaoApoio::class,
            parentColumns = ["id"],
            childColumns = ["orgaoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ocorrenciaId"]), Index(value = ["orgaoId"])]
)
data class RoomApoioOcorrencia(
    @PrimaryKey val id: String,
    val ocorrenciaId: String,
    val orgaoId: String,
    val viatura: String?,
    val encarregado: String?
)

@Entity(
    tableName = "evidencias",
    foreignKeys = [
        ForeignKey(
            entity = RoomOcorrencia::class,
            parentColumns = ["id"],
            childColumns = ["ocorrenciaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ocorrenciaId"])]
)
data class RoomEvidencia(
    @PrimaryKey val id: String,
    val ocorrenciaId: String,
    val tipo: String,
    val hashSha256: String,
    val latitude: Double?,
    val longitude: Double?,
    val dataHora: String,
    val usuario: String?,
    val urlStorage: String,
    val miniaturaUrl: String?,
    val ocrBruto: String?,
    val jsonOcr: String // JSON serialization of Map
)

@Entity(
    tableName = "timeline_eventos",
    foreignKeys = [
        ForeignKey(
            entity = RoomOcorrencia::class,
            parentColumns = ["id"],
            childColumns = ["ocorrenciaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ocorrenciaId"])]
)
data class RoomTimelineEvento(
    @PrimaryKey val id: String,
    val ocorrenciaId: String,
    val evento: String,
    val descricao: String?,
    val dataHora: String
)

@Entity(tableName = "configuracoes")
data class RoomConfiguracao(
    @PrimaryKey val id: String = "global_config",
    val tema: String = "Automático", // Claro, Escuro, Automático
    val backupAutomatico: String = "Desativado", // Diário, Semanal, Mensal, Desativado
    val backupSomenteWifi: Boolean = true,
    val backupUriSaf: String? = null,
    val ultimoBackupData: String? = null,
    val ultimoBackupTamanho: Long = 0,
    val ultimoBackupStatus: String? = null
)

@Entity(tableName = "backup_log")
data class RoomBackupLog(
    @PrimaryKey val id: String,
    val dataHora: String,
    val tipo: String,
    val status: String,
    val tamanho: Long,
    val mensagem: String?
)

@Entity(tableName = "tarefas")
data class RoomTarefa(
    @PrimaryKey val id: String,
    val titulo: String,
    val descricao: String? = null,
    val concluida: Boolean,
    val data: String, // Formato YYYY-MM-DD
    val categoria: String,
    val prioridade: String = "MEDIA", // Valores: ALTA, MEDIA, BAIXA
    val criadoEm: Long = System.currentTimeMillis(),
    val concluidoEm: Long? = null,
    val hora: String? = null, // Formato HH:MM
    val escalaId: String? = null,
    val cor: String = "#10B981"
)

@Entity(tableName = "eventos_agenda")
data class RoomEventoAgenda(
    @PrimaryKey val id: String,
    val titulo: String,
    val descricao: String?,
    val data: String, // Formato YYYY-MM-DD
    val horaInicio: String?, // Formato HH:MM
    val horaFim: String?,
    val tipo: String? = null, // TipoEvento.name
    val local: String? = null, // Endereço / Maps
    val escalaId: String? = null,
    val cor: String = "#3B82F6"
)

@Entity(tableName = "prontidao_dias")
data class RoomProntidaoDia(
    @PrimaryKey val data: String, // Formato YYYY-MM-DD
    val escala: String
)

@Entity(
    tableName = "subtarefas",
    foreignKeys = [
        ForeignKey(
            entity = RoomTarefa::class,
            parentColumns = ["id"],
            childColumns = ["tarefaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tarefaId"])]
)
data class RoomSubtarefa(
    @PrimaryKey val id: String,
    val tarefaId: String,
    val titulo: String,
    val concluida: Boolean,
    val parentId: String? = null
)

@Entity(
    tableName = "lembretes",
    indices = [Index(value = ["referenciaId"])]
)
data class RoomLembrete(
    @PrimaryKey val id: String,
    val referenciaId: String, // Pode ser o ID do Evento ou da Tarefa
    val tipoReferencia: String, // "EVENTO" ou "TAREFA"
    val minutosAntes: Int
)

data class RoomTarefaComSubtarefas(
    @Embedded val tarefa: RoomTarefa,
    @Relation(
        parentColumn = "id",
        entityColumn = "tarefaId"
    )
    val subtarefas: List<RoomSubtarefa>,
    @Relation(
        parentColumn = "id",
        entityColumn = "referenciaId"
    )
    val lembretes: List<RoomLembrete>
)

data class RoomEventoComLembretes(
    @Embedded val evento: RoomEventoAgenda,
    @Relation(
        parentColumn = "id",
        entityColumn = "referenciaId"
    )
    val lembretes: List<RoomLembrete>
)
