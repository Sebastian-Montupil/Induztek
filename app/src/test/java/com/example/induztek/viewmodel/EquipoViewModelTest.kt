
package com.example.induztek.viewmodel

import com.example.induztek.data.local.dao.EquipoDao
import com.example.induztek.data.local.entity.EquipoEntity
import com.example.induztek.data.repository.EquipoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
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
class EquipoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dao: EquipoDaoPrueba
    private lateinit var viewModel: EquipoViewModel

    @Before
    fun preparar() {
        Dispatchers.setMain(testDispatcher)
        dao = EquipoDaoPrueba()
        viewModel = EquipoViewModel(EquipoRepository(dao))
    }

    @After
    fun finalizar() {
        Dispatchers.resetMain()
    }

    private fun completarFormulario() {
        viewModel.onClienteSeleccionado("CLI-01")
        viewModel.onInstalacionSeleccionada("INS-01")
        viewModel.onNombreCambia("Multimetro")
        viewModel.onTipoCambia("Medicion")
        viewModel.onMarcaCambia("Fluke")
        viewModel.onModeloCambia("87V")
        viewModel.onNumeroSerieCambia("SER-001")
        viewModel.onCodigoInternoCambia("EQ-001")
        viewModel.onObservacionesCambia("Equipo de prueba")
    }

    @Test
    fun seleccionarCliente_actualizaEstado() {
        viewModel.onClienteSeleccionado("CLI-01")

        assertEquals("CLI-01", viewModel.uiState.value.clienteSeleccionadoId)
        assertNull(viewModel.uiState.value.instalacionSeleccionadaId)
    }

    @Test
    fun seleccionarOtroCliente_limpiaInstalacion() {
        viewModel.onClienteSeleccionado("CLI-01")
        viewModel.onInstalacionSeleccionada("INS-01")
        viewModel.onClienteSeleccionado("CLI-02")

        assertEquals("CLI-02", viewModel.uiState.value.clienteSeleccionadoId)
        assertNull(viewModel.uiState.value.instalacionSeleccionadaId)
        assertTrue(viewModel.uiState.value.equipos.isEmpty())
    }

    @Test
    fun instalacionInvalida_noSeSelecciona() {
        viewModel.onClienteSeleccionado("CLI-01")
        viewModel.onInstalacionSeleccionada("INS-02")

        assertNull(viewModel.uiState.value.instalacionSeleccionadaId)
    }

    @Test
    fun instalacionValida_seSelecciona() {
        viewModel.onClienteSeleccionado("CLI-01")
        viewModel.onInstalacionSeleccionada("INS-01")

        assertEquals("INS-01", viewModel.uiState.value.instalacionSeleccionadaId)
    }

    @Test
    fun cambiarCampos_actualizaFormulario() {
        completarFormulario()

        val estado = viewModel.uiState.value

        assertEquals("Multimetro", estado.nombre)
        assertEquals("Medicion", estado.tipo)
        assertEquals("Fluke", estado.marca)
        assertEquals("87V", estado.modelo)
        assertEquals("SER-001", estado.numeroSerie)
        assertEquals("EQ-001", estado.codigoInterno)
        assertEquals("Equipo de prueba", estado.observaciones)
    }

    @Test
    fun registrarSinDatos_muestraErrores() {
        viewModel.registrarEquipo()

        val estado = viewModel.uiState.value

        assertNotNull(estado.mensajeError)
        assertTrue(estado.clienteError)
        assertTrue(estado.nombreError)
        assertTrue(dao.equipos.isEmpty())
    }

    @Test
    fun registrarEquipoValido_guardaYLimpiaFormulario() =
        runTest(testDispatcher) {
            completarFormulario()

            viewModel.registrarEquipo()
            advanceUntilIdle()

            val estado = viewModel.uiState.value

            assertEquals(1, dao.equipos.size)
            assertEquals("Multimetro", dao.equipos.first().nombre)
            assertEquals("Equipo registrado exitosamente.", estado.mensajeExito)
            assertEquals("", estado.nombre)
            assertEquals("", estado.numeroSerie)
            assertFalse(estado.guardando)
        }

    @Test
    fun errorDeValidacion_muestraMensaje() =
        runTest(testDispatcher) {
            completarFormulario()
            dao.errorRegistro = IllegalArgumentException("Error de prueba")

            viewModel.registrarEquipo()
            advanceUntilIdle()

            assertEquals("Error de prueba", viewModel.uiState.value.mensajeError)
            assertFalse(viewModel.uiState.value.guardando)
        }

    @Test
    fun numeroSerieDuplicado_muestraMensaje() =
        runTest(testDispatcher) {
            completarFormulario()
            dao.errorRegistro = RuntimeException("UNIQUE constraint failed")

            viewModel.registrarEquipo()
            advanceUntilIdle()

            assertEquals(
                "El número de serie ingresado ya se encuentra registrado.",
                viewModel.uiState.value.mensajeError
            )
            assertFalse(viewModel.uiState.value.guardando)
        }

    @Test
    fun errorGenerico_muestraMensaje() =
        runTest(testDispatcher) {
            completarFormulario()
            dao.errorRegistro = RuntimeException("Sin conexion")

            viewModel.registrarEquipo()
            advanceUntilIdle()

            assertTrue(
                viewModel.uiState.value.mensajeError
                    ?.contains("Sin conexion") == true
            )
        }

    @Test
    fun observarEquipos_actualizaListado() =
        runTest(testDispatcher) {
            completarFormulario()
            advanceUntilIdle()

            dao.insertarEquipo(
                EquipoEntity(
                    clienteId = "CLI-01",
                    instalacionId = "INS-01",
                    nombre = "Termometro",
                    tipo = "Medicion",
                    marca = "Fluke",
                    modelo = "T1",
                    numeroSerie = "SER-002",
                    codigoInterno = "",
                    observaciones = ""
                )
            )

            advanceUntilIdle()

            assertEquals(1, viewModel.uiState.value.equipos.size)
            assertEquals(
                "Termometro",
                viewModel.uiState.value.equipos.first().nombre
            )
        }

    @Test
    fun limpiarMensajes_eliminaMensajes() =
        runTest(testDispatcher) {
            completarFormulario()
            viewModel.registrarEquipo()
            advanceUntilIdle()

            assertNotNull(viewModel.uiState.value.mensajeExito)

            viewModel.limpiarMensajes()

            assertNull(viewModel.uiState.value.mensajeExito)
            assertNull(viewModel.uiState.value.mensajeError)
        }


    private class EquipoDaoPrueba : EquipoDao {

        val equipos = mutableListOf<EquipoEntity>()
        var errorRegistro: Exception? = null

        private val flujoEquipos =
            MutableStateFlow<List<EquipoEntity>>(emptyList())

        override suspend fun insertarEquipo(equipo: EquipoEntity): Long {
            errorRegistro?.let { throw it }

            val id = equipos.size.toLong() + 1
            equipos.add(equipo.copy(id = id))
            flujoEquipos.value = equipos.toList()
            return id
        }

        override fun observarEquiposPorInstalacion(
            instalacionId: String
        ): Flow<List<EquipoEntity>> {
            return flujoEquipos.map { lista ->
                lista.filter { it.instalacionId == instalacionId }
            }
        }

        override suspend fun obtenerEquipoPorId(
            id: Long
        ): EquipoEntity? {
            return equipos.firstOrNull { it.id == id }
        }
    }
}