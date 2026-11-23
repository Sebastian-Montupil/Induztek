package com.example.induztek.viewmodel

import com.example.induztek.data.repository.SesionRepository
import com.example.induztek.network.CambiarContrasenaDto
import com.example.induztek.network.UsuarioApi
import com.example.induztek.network.UsuarioResponseDto
import com.example.induztek.network.UsuarioUpdateDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class IngresoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeUsuarioApi(
        var perfilResponse: UsuarioResponseDto = UsuarioResponseDto(),
        var shouldThrowHttp: Int? = null,
        var shouldThrowIo: Boolean = false,
    ) : UsuarioApi {
        override suspend fun obtenerPerfilMe(): UsuarioResponseDto {
            if (shouldThrowHttp != null) {
                throw HttpException(Response.error<Any>(shouldThrowHttp!!, "".toResponseBody()))
            }
            if (shouldThrowIo) {
                throw IOException("Error de red")
            }
            return perfilResponse
        }

        override suspend fun actualizarPerfil(id: Long, request: UsuarioUpdateDto): UsuarioResponseDto {
            return perfilResponse
        }

        override suspend fun cambiarContrasena(id: Long, request: CambiarContrasenaDto) {
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        SesionRepository.cerrarSesion()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        SesionRepository.cerrarSesion()
    }

    @Test
    fun onCorreoYContrasenaCambian_actualizanEstado() {
        val viewModel = IngresoViewModel()
        viewModel.onCorreoCambia("test@induztek.cl")
        viewModel.onContrasenaCambia("secreto123")

        val state = viewModel.uiState.value
        assertEquals("test@induztek.cl", state.correo)
        assertEquals("secreto123", state.contrasena)
    }

    @Test
    fun iniciarSesion_camposVacios_muestraError() {
        val viewModel = IngresoViewModel()
        viewModel.onCorreoCambia("")
        viewModel.onContrasenaCambia("")

        var exitoLlamado = false
        viewModel.iniciarSesion {
            exitoLlamado = true
        }

        val state = viewModel.uiState.value
        assertNotNull(state.errorIngreso)
        assertEquals(false, exitoLlamado)
    }

    @Test
    fun iniciarSesion_exitoso_estableceUsuarioYNavega() = runTest(testDispatcher) {
        val dto = UsuarioResponseDto(id = 10, nombre = "Ana Lopez", correo = "ana@induztek.cl", rolNombre = "JEFE_TECNICO")
        val api = FakeUsuarioApi(perfilResponse = dto)
        val viewModel = IngresoViewModel(api)

        viewModel.onCorreoCambia("ana@induztek.cl")
        viewModel.onContrasenaCambia("password123")

        var navego = false
        viewModel.iniciarSesion {
            navego = true
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.cargando)
        assertNull(state.errorIngreso)
        assertEquals(true, navego)
        assertNotNull(SesionRepository.usuarioActual.value)
        assertEquals("Ana Lopez", SesionRepository.usuarioActual.value?.nombreCompleto)
    }

    @Test
    fun iniciarSesion_errorHttp401_muestraError() = runTest(testDispatcher) {
        val api = FakeUsuarioApi(shouldThrowHttp = 401)
        val viewModel = IngresoViewModel(api)

        viewModel.onCorreoCambia("ana@induztek.cl")
        viewModel.onContrasenaCambia("claveincorrecta")

        var navego = false
        viewModel.iniciarSesion {
            navego = true
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.cargando)
        assertEquals("Correo o contraseña incorrectos.", state.errorIngreso)
        assertEquals(false, navego)
    }

    @Test
    fun iniciarSesion_errorIo_muestraError() = runTest(testDispatcher) {
        val api = FakeUsuarioApi(shouldThrowIo = true)
        val viewModel = IngresoViewModel(api)

        viewModel.onCorreoCambia("ana@induztek.cl")
        viewModel.onContrasenaCambia("password123")

        var navego = false
        viewModel.iniciarSesion {
            navego = true
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.cargando)
        assertEquals("No se pudo conectar con el servidor.", state.errorIngreso)
        assertEquals(false, navego)
    }
}
