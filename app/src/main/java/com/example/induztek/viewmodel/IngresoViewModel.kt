package com.example.induztek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztek.data.repository.SesionRepository
import com.example.induztek.model.IngresoUiState
import com.example.induztek.model.Rol
import com.example.induztek.model.Usuario
import com.example.induztek.network.RetrofitClient
import com.example.induztek.network.SesionManager
import com.example.induztek.network.UsuarioApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class IngresoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(IngresoUiState())
    val uiState: StateFlow<IngresoUiState> = _uiState.asStateFlow()

    private val usuarioApi = RetrofitClient.crearServicio(UsuarioApi::class.java)

    fun onCorreoCambia(correo: String) {
        _uiState.update { it.copy(correo = correo, errorIngreso = null) }
    }

    fun onContrasenaCambia(contrasena: String) {
        _uiState.update { it.copy(contrasena = contrasena, errorIngreso = null) }
    }

    private fun parseRol(rolNombre: String?): Rol {
        val normalized = rolNombre?.uppercase()?.replace(" ", "_") ?: ""
        return when {
            normalized.contains("JEFE") -> Rol.JEFE_TECNICO
            normalized.contains("INGENIERO") -> Rol.INGENIERO
            normalized.contains("TECNICO") -> Rol.TECNICO
            else -> Rol.CLIENTE
        }
    }

    fun iniciarSesion(onExito: () -> Unit) {
        val currentState = _uiState.value
        if (currentState.cargando) {
            return
        }

        val correo = currentState.correo.trim()
        val contrasena = currentState.contrasena

        if (correo.isEmpty() || contrasena.isEmpty()) {
            _uiState.update {
                it.copy(errorIngreso = "Por favor ingrese correo y contraseña.")
            }
            return
        }

        _uiState.update {
            it.copy(cargando = true, errorIngreso = null)
        }

        SesionManager.guardarCredenciales(correo, contrasena)

        viewModelScope.launch {
            try {
                val dto = usuarioApi.obtenerPerfilMe()
                val usuario = Usuario(
                    id = dto.id?.toString() ?: "0",
                    nombreCompleto = dto.nombre ?: "Usuario",
                    rut = "N/A",
                    correo = dto.correo ?: correo,
                    rol = parseRol(dto.rolNombre),
                    activo = true,
                    clienteId = null,
                    cargo = dto.ciudad,
                )

                SesionRepository.establecerUsuario(usuario)
                _uiState.update {
                    it.copy(cargando = false, errorIngreso = null)
                }
                onExito()
            } catch (e: CancellationException) {
                SesionManager.cerrarSesion()
                throw e
            } catch (e: HttpException) {
                SesionManager.cerrarSesion()
                val mensaje = when (e.code()) {
                    401 -> "Correo o contraseña incorrectos."
                    403 -> "No tienes permiso para realizar esta acción."
                    500 -> "Ocurrió un problema en el servidor."
                    else -> "Error de autenticación (${e.code()})."
                }
                _uiState.update {
                    it.copy(cargando = false, errorIngreso = mensaje)
                }
            } catch (e: IOException) {
                SesionManager.cerrarSesion()
                android.util.Log.e(
                    "InduztekLogin",
                    "Error: ${e.javaClass.simpleName}: ${e.message}"
                )
                _uiState.update {
                    it.copy(cargando = false, errorIngreso = "No se pudo conectar con el servidor.")
                }
            } catch (e: Exception) {
                SesionManager.cerrarSesion()
                _uiState.update {
                    it.copy(cargando = false, errorIngreso = "Error inesperado: ${e.localizedMessage ?: "Desconocido"}")
                }
            }
        }
    }
}
