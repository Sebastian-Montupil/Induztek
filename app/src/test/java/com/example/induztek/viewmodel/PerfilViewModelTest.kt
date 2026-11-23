package com.example.induztek.viewmodel

import com.example.induztek.data.repository.SesionRepository
import com.example.induztek.model.Rol
import com.example.induztek.model.Usuario
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
class PerfilViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeUsuarioApi(
        var perfilResponse: UsuarioResponseDto = UsuarioResponseDto(),
        var shouldThrowHttp: Int? = null,
        var shouldThrowIo: Boolean = false,
        var shouldThrowGeneric: Boolean = false,
    ) : UsuarioApi {
        override suspend fun obtenerPerfilMe(): UsuarioResponseDto {
            if (shouldThrowHttp != null) {
                throw HttpException(Response.error<Any>(shouldThrowHttp!!, "".toResponseBody()))
            }
            if (shouldThrowIo) {
                throw IOException("Error de red")
            }
            if (shouldThrowGeneric) {
                throw RuntimeException("Error genérico")
            }
            return perfilResponse
        }

        override suspend fun actualizarPerfil(id: Long, request: UsuarioUpdateDto): UsuarioResponseDto {
            if (shouldThrowHttp != null) {
                throw HttpException(Response.error<Any>(shouldThrowHttp!!, "".toResponseBody()))
            }
            if (shouldThrowIo) {
                throw IOException("Error de red")
            }
            if (shouldThrowGeneric) {
                throw RuntimeException("Error genérico")
            }
            return perfilResponse.copy(nombre = request.nombre, correo = request.correo, ciudad = request.ciudad)
        }

        override suspend fun cambiarContrasena(id: Long, request: CambiarContrasenaDto) {
            if (shouldThrowHttp != null) {
                throw HttpException(Response.error<Any>(shouldThrowHttp!!, "".toResponseBody()))
            }
            if (shouldThrowIo) {
                throw IOException("Error de red")
            }
            if (shouldThrowGeneric) {
                throw RuntimeException("Error genérico")
            }
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val usuario = Usuario(
            id = "1",
            nombreCompleto = "Juan Perez",
            rut = "12345678-9",
            correo = "juan@induztek.cl",
            rol = Rol.JEFE_TECNICO,
            activo = true,
            cargo = "Santiago",
        )
        SesionRepository.establecerUsuario(usuario)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        SesionRepository.cerrarSesion()
    }

    @Test
    fun cargarPerfil_inicializaEstadoConUsuarioActual() {
        val api = FakeUsuarioApi()
        val viewModel = PerfilViewModel(api)
        val state = viewModel.uiState.value

        assertEquals("Juan Perez", state.nombre)
        assertEquals("juan@induztek.cl", state.correo)
        assertEquals("Santiago", state.ciudad)
        assertEquals("JEFE_TECNICO", state.rol)
    }

    @Test
    fun actualizarPerfil_exitoso_actualizaEstado() = runTest(testDispatcher) {
        val dto = UsuarioResponseDto(id = 1, nombre = "Juan Actualizado", correo = "juan.nuevo@induztek.cl", ciudad = "Concepción")
        val api = FakeUsuarioApi(perfilResponse = dto)
        val viewModel = PerfilViewModel(api)

        viewModel.onNombreCambia("Juan Actualizado")
        viewModel.onCorreoCambia("juan.nuevo@induztek.cl")
        viewModel.onCiudadCambia("Concepción")
        viewModel.onContrasenaActualCambia("123456")

        viewModel.actualizarPerfil()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Perfil actualizado exitosamente.", state.mensajeExito)
        assertNull(state.mensajeError)
        assertEquals("Juan Actualizado", SesionRepository.usuarioActual.value?.nombreCompleto)
    }

    @Test
    fun actualizarPerfil_errorHttp401_muestraMensaje() = runTest(testDispatcher) {
        val api = FakeUsuarioApi(shouldThrowHttp = 401)
        val viewModel = PerfilViewModel(api)

        viewModel.onNombreCambia("Juan Actualizado")
        viewModel.onCorreoCambia("juan.nuevo@induztek.cl")
        viewModel.onCiudadCambia("Concepción")

        viewModel.actualizarPerfil()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Sesión expirada o no autorizada.", state.mensajeError)
    }

    @Test
    fun actualizarPerfil_errorHttp403_muestraMensaje() = runTest(testDispatcher) {
        val api = FakeUsuarioApi(shouldThrowHttp = 403)
        val viewModel = PerfilViewModel(api)

        viewModel.onNombreCambia("Juan Actualizado")
        viewModel.onCorreoCambia("juan.nuevo@induztek.cl")
        viewModel.onCiudadCambia("Concepción")

        viewModel.actualizarPerfil()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("No tienes permiso para actualizar este perfil.", state.mensajeError)
    }

    @Test
    fun actualizarPerfil_errorHttp500_muestraMensaje() = runTest(testDispatcher) {
        val api = FakeUsuarioApi(shouldThrowHttp = 500)
        val viewModel = PerfilViewModel(api)

        viewModel.onNombreCambia("Juan Actualizado")
        viewModel.onCorreoCambia("juan.nuevo@induztek.cl")
        viewModel.onCiudadCambia("Concepción")

        viewModel.actualizarPerfil()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Ocurrió un problema en el servidor.", state.mensajeError)
    }

    @Test
    fun actualizarPerfil_errorIo_muestraMensaje() = runTest(testDispatcher) {
        val api = FakeUsuarioApi(shouldThrowIo = true)
        val viewModel = PerfilViewModel(api)

        viewModel.onNombreCambia("Juan Actualizado")
        viewModel.onCorreoCambia("juan.nuevo@induztek.cl")
        viewModel.onCiudadCambia("Concepción")

        viewModel.actualizarPerfil()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("No se pudo conectar con el servidor.", state.mensajeError)
    }

    @Test
    fun actualizarPerfil_errorGenerico_muestraMensaje() = runTest(testDispatcher) {
        val api = FakeUsuarioApi(shouldThrowGeneric = true)
        val viewModel = PerfilViewModel(api)

        viewModel.onNombreCambia("Juan Actualizado")
        viewModel.onCorreoCambia("juan.nuevo@induztek.cl")
        viewModel.onCiudadCambia("Concepción")

        viewModel.actualizarPerfil()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.mensajeError)
    }

    @Test
    fun cambiarContrasena_exitoso_actualizaEstado() = runTest(testDispatcher) {
        val api = FakeUsuarioApi()
        val viewModel = PerfilViewModel(api)

        viewModel.onContrasenaActualCambia("123456")
        viewModel.onContrasenaNuevaCambia("nuevaclave123")
        viewModel.onConfirmarContrasenaCambia("nuevaclave123")

        viewModel.cambiarContrasena()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Contraseña cambiada exitosamente.", state.mensajeExitoContrasena)
        assertNull(state.mensajeErrorContrasena)
    }

    @Test
    fun cambiarContrasena_errorHttp401_muestraMensaje() = runTest(testDispatcher) {
        val api = FakeUsuarioApi(shouldThrowHttp = 401)
        val viewModel = PerfilViewModel(api)

        viewModel.onContrasenaActualCambia("clavemal")
        viewModel.onContrasenaNuevaCambia("nuevaclave123")
        viewModel.onConfirmarContrasenaCambia("nuevaclave123")

        viewModel.cambiarContrasena()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("La contraseña actual es incorrecta o la sesión expiró.", state.mensajeErrorContrasena)
    }

    @Test
    fun cambiarContrasena_errorHttp403_muestraMensaje() = runTest(testDispatcher) {
        val api = FakeUsuarioApi(shouldThrowHttp = 403)
        val viewModel = PerfilViewModel(api)

        viewModel.onContrasenaActualCambia("123456")
        viewModel.onContrasenaNuevaCambia("nuevaclave123")
        viewModel.onConfirmarContrasenaCambia("nuevaclave123")

        viewModel.cambiarContrasena()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("No tienes permiso para realizar esta acción.", state.mensajeErrorContrasena)
    }

    @Test
    fun cambiarContrasena_errorHttp500_muestraMensaje() = runTest(testDispatcher) {
        val api = FakeUsuarioApi(shouldThrowHttp = 500)
        val viewModel = PerfilViewModel(api)

        viewModel.onContrasenaActualCambia("123456")
        viewModel.onContrasenaNuevaCambia("nuevaclave123")
        viewModel.onConfirmarContrasenaCambia("nuevaclave123")

        viewModel.cambiarContrasena()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Ocurrió un problema en el servidor.", state.mensajeErrorContrasena)
    }

    @Test
    fun cambiarContrasena_errorIo_muestraMensaje() = runTest(testDispatcher) {
        val api = FakeUsuarioApi(shouldThrowIo = true)
        val viewModel = PerfilViewModel(api)

        viewModel.onContrasenaActualCambia("123456")
        viewModel.onContrasenaNuevaCambia("nuevaclave123")
        viewModel.onConfirmarContrasenaCambia("nuevaclave123")

        viewModel.cambiarContrasena()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("No se pudo conectar con el servidor.", state.mensajeErrorContrasena)
    }

    @Test
    fun actualizarPerfil_sinSesion_muestraError() {
        SesionRepository.cerrarSesion()
        val viewModel = PerfilViewModel(FakeUsuarioApi())

        viewModel.actualizarPerfil()

        assertEquals(
            "No se encontró el ID de usuario activo.",
            viewModel.uiState.value.mensajeError
        )
    }

    @Test
    fun actualizarPerfil_camposVacios_muestraError() {
        val viewModel = PerfilViewModel(FakeUsuarioApi())

        viewModel.onNombreCambia("")
        viewModel.actualizarPerfil()

        assertEquals(
            "Nombre, correo y ciudad son obligatorios.",
            viewModel.uiState.value.mensajeError
        )
    }

    @Test
    fun cambiarContrasena_camposVacios_muestraError() {
        val viewModel = PerfilViewModel(FakeUsuarioApi())

        viewModel.cambiarContrasena()

        assertEquals(
            "Todos los campos de contraseña son obligatorios.",
            viewModel.uiState.value.mensajeErrorContrasena
        )
    }

    @Test
    fun cambiarContrasena_muyCorta_muestraError() {
        val viewModel = PerfilViewModel(FakeUsuarioApi())

        viewModel.onContrasenaActualCambia("123456")
        viewModel.onContrasenaNuevaCambia("123")
        viewModel.onConfirmarContrasenaCambia("123")

        viewModel.cambiarContrasena()

        assertEquals(
            "La nueva contraseña debe tener al menos 6 caracteres.",
            viewModel.uiState.value.mensajeErrorContrasena
        )
    }

    @Test
    fun cambiarContrasena_noCoinciden_muestraError() {
        val viewModel = PerfilViewModel(FakeUsuarioApi())

        viewModel.onContrasenaActualCambia("123456")
        viewModel.onContrasenaNuevaCambia("nueva123")
        viewModel.onConfirmarContrasenaCambia("otra123")

        viewModel.cambiarContrasena()

        assertEquals(
            "Las nuevas contraseñas no coinciden.",
            viewModel.uiState.value.mensajeErrorContrasena
        )
    }


    @Test
    fun limpiarMensajes_eliminaError() {
        val viewModel = PerfilViewModel(FakeUsuarioApi())

        // Provocamos un error de validación
        viewModel.onNombreCambia("")
        viewModel.actualizarPerfil()

        // Comprobamos que apareció el error
        assertNotNull(viewModel.uiState.value.mensajeError)

        // Limpiamos los mensajes
        viewModel.limpiarMensajes()

        // Verificamos que quedaron vacíos
        assertNull(viewModel.uiState.value.mensajeError)
        assertNull(viewModel.uiState.value.mensajeExito)
        assertNull(viewModel.uiState.value.mensajeErrorContrasena)
        assertNull(viewModel.uiState.value.mensajeExitoContrasena)
    }
}
