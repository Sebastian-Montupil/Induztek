package com.example.induztek.ui.viewmodel

import com.example.induztek.data.local.entity.CalibracionEntity
import com.example.induztek.data.repository.SesionRepository
import com.example.induztek.model.Rol
import com.example.induztek.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class CalibracionViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val registros = mutableListOf<CalibracionEntity>()

    private var errorListado: Exception? = null
    private var errorRegistro: Exception? = null

    private lateinit var viewModel: CalibracionViewModel

    @Before
    fun preparar() {
        Dispatchers.setMain(dispatcher)

        registros.clear()
        errorListado = null
        errorRegistro = null

        SesionRepository.establecerUsuario(
            crearUsuario(Rol.JEFE_TECNICO)
        )

        viewModel = CalibracionViewModel(
            listarRemotas = {
                errorListado?.let { throw it }
                registros.toList()
            },
            registrarRemota = { calibracion ->
                errorRegistro?.let { throw it }

                registros.add(
                    calibracion.copy(
                        id = registros.size.toLong() + 1
                    )
                )
            }
        )
    }

    @After
    fun finalizar() {
        SesionRepository.cerrarSesion()
        Dispatchers.resetMain()
    }

    private fun crearUsuario(
        rol: Rol,
        activo: Boolean = true
    ): Usuario {
        return Usuario(
            id = "1",
            nombreCompleto = "Usuario de prueba",
            rut = "12345678-9",
            correo = "prueba@induztek.local",
            rol = rol,
            activo = activo,
            cargo = "Santiago"
        )
    }

    private fun registrarValida() {
        viewModel.registrarCalibracion(
            instrumentoId = 1L,
            fechaCalibracion = "2026-11-23",
            fechaVencimiento = "2027-11-23"
        )
    }

    @Test
    fun estadoInicialEstaVacio() {
        assertTrue(viewModel.calibraciones.value.isEmpty())
        assertFalse(viewModel.guardando.value)
        assertNull(viewModel.mensaje.value)
    }

    @Test
    fun cargaCalibracionesCorrectamente() = runTest(dispatcher) {
        registros.add(
            CalibracionEntity(
                id = 1L,
                instrumentoId = 1L,
                fechaCalibracion = "2026-06-30",
                fechaVencimiento = "2027-06-30"
            )
        )

        viewModel.cargarCalibraciones()
        advanceUntilIdle()

        assertEquals(1, viewModel.calibraciones.value.size)
        assertEquals(
            "2027-06-30",
            viewModel.calibraciones.value.first().fechaVencimiento
        )
        assertFalse(viewModel.cargando.value)
    }

    @Test
    fun actualizaListadoDeCalibraciones() = runTest(dispatcher) {
        advanceUntilIdle()

        registros.add(
            CalibracionEntity(
                id = 2L,
                instrumentoId = 1L,
                fechaCalibracion = "2026-11-23",
                fechaVencimiento = "2027-11-23"
            )
        )

        viewModel.cargarCalibraciones()
        advanceUntilIdle()

        assertEquals(1, viewModel.calibraciones.value.size)
        assertFalse(viewModel.cargando.value)
    }

    @Test
    fun usuarioSinSesionNoPuedeRegistrar() = runTest(dispatcher) {
        SesionRepository.cerrarSesion()

        registrarValida()
        advanceUntilIdle()

        assertTrue(registros.isEmpty())
        assertNotNull(viewModel.mensaje.value)
        assertFalse(viewModel.guardando.value)
    }

    @Test
    fun usuarioInactivoNoPuedeRegistrar() = runTest(dispatcher) {
        SesionRepository.establecerUsuario(
            crearUsuario(Rol.JEFE_TECNICO, activo = false)
        )

        registrarValida()
        advanceUntilIdle()

        assertTrue(registros.isEmpty())
        assertTrue(
            viewModel.mensaje.value?.contains("inactivo") == true
        )
    }

    @Test
    fun tecnicoNoPuedeRegistrar() = runTest(dispatcher) {
        SesionRepository.establecerUsuario(
            crearUsuario(Rol.TECNICO)
        )

        registrarValida()
        advanceUntilIdle()

        assertTrue(registros.isEmpty())
        assertTrue(
            viewModel.mensaje.value?.contains("Jefe técnico") == true
        )
    }

    @Test
    fun instrumentoInvalidoMuestraError() = runTest(dispatcher) {
        viewModel.registrarCalibracion(
            instrumentoId = 0L,
            fechaCalibracion = "2026-11-23",
            fechaVencimiento = "2027-11-23"
        )

        advanceUntilIdle()

        assertEquals(
            "Selecciona un instrumento válido.",
            viewModel.mensaje.value
        )
        assertTrue(registros.isEmpty())
    }

    @Test
    fun fechasVaciasMuestranError() = runTest(dispatcher) {
        viewModel.registrarCalibracion(
            instrumentoId = 1L,
            fechaCalibracion = "",
            fechaVencimiento = ""
        )

        advanceUntilIdle()

        assertNotNull(viewModel.mensaje.value)
        assertTrue(registros.isEmpty())
    }

    @Test
    fun formatoFechaIncorrectoMuestraError() = runTest(dispatcher) {
        viewModel.registrarCalibracion(
            instrumentoId = 1L,
            fechaCalibracion = "23/11/2026",
            fechaVencimiento = "2027-11-23"
        )

        advanceUntilIdle()

        assertTrue(
            viewModel.mensaje.value?.contains("formato") == true
        )
        assertTrue(registros.isEmpty())
    }

    @Test
    fun fechaInexistenteMuestraError() = runTest(dispatcher) {
        viewModel.registrarCalibracion(
            instrumentoId = 1L,
            fechaCalibracion = "2026-02-31",
            fechaVencimiento = "2027-11-23"
        )

        advanceUntilIdle()

        assertTrue(
            viewModel.mensaje.value?.contains("fecha real") == true
        )
        assertTrue(registros.isEmpty())
    }

    @Test
    fun vencimientoAnteriorMuestraError() = runTest(dispatcher) {
        viewModel.registrarCalibracion(
            instrumentoId = 1L,
            fechaCalibracion = "2027-11-23",
            fechaVencimiento = "2026-11-23"
        )

        advanceUntilIdle()

        assertTrue(
            viewModel.mensaje.value?.contains("anterior") == true
        )
        assertTrue(registros.isEmpty())
    }

    @Test
    fun calibracionValidaSeGuarda() = runTest(dispatcher) {
        registrarValida()
        advanceUntilIdle()

        assertEquals(1, registros.size)
        assertEquals(1L, registros.first().instrumentoId)
        assertEquals(
            "2026-11-23",
            registros.first().fechaCalibracion
        )
        assertEquals(
            "2027-11-23",
            registros.first().fechaVencimiento
        )

        assertEquals(
            "Calibración registrada exitosamente.",
            viewModel.mensaje.value
        )
        assertEquals(1, viewModel.calibraciones.value.size)
        assertFalse(viewModel.guardando.value)
    }

    @Test
    fun errorDeRegistroMuestraMensaje() = runTest(dispatcher) {
        errorRegistro = IllegalStateException("Error de prueba")

        registrarValida()
        advanceUntilIdle()

        assertTrue(registros.isEmpty())
        assertTrue(
            viewModel.mensaje.value?.contains("Error de prueba") == true
        )
        assertFalse(viewModel.guardando.value)
    }

    @Test
    fun errorAlCargarMuestraMensaje() = runTest(dispatcher) {
        errorListado = IllegalStateException("Sin conexión")

        viewModel.cargarCalibraciones()
        advanceUntilIdle()

        assertTrue(
            viewModel.mensaje.value?.contains("Sin conexión") == true
        )
        assertFalse(viewModel.cargando.value)
    }

    @Test
    fun limpiarMensajeEliminaNotificacion() = runTest(dispatcher) {
        registrarValida()
        advanceUntilIdle()

        assertNotNull(viewModel.mensaje.value)

        viewModel.limpiarMensaje()

        assertNull(viewModel.mensaje.value)
    }
}
