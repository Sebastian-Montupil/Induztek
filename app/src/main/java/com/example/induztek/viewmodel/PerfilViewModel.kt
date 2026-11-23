package com.example.induztek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztek.data.repository.SesionRepository
import com.example.induztek.model.PerfilUiState
import com.example.induztek.network.CambiarContrasenaDto
import com.example.induztek.network.RetrofitClient
import com.example.induztek.network.SesionManager
import com.example.induztek.network.UsuarioApi
import com.example.induztek.network.UsuarioUpdateDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class PerfilViewModel(
    private val usuarioApi: UsuarioApi = RetrofitClient.crearServicio(UsuarioApi::class.java),
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        cargarPerfil()
    }

    fun cargarPerfil() {
        val usuario = SesionRepository.usuarioActual.value
        if (usuario != null) {
            _uiState.update {
                it.copy(
                    nombre = usuario.nombreCompleto,
                    correo = usuario.correo,
                    ciudad = usuario.cargo ?: "",
                    rol = usuario.rol.name,
                )
            }
        }
    }

    fun onNombreCambia(nombre: String) {
        _uiState.update { it.copy(nombre = nombre, mensajeError = null, mensajeExito = null) }
    }

    fun onCorreoCambia(correo: String) {
        _uiState.update { it.copy(correo = correo, mensajeError = null, mensajeExito = null) }
    }

    fun onCiudadCambia(ciudad: String) {
        _uiState.update { it.copy(ciudad = ciudad, mensajeError = null, mensajeExito = null) }
    }

    fun onContrasenaActualCambia(valor: String) {
        _uiState.update { it.copy(contrasenaActual = valor, mensajeErrorContrasena = null, mensajeExitoContrasena = null) }
    }

    fun onContrasenaNuevaCambia(valor: String) {
        _uiState.update { it.copy(contrasenaNueva = valor, mensajeErrorContrasena = null, mensajeExitoContrasena = null) }
    }

    fun onConfirmarContrasenaCambia(valor: String) {
        _uiState.update { it.copy(confirmarContrasena = valor, mensajeErrorContrasena = null, mensajeExitoContrasena = null) }
    }

    fun actualizarPerfil() {
        val currentState = _uiState.value
        if (currentState.cargando) {
            return
        }

        val usuarioActual = SesionRepository.usuarioActual.value
        val usuarioId = usuarioActual?.id?.toLongOrNull()
        if (usuarioId == null || usuarioId <= 0L) {
            _uiState.update { it.copy(mensajeError = "No se encontró el ID de usuario activo.") }
            return
        }

        val nombre = currentState.nombre.trim()
        val correo = currentState.correo.trim()
        val ciudad = currentState.ciudad.trim()

        if (nombre.isEmpty() || correo.isEmpty() || ciudad.isEmpty()) {
            _uiState.update { it.copy(mensajeError = "Nombre, correo y ciudad son obligatorios.") }
            return
        }

        _uiState.update { it.copy(cargando = true, mensajeError = null, mensajeExito = null) }

        viewModelScope.launch {
            try {
                val dto = UsuarioUpdateDto(
                    nombre = nombre,
                    correo = correo,
                    ciudad = ciudad.ifBlank { null },
                )
                val actualizadoDto = usuarioApi.actualizarPerfil(usuarioId, dto)

                val usuarioActualizado = usuarioActual.copy(
                    nombreCompleto = actualizadoDto.nombre ?: nombre,
                    correo = actualizadoDto.correo ?: correo,
                    cargo = actualizadoDto.ciudad,
                )
                SesionRepository.establecerUsuario(usuarioActualizado)
                SesionManager.guardarCredenciales(usuarioActualizado.correo, currentState.contrasenaActual)

                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensajeExito = "Perfil actualizado exitosamente.",
                        mensajeError = null,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                val mensaje = when (e.code()) {
                    401 -> "Sesión expirada o no autorizada."
                    403 -> "No tienes permiso para actualizar este perfil."
                    500 -> "Ocurrió un problema en el servidor."
                    else -> "Error al actualizar (${e.code()})."
                }
                _uiState.update { it.copy(cargando = false, mensajeError = mensaje) }
            } catch (e: IOException) {
                _uiState.update { it.copy(cargando = false, mensajeError = "No se pudo conectar con el servidor.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, mensajeError = "Error: ${e.localizedMessage ?: "Desconocido"}") }
            }
        }
    }

    fun cambiarContrasena() {
        val currentState = _uiState.value
        if (currentState.guardandoContrasena) {
            return
        }

        val usuarioActual = SesionRepository.usuarioActual.value
        val usuarioId = usuarioActual?.id?.toLongOrNull()
        if (usuarioId == null || usuarioId <= 0L) {
            _uiState.update { it.copy(mensajeErrorContrasena = "No se encontró el ID de usuario activo.") }
            return
        }

        val actual = currentState.contrasenaActual
        val nueva = currentState.contrasenaNueva
        val confirmar = currentState.confirmarContrasena

        if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
            _uiState.update { it.copy(mensajeErrorContrasena = "Todos los campos de contraseña son obligatorios.") }
            return
        }

        if (nueva.length < 6) {
            _uiState.update { it.copy(mensajeErrorContrasena = "La nueva contraseña debe tener al menos 6 caracteres.") }
            return
        }

        if (nueva != confirmar) {
            _uiState.update { it.copy(mensajeErrorContrasena = "Las nuevas contraseñas no coinciden.") }
            return
        }

        _uiState.update { it.copy(guardandoContrasena = true, mensajeErrorContrasena = null, mensajeExitoContrasena = null) }

        viewModelScope.launch {
            try {
                val dto = CambiarContrasenaDto(
                    actual = actual,
                    nueva = nueva,
                )
                usuarioApi.cambiarContrasena(usuarioId, dto)

                SesionManager.guardarCredenciales(usuarioActual.correo, nueva)

                _uiState.update {
                    it.copy(
                        guardandoContrasena = false,
                        contrasenaActual = "",
                        contrasenaNueva = "",
                        confirmarContrasena = "",
                        mensajeExitoContrasena = "Contraseña cambiada exitosamente.",
                        mensajeErrorContrasena = null,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                val mensaje = when (e.code()) {
                    401 -> "La contraseña actual es incorrecta o la sesión expiró."
                    403 -> "No tienes permiso para realizar esta acción."
                    500 -> "Ocurrió un problema en el servidor."
                    else -> "Error al cambiar contraseña (${e.code()})."
                }
                _uiState.update { it.copy(guardandoContrasena = false, mensajeErrorContrasena = mensaje) }
            } catch (e: IOException) {
                _uiState.update { it.copy(guardandoContrasena = false, mensajeErrorContrasena = "No se pudo conectar con el servidor.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardandoContrasena = false, mensajeErrorContrasena = "Error: ${e.localizedMessage ?: "Desconocido"}") }
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.update {
            it.copy(
                mensajeError = null,
                mensajeExito = null,
                mensajeErrorContrasena = null,
                mensajeExitoContrasena = null,
            )
        }
    }
}
