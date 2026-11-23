
package com.example.induztek.viewmodel

import com.example.induztek.data.local.dao.VisitaDao
import com.example.induztek.data.local.entity.VisitaEntity
import com.example.induztek.data.local.entity.VisitaTecnicoEntity
import com.example.induztek.data.repository.VisitaRepository
import com.example.induztek.model.TipoVisita
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NuevaVisitaViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var dao: VisitaDaoPrueba
    private lateinit var viewModel: NuevaVisitaViewModel

    @Before
    fun preparar() {
        Dispatchers.setMain(dispatcher)
        dao = VisitaDaoPrueba()
        viewModel = NuevaVisitaViewModel(VisitaRepository(dao))
    }

    @After
    fun finalizar() {
        Dispatchers.resetMain()
    }

    private fun completarFormulario() {
        viewModel.onClienteSeleccionado("CLI-01")
        viewModel.onInstalacionSeleccionada("INS-01")
        viewModel.onFechaSeleccionada(1000L)
        viewModel.onNumeroOtCambia("OT-001")
        viewModel.onTipoVisitaCambia(TipoVisita.SOLO_PRUEBAS)
        viewModel.onIngenieroCargoCambia("ING-01")
        viewModel.onTecnicoToggle("TEC-01")
        viewModel.onObservacionesCambia("Visita de prueba")
    }

    @Test
    fun seleccionarCliente_actualizaEstado() {
        viewModel.onClienteSeleccionado("CLI-01")

        assertEquals(
            "CLI-01",
            viewModel.uiState.value.clienteSeleccionadoId
        )
    }

    @Test
    fun cambiarCliente_limpiaInstalacion() {
        viewModel.onClienteSeleccionado("CLI-01")
        viewModel.onInstalacionSeleccionada("INS-01")
        viewModel.onClienteSeleccionado("CLI-02")

        assertEquals(
            "CLI-02",
            viewModel.uiState.value.clienteSeleccionadoId
        )
        assertNull(viewModel.uiState.value.instalacionSeleccionadaId)
    }

    @Test
    fun instalacionIncorrecta_noSeSelecciona() {
        viewModel.onClienteSeleccionado("CLI-01")
        viewModel.onInstalacionSeleccionada("INS-02")

        assertNull(viewModel.uiState.value.instalacionSeleccionadaId)
    }

    @Test
    fun instalacionCorrecta_seSelecciona() {
        viewModel.onClienteSeleccionado("CLI-01")
        viewModel.onInstalacionSeleccionada("INS-01")

        assertEquals(
            "INS-01",
            viewModel.uiState.value.instalacionSeleccionadaId
        )
    }

    @Test
    fun seleccionarFecha_actualizaEstado() {
        viewModel.onFechaSeleccionada(1000L)

        assertEquals(1000L, viewModel.uiState.value.fechaSelectedMillis)
        assertFalse(viewModel.uiState.value.fechaError)

        viewModel.onFechaSeleccionada(null)

        assertTrue(viewModel.uiState.value.fechaError)
    }

    @Test
    fun cambiarNumeroOt_actualizaEstado() {
        viewModel.onNumeroOtCambia("OT-123")

        assertEquals("OT-123", viewModel.uiState.value.numeroOt)
        assertFalse(viewModel.uiState.value.numeroOtError)
    }

    @Test
    fun seleccionarTipoVisita_actualizaEstado() {
        viewModel.onTipoVisitaCambia(TipoVisita.SOLO_PRUEBAS)

        assertEquals(
            TipoVisita.SOLO_PRUEBAS,
            viewModel.uiState.value.tipoVisita
        )
    }

    @Test
    fun seleccionarIngeniero_actualizaEstado() {
        viewModel.onIngenieroCargoCambia("ING-01")

        assertEquals(
            "ING-01",
            viewModel.uiState.value.ingenieroCargoId
        )
    }

    @Test
    fun seleccionarTecnico_agregaYQuitaTecnico() {
        viewModel.onTecnicoToggle("TEC-01")

        assertTrue(
            viewModel.uiState.value.tecnicosSeleccionadosIds
                .contains("TEC-01")
        )

        viewModel.onTecnicoToggle("TEC-01")

        assertFalse(
            viewModel.uiState.value.tecnicosSeleccionadosIds
                .contains("TEC-01")
        )
    }

    @Test
    fun cambiarObservaciones_actualizaEstado() {
        viewModel.onObservacionesCambia("Revision tecnica")

        assertEquals(
            "Revision tecnica",
            viewModel.uiState.value.observaciones
        )
    }

    @Test
    fun formularioVacio_muestraErrores() {
        viewModel.validarDatos()

        val estado = viewModel.uiState.value

        assertTrue(estado.clienteError)
        assertTrue(estado.instalacionError)
        assertTrue(estado.fechaError)
        assertTrue(estado.numeroOtError)
        assertTrue(estado.tipoVisitaError)
        assertTrue(estado.ingenieroError)
        assertFalse(estado.hasSuccessfullySaved)
        assertTrue(dao.visitas.isEmpty())
    }

    @Test
    fun guardarVisitaValida_registraCorrectamente() =
        runTest(dispatcher) {
            completarFormulario()

            viewModel.validarDatos()
            advanceUntilIdle()

            val estado = viewModel.uiState.value

            assertEquals(1, dao.visitas.size)
            assertEquals("OT-001", dao.visitas.first().numeroOt)
            assertEquals("CLI-01", dao.visitas.first().clienteId)
            assertEquals("INS-01", dao.visitas.first().instalacionId)

            assertEquals(
                listOf("TEC-01"),
                dao.obtenerTecnicosDeVisita(1L)
            )

            assertTrue(estado.hasSuccessfullySaved)
            assertTrue(estado.showSuccessDialog)
            assertFalse(estado.isSaving)
            assertNull(estado.errorPersistencia)
        }

    @Test
    fun guardarDosVeces_noDuplicaVisita() =
        runTest(dispatcher) {
            completarFormulario()

            viewModel.validarDatos()
            advanceUntilIdle()

            viewModel.validarDatos()
            advanceUntilIdle()

            assertEquals(1, dao.visitas.size)
        }

    @Test
    fun errorDeGuardado_muestraMensaje() =
        runTest(dispatcher) {
            completarFormulario()
            dao.errorRegistro = IllegalStateException("Fallo simulado")

            viewModel.validarDatos()
            advanceUntilIdle()

            val estado = viewModel.uiState.value

            assertTrue(
                estado.errorPersistencia
                    ?.contains("Fallo simulado") == true
            )
            assertFalse(estado.isSaving)
            assertFalse(estado.hasSuccessfullySaved)
        }

    @Test
    fun cerrarDialogo_ocultaDialogo() {
        viewModel.cerrarDialogo()

        assertFalse(viewModel.uiState.value.showValidDialog)
    }

    @Test
    fun cerrarDialogoExito_ocultaConfirmacion() =
        runTest(dispatcher) {
            completarFormulario()
            viewModel.validarDatos()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.showSuccessDialog)

            viewModel.cerrarDialogoExito()

            assertFalse(viewModel.uiState.value.showSuccessDialog)
        }

    private class VisitaDaoPrueba : VisitaDao {

        val visitas = mutableListOf<VisitaEntity>()
        private val asociaciones =
            mutableListOf<VisitaTecnicoEntity>()

        var errorRegistro: Exception? = null

        override suspend fun insertarVisita(
            visita: VisitaEntity
        ): Long {
            errorRegistro?.let { throw it }

            val id = visitas.size.toLong() + 1L
            visitas.add(visita.copy(id = id))
            return id
        }

        override suspend fun insertarVisitaTecnicos(
            visitaTecnicos: List<VisitaTecnicoEntity>
        ) {
            asociaciones.addAll(visitaTecnicos)
        }

        override fun obtenerVisitas(): Flow<List<VisitaEntity>> {
            return flowOf(visitas.toList())
        }

        override fun obtenerVisitasPorCliente(
            clienteId: String
        ): Flow<List<VisitaEntity>> {
            return flowOf(
                visitas.filter { it.clienteId == clienteId }
            )
        }

        override suspend fun obtenerVisitaPorId(
            id: Long
        ): VisitaEntity? {
            return visitas.firstOrNull { it.id == id }
        }

        override suspend fun obtenerTecnicosDeVisita(
            visitaId: Long
        ): List<String> {
            return asociaciones
                .filter { it.visitaId == visitaId }
                .map { it.tecnicoId }
        }
    }
}
