package com.andrefdias.dailynote

import android.net.Uri
import android.net.TestUri
import com.andrefdias.dailynote.domain.model.*
import com.andrefdias.dailynote.domain.repository.*
import com.andrefdias.dailynote.ui.screens.occurrence.*
import com.andrefdias.dailynote.ui.screens.occurrence.document.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class PersonIdentificationTest {

    private lateinit var fakeRepository: FakeOcorrenciaRepository
    private lateinit var fakeOcrService: FakeOcrService
    private lateinit var fakeCameraService: FakeCameraCaptureService
    private lateinit var viewModel: PersonIdentificationViewModel

    @Before
    fun setUp() {
        fakeRepository = FakeOcorrenciaRepository()
        fakeOcrService = FakeOcrService()
        fakeCameraService = FakeCameraCaptureService()
        viewModel = PersonIdentificationViewModel(fakeRepository, fakeOcrService, fakeCameraService)
        viewModel.testScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined)
        viewModel.setOccurrenceId("test-occ-id")
    }

    @Test
    fun testSelectDocumentType() {
        viewModel.selectDocumentType(DocumentType.RG)
        val state = viewModel.state.value
        assertEquals(DocumentType.RG, state.selectedType)
    }

    @Test
    fun testChangeDocumentTypeResetsFields() {
        viewModel.selectDocumentType(DocumentType.CNH)
        viewModel.updateCnhState(CnhDocumentState(registro = "12345"))
        
        viewModel.resetSelection()
        val state = viewModel.state.value
        assertNull(state.selectedType)
        assertEquals("", state.cnhState.registro)
    }

    @Test
    fun testRequiredFieldValidation() {
        viewModel.selectDocumentType(DocumentType.OAB)
        viewModel.updateOabState(OabDocumentState(nome = "", numero = "123", uf = ""))

        val isValid = viewModel.validateForm()
        assertFalse(isValid)
        assertEquals("Campo obrigatório", viewModel.state.value.validationErrors["nome"])
        assertEquals("UF deve ter 2 letras", viewModel.state.value.validationErrors["uf"])
    }

    @Test
    fun testCpfValidation() {
        viewModel.selectDocumentType(DocumentType.CPF)
        viewModel.updateCpfState(CpfDocumentState(nome = "José da Silva", cpf = "12345678901"))
        assertFalse(viewModel.validateForm())
        assertEquals("CPF inválido", viewModel.state.value.validationErrors["cpf"])
    }

    @Test
    fun testPlacaValidation() {
        viewModel.selectDocumentType(DocumentType.CRLV)
        viewModel.updateCrlvState(CrlvDocumentState(placa = "ABC-123"))
        assertFalse(viewModel.validateForm())
        assertNotNull(viewModel.state.value.validationErrors["placa"])
    }

    @Test
    fun testOcrFieldMapping() = runBlocking {
        viewModel.selectDocumentType(DocumentType.OAB)
        val dummyUri = TestUri()
        
        val enriched = mapOf(
            "nome" to "Cláudia Rezende",
            "numero" to "45290",
            "uf" to "SP"
        )
        fakeOcrService.ocrResultToReturn = Result.success(
            OcrDocumentResult("OAB", "Raw Text OAB Cláudia Rezende 45290 SP", enriched, emptyMap())
        )

        viewModel.processOcr(dummyUri)
        
        val state = viewModel.state.value
        assertEquals("Cláudia Rezende", state.oabState.nome)
        assertEquals("45290", state.oabState.numero)
        assertEquals("SP", state.oabState.uf)
    }

    @Test
    fun testSaveDocumentToRepository() = runBlocking {
        viewModel.selectDocumentType(DocumentType.OAB)
        viewModel.updateOabState(OabDocumentState(nome = "Marcos Oliveira", numero = "987654", uf = "RJ"))

        var saved = false
        viewModel.saveDocument {
            saved = true
        }

        assertTrue(saved)
        assertEquals(1, fakeRepository.addedDocuments.size)
        val savedDoc = fakeRepository.addedDocuments[0]
        assertEquals("RJ", savedDoc.dadosEstruturados["uf"])
        assertEquals("987654", savedDoc.dadosEstruturados["numero"])
        assertEquals("OAB", savedDoc.tipo)
        
        assertEquals(1, fakeRepository.addedPeople.size)
        assertEquals("Marcos Oliveira", fakeRepository.addedPeople[0].nome)
    }

    @Test
    fun testRgParser() {
        val rawFields = mapOf(
            "nome" to "Roberto Ramos",
            "rg" to "12.345.678-9",
            "cpf" to "111.222.333-44",
            "nascimento" to "10/05/1985",
            "mae" to "Maria Ramos",
            "uf" to "MG"
        )
        val ocrResult = OcrDocumentResult("RG", "Full OCR text", rawFields, emptyMap())
        val rgState = RgParser.parse(ocrResult)
        
        assertEquals("Roberto Ramos", rgState.nome)
        assertEquals("12.345.678-9", rgState.rg)
        assertEquals("111.222.333-44", rgState.cpf)
        assertEquals("10/05/1985", rgState.nascimento)
        assertEquals("Maria Ramos", rgState.mae)
        assertEquals("MG", rgState.uf)
    }

    // Helper Fakes
    class FakeOcorrenciaRepository : OcorrenciaRepository {
        val addedDocuments = mutableListOf<Documento>()
        
        override suspend fun addDocumento(documento: Documento): Result<Documento> {
            addedDocuments.add(documento)
            return Result.success(documento)
        }
        
        val addedPeople = mutableListOf<Pessoa>()

        override suspend fun salvarPessoaEDocumento(pessoa: Pessoa, documento: Documento): Result<String> {
            addedPeople.add(pessoa)
            addedDocuments.add(documento)
            return Result.success(pessoa.id ?: UUID.randomUUID().toString())
        }
        
        override suspend fun createOcorrencia(ocorrencia: Ocorrencia): Result<Ocorrencia> = Result.failure(Exception())
        override suspend fun getOcorrenciaById(id: String): Result<Ocorrencia> = Result.failure(Exception())
        override fun getOcorrencias(): Flow<List<Ocorrencia>> = flowOf(emptyList())
        override suspend fun addVeiculoEnvolvido(veiculo: VeiculoEnvolvido): Result<VeiculoEnvolvido> = Result.failure(Exception())
        override suspend fun addVitima(vitima: Vitima): Result<Vitima> = Result.failure(Exception())
        override suspend fun getOrgaosApoio(): Result<List<OrgaoApoio>> = Result.failure(Exception())
        override suspend fun vincularOrgaoApoio(ocorrenciaId: String, orgaoId: String): Result<Unit> = Result.failure(Exception())
        override suspend fun desvincularOrgaoApoio(ocorrenciaId: String, orgaoId: String): Result<Unit> = Result.failure(Exception())
        override suspend fun upsertPessoa(pessoa: Pessoa): Result<Pessoa> = Result.failure(Exception())
        override suspend fun getPessoasDaOcorrencia(ocorrenciaId: String): Result<List<Pessoa>> = Result.failure(Exception())
        override suspend fun getDocumentosDaOcorrencia(ocorrenciaId: String): Result<List<Documento>> = Result.failure(Exception())
        override suspend fun uploadFile(bucket: String, path: String, bytes: ByteArray): Result<String> = Result.failure(Exception())
        override suspend fun vincularOrgaoApoioDetalhado(ocorrenciaId: String, orgaoId: String, viatura: String?, encarregado: String?, descricaoOutros: String?): Result<Unit> = Result.failure(Exception())
        override suspend fun addViatura(viatura: Viatura): Result<Viatura> = Result.failure(Exception())
        override suspend fun salvarViaturaComMilitares(viatura: Viatura, militares: List<Militar>): Result<Viatura> = Result.failure(Exception())
        override suspend fun deleteViatura(viaturaId: String): Result<Unit> = Result.failure(Exception())
        override suspend fun addMilitar(militar: Militar): Result<Militar> = Result.failure(Exception())
        override suspend fun deleteMilitar(militarId: String): Result<Unit> = Result.failure(Exception())
        override suspend fun moveMilitar(militarId: String, newViaturaId: String): Result<Unit> = Result.failure(Exception())
        override suspend fun getViaturasDaOcorrencia(ocorrenciaId: String): Result<List<Viatura>> = Result.failure(Exception())
        override suspend fun addViaturaMaster(viatura: ViaturaMaster): Result<ViaturaMaster> = Result.failure(Exception())
        override suspend fun getViaturasMaster(): Result<List<ViaturaMaster>> = Result.failure(Exception())
        override suspend fun addMilitarMaster(militar: MilitarMaster): Result<MilitarMaster> = Result.failure(Exception())
        override suspend fun getMilitaresMaster(): Result<List<MilitarMaster>> = Result.failure(Exception())
        override suspend fun addVeiculoMaster(veiculo: VeiculoMaster): Result<VeiculoMaster> = Result.failure(Exception())
        override suspend fun getVeiculosMaster(): Result<List<VeiculoMaster>> = Result.failure(Exception())
        override suspend fun logAudit(log: AuditLog): Result<Unit> = Result.failure(Exception())
        override suspend fun addTimelineEvent(event: TimelineEvent): Result<TimelineEvent> = Result.failure(Exception())
        override suspend fun getTimelineEvents(ocorrenciaId: String): Result<List<TimelineEvent>> = Result.failure(Exception())
        override suspend fun addEvidencia(evidencia: Evidencia): Result<Evidencia> = Result.failure(Exception())
        override suspend fun getEvidencias(ocorrenciaId: String): Result<List<Evidencia>> = Result.failure(Exception())
        override suspend fun deleteDocumento(id: String): Result<Unit> = Result.failure(Exception())
        override suspend fun deleteVeiculo(id: String): Result<Unit> = Result.failure(Exception())
        override suspend fun deleteEvidencia(id: String): Result<Unit> = Result.failure(Exception())
        override suspend fun deleteOcorrencia(id: String): Result<Unit> = Result.failure(Exception())
    }

    class FakeOcrService : OcrService {
        var ocrResultToReturn: Result<OcrDocumentResult> = Result.failure(Exception("Not set"))
        override suspend fun recognizeText(imageUri: Uri): Result<OcrDocumentResult> = ocrResultToReturn
    }

    class FakeCameraCaptureService : CameraCaptureService {
        override fun createPhotoUri(): Uri = TestUri()
        override fun launchCamera(launcher: androidx.activity.result.ActivityResultLauncher<Uri>, uri: Uri) {}
    }
}
