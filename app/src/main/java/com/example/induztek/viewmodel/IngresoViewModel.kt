package com.example.induztek.viewmodel

import androidx.lifecycle.ViewModel
import com.example.induztek.data.repository.SesionRepository
import com.example.induztek.model.CatalogoUsuarios
import com.example.induztek.model.IngresoUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class IngresoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        IngresoUiState(
            usuariosDisponibles = CatalogoUsuarios.usuarios.filter { it.activo }
        )
    )
    val uiState: StateFlow<IngresoUiState> = _uiState.asStateFlow()

    fun onUsuarioSeleccionado(id: String) {
        _uiState.update { currentState ->
            currentState.copy(
                usuarioSeleccionadoId = id,
                errorIngreso = null
            )
        }
    }

    fun iniciarSesion(): Boolean {
        val currentState = _uiState.value
        val seleccionadoId = currentState.usuarioSeleccionadoId

        if (seleccionadoId == null) {
            _uiState.update { state ->
                state.copy(errorIngreso = "Debe seleccionar un usuario para iniciar sesión.")
            }
            return false
        }

        val usuario = currentState.usuariosDisponibles.find { it.id == seleccionadoId }

        if (usuario == null || !usuario.activo) {
            _uiState.update { state ->
                state.copy(errorIngreso = "El usuario seleccionado no es válido o está inactivo.")
            }
            return false
        }

        SesionRepository.establecerUsuario(usuario)
        _uiState.update { state ->
            state.copy(errorIngreso = null)
        }
        return true
    }
}
