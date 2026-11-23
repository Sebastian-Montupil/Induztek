package com.example.induztek.ui.viewmodel

import com.example.induztek.data.local.dao.CalibracionDao
import com.example.induztek.data.local.dao.InstrumentoDao
import com.example.induztek.data.local.entity.CalibracionEntity
import com.example.induztek.data.local.entity.InstrumentoEntity
import com.example.induztek.data.repository.InstrumentoRepository
import com.example.induztek.data.repository.SesionRepository
import com.example.induztek.model.Rol
import com.example.induztek.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
class InstrumentoViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var dao: InstrumentoDaoPrueba
    private lateinit var viewModel: InstrumentoViewModel

    @Before
    fun preparar() {
        Dispatchers.setMain(dispatcher)

        SesionRepository.establecerUsuario(
            Usuario(
                id = "1",
                nombreCompleto = "Jefe de prueba",
                rut = "12345678-9",
                correo = "jefe@induztek.local",
                rol = Rol.JEFE_TECNICO,
                activo = true,
                cargo = "Santiago"
            )
        )

        dao = InstrumentoDaoPrueba()

        viewModel = InstrumentoViewModel(
            InstrumentoRepository(
                instrumentoDao = dao,
                calibracionDao = CalibracionDaoPrueba()
            )
        )
    }

    @After
    fun finalizar() {
        SesionRepository.cerrarSesion()
        Dispatchers.resetMain()
    }

    private fun completarFormulario() {
        viewModel.onNombreCambia("Multimetro digital")
        viewModel.onTipoInstrumentoCambia("Multímetro")
        viewModel.onMarcaCambia("Fluke")
        viewModel.onModeloCambia("87V")
        viewModel.onNumeroSerieCambia("INS-TEST-001")
        viewModel.onCodigoInternoCambia("INST-TEST")
        viewModel.onFechaVencimientoCalibracionCambia("2027-06-30")
        viewModel.onObservacionesCambia("Instrumento de prueba")
    }

    @Test
    fun formularioActualizaSusCampos() {
        completarFormulario()

        val estado = viewModel.uiState.value

        assertEquals("Multimetro digital", estado.nombre)
        assertEquals("Multímetro", estado.tipoInstrumento)
        assertEquals("Fluke", estado.marca)
        assertEquals("87V", estado.modelo)
        assertEquals("INS-TEST-001", estado.numeroSerie)
        assertEquals("INST-TEST", estado.codigoInterno)
        assertEquals("2027-06-30", estado.fechaVencimientoCalibracion)
        assertEquals("Instrumento de prueba", estado.observaciones)
    }

    @Test
    fun formularioVacioMuestraErrores() {
        viewModel.registrarInstrumento()

        val estado = viewModel.uiState.value

        assertTrue(estado.nombreError)
        assertTrue(estado.tipoInstrumentoError)
        assertTrue(estado.marcaError)
        assertTrue(estado.numeroSerieError)
        assertNotNull(estado.mensajeError)
        assertTrue(dao.instrumentos.isEmpty())
    }

    @Test
    fun instrumentoValidoSeGuardaCorrectamente() = runTest(dispatcher) {
        completarFormulario()

        viewModel.registrarInstrumento()
        advanceUntilIdle()

        val estado = viewModel.uiState.value

        assertEquals(1, dao.instrumentos.size)
        assertEquals("Fluke", dao.instrumentos.first().marca)
        assertEquals("INS-TEST-001", dao.instrumentos.first().numeroSerie)
        assertEquals("Instrumento registrado exitosamente.", estado.mensajeExito)
        assertFalse(estado.guardando)
    }

    @Test
    fun registroExitosoLimpiaFormulario() = runTest(dispatcher) {
        completarFormulario()

        viewModel.registrarInstrumento()
        advanceUntilIdle()

        val estado = viewModel.uiState.value

        assertEquals("", estado.nombre)
        assertEquals("", estado.marca)
        assertEquals("", estado.numeroSerie)
        assertEquals("", estado.observaciones)
    }

    @Test
    fun numeroSerieDuplicadoMuestraMensaje() = runTest(dispatcher) {
        completarFormulario()
        dao.errorRegistro = IllegalStateException("UNIQUE constraint failed")

        viewModel.registrarInstrumento()
        advanceUntilIdle()

        assertEquals(
            "El número de serie ingresado ya se encuentra registrado.",
            viewModel.uiState.value.mensajeError
        )
        assertFalse(viewModel.uiState.value.guardando)
    }

    @Test
    fun errorDeRegistroMuestraMensaje() = runTest(dispatcher) {
        completarFormulario()
        dao.errorRegistro = IllegalStateException("Error de base de datos")

        viewModel.registrarInstrumento()
        advanceUntilIdle()

        assertTrue(
            viewModel.uiState.value.mensajeError
                ?.contains("Error de base de datos") == true
        )
        assertFalse(viewModel.uiState.value.guardando)
    }

    @Test
    fun usuarioSinPermisoNoPuedeRegistrar() = runTest(dispatcher) {
        SesionRepository.establecerUsuario(
            Usuario(
                id = "2",
                nombreCompleto = "Tecnico de prueba",
                rut = "12345678-9",
                correo = "tecnico@induztek.local",
                rol = Rol.TECNICO,
                activo = true,
                cargo = "Santiago"
            )
        )

        completarFormulario()

        viewModel.registrarInstrumento()
        advanceUntilIdle()

        assertTrue(dao.instrumentos.isEmpty())
        assertNotNull(viewModel.uiState.value.mensajeError)
        assertFalse(viewModel.uiState.value.guardando)
    }

    @Test
    fun limpiarMensajesEliminaNotificaciones() = runTest(dispatcher) {
        completarFormulario()

        viewModel.registrarInstrumento()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.mensajeExito)

        viewModel.limpiarMensajes()

        assertNull(viewModel.uiState.value.mensajeExito)
        assertNull(viewModel.uiState.value.mensajeError)
    }

    private class InstrumentoDaoPrueba : InstrumentoDao {

        val instrumentos = mutableListOf<InstrumentoEntity>()
        var errorRegistro: Exception? = null

        private val flujo = MutableStateFlow<List<InstrumentoEntity>>(emptyList())

        override suspend fun insertarInstrumento(instrumento: InstrumentoEntity): Long {
            errorRegistro?.let { throw it }

            val id = instrumentos.size.toLong() + 1L
            instrumentos.add(instrumento.copy(id = id))
            flujo.value = instrumentos.toList()
            return id
        }

        override suspend fun actualizarInstrumento(instrumento: InstrumentoEntity): Int {
            val posicion = instrumentos.indexOfFirst { it.id == instrumento.id }

            if (posicion < 0) return 0

            instrumentos[posicion] = instrumento
            flujo.value = instrumentos.toList()
            return 1
        }

        override fun obtenerInstrumentos(): Flow<List<InstrumentoEntity>> {
            return flujo
        }

        override suspend fun obtenerInstrumentoPorId(id: Long): InstrumentoEntity? {
            return instrumentos.firstOrNull { it.id == id }
        }
    }

    private class CalibracionDaoPrueba : CalibracionDao {

        override suspend fun insertarCalibracion(calibracion: CalibracionEntity): Long {
            return 1L
        }

        override suspend fun actualizarCalibracion(calibracion: CalibracionEntity): Int {
            return 1
        }

        override fun observarCalibracionesDeInstrumento(
            instrumentoId: Long
        ): Flow<List<CalibracionEntity>> {
            return flowOf(emptyList())
        }

        override suspend fun obtenerUltimaCalibracionHastaFecha(
            instrumentoId: Long,
            fechaPrueba: String
        ): CalibracionEntity? {
            return null
        }
    }
}
