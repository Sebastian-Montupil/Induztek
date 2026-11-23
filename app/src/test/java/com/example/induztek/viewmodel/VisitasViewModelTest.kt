
package com.example.induztek.viewmodel

import kotlinx.coroutines.flow.map
import com.example.induztek.data.local.dao.VisitaDao
import com.example.induztek.data.local.entity.VisitaEntity
import com.example.induztek.data.local.entity.VisitaTecnicoEntity
import com.example.induztek.data.repository.SesionRepository
import com.example.induztek.data.repository.VisitaRepository
import com.example.induztek.model.Rol
import com.example.induztek.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VisitasViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var dao: VisitaDaoPrueba

    @Before
    fun preparar() {
        Dispatchers.setMain(dispatcher)
        SesionRepository.cerrarSesion()
        dao = VisitaDaoPrueba()
    }

    @After
    fun finalizar() {
        SesionRepository.cerrarSesion()
        Dispatchers.resetMain()
    }

    private fun iniciarSesion(
        rol: Rol = Rol.TECNICO,
        activo: Boolean = true,
        clienteId: String? = null
    ) {
        SesionRepository.establecerUsuario(
            Usuario(
                id = "USR-01",
                nombreCompleto = "Usuario Prueba",
                rut = "11111111-1",
                correo = "prueba@example.com",
                rol = rol,
                activo = activo,
                clienteId = clienteId
            )
        )
    }

    private fun visita(
        id: Long,
        clienteId: String = "CLI-01"
    ) = VisitaEntity(
        id = id,
        clienteId = clienteId,
        instalacionId = "INS-01",
        fechaMillis = 1000L,
        numeroOt = "OT-$id",
        tipoVisita = "SOLO_PRUEBAS",
        ingenieroCargoId = "ING-01",
        observaciones = "Visita de prueba"
    )

    @Test
    fun estadoInicial_indicaCarga() {
        val viewModel = VisitasViewModel(VisitaRepository(dao))

        assertTrue(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.visitas.isEmpty())
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun usuarioActivo_puedeVerVisitas() = runTest(dispatcher) {
        iniciarSesion()
        dao.actualizarVisitas(
            listOf(visita(1), visita(2))
        )

        val viewModel = VisitasViewModel(VisitaRepository(dao))

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(2, viewModel.uiState.value.visitas.size)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun listadoVacio_finalizaCarga() = runTest(dispatcher) {
        iniciarSesion()

        val viewModel = VisitasViewModel(VisitaRepository(dao))

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.visitas.isEmpty())
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun actualizarVisitas_refrescaListado() = runTest(dispatcher) {
        iniciarSesion()

        val viewModel = VisitasViewModel(VisitaRepository(dao))

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        dao.actualizarVisitas(listOf(visita(3)))
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.visitas.size)
        assertEquals(3L, viewModel.uiState.value.visitas.first().id)
    }

    @Test
    fun usuarioSinSesion_noVeVisitas() = runTest(dispatcher) {
        dao.actualizarVisitas(listOf(visita(1)))

        val viewModel = VisitasViewModel(VisitaRepository(dao))

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.visitas.isEmpty())
    }

    @Test
    fun usuarioInactivo_noVeVisitas() = runTest(dispatcher) {
        iniciarSesion(activo = false)
        dao.actualizarVisitas(listOf(visita(1)))

        val viewModel = VisitasViewModel(VisitaRepository(dao))

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.visitas.isEmpty())
    }

    @Test
    fun clienteSinId_noVeVisitas() = runTest(dispatcher) {
        iniciarSesion(rol = Rol.CLIENTE)

        dao.actualizarVisitas(listOf(visita(1)))

        val viewModel = VisitasViewModel(VisitaRepository(dao))

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.visitas.isEmpty())
    }

    @Test
    fun clienteVeSoloSusVisitas() = runTest(dispatcher) {
        iniciarSesion(
            rol = Rol.CLIENTE,
            clienteId = "CLI-01"
        )

        dao.actualizarVisitas(
            listOf(
                visita(1, "CLI-01"),
                visita(2, "CLI-02")
            )
        )

        val viewModel = VisitasViewModel(VisitaRepository(dao))

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.visitas.size)
        assertEquals(
            "CLI-01",
            viewModel.uiState.value.visitas.first().clienteId
        )
    }

    @Test
    fun errorAlConsultar_muestraMensaje() = runTest(dispatcher) {
        iniciarSesion()
        dao.errorConsulta = IllegalStateException("Fallo simulado")

        val viewModel = VisitasViewModel(VisitaRepository(dao))

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.visitas.isEmpty())
        assertTrue(
            viewModel.uiState.value.errorMessage
                ?.contains("Fallo simulado") == true
        )
    }

    private class VisitaDaoPrueba : VisitaDao {

        private val flujoVisitas =
            MutableStateFlow<List<VisitaEntity>>(emptyList())

        var errorConsulta: Exception? = null

        fun actualizarVisitas(visitas: List<VisitaEntity>) {
            flujoVisitas.value = visitas
        }

        override suspend fun insertarVisita(
            visita: VisitaEntity
        ): Long = 1L

        override suspend fun insertarVisitaTecnicos(
            visitaTecnicos: List<VisitaTecnicoEntity>
        ) = Unit

        override fun obtenerVisitas(): Flow<List<VisitaEntity>> {
            val error = errorConsulta

            return if (error != null) {
                flow { throw error }
            } else {
                flujoVisitas
            }
        }

        override fun obtenerVisitasPorCliente(
            clienteId: String
        ): Flow<List<VisitaEntity>> {
            val error = errorConsulta

            return if (error != null) {
                flow { throw error }
            } else {
                flujoVisitas.map { visitas ->
                    visitas.filter { it.clienteId == clienteId }
                }
            }
        }

        override suspend fun obtenerVisitaPorId(
            id: Long
        ): VisitaEntity? {
            return flujoVisitas.value.firstOrNull { it.id == id }
        }

        override suspend fun obtenerTecnicosDeVisita(
            visitaId: Long
        ): List<String> = emptyList()
    }
}
