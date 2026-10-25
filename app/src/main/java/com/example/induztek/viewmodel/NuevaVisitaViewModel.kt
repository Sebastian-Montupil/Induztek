package com.example.induztek.viewmodel

import androidx.lifecycle.ViewModel
import com.example.induztek.domain.validation.ValidadorNuevaVisita
import com.example.induztek.model.CatalogoClientes
import com.example.induztek.model.NuevaVisitaUiState
import com.example.induztek.model.TipoVisita
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NuevaVisitaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NuevaVisitaUiState())
    val uiState: StateFlow<NuevaVisitaUiState> = _uiState.asStateFlow()

    fun onClienteSeleccionado(clienteId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                clienteSeleccionadoId = clienteId,
                instalacionSeleccionadaId = null,
                clienteError = false,
                instalacionError = false
            )
        }
    }

    fun onInstalacionSeleccionada(instalacionId: String) {
        _uiState.update { currentState ->
            val clienteActual = currentState.clienteSeleccionadoId
            val esValida = CatalogoClientes.getInstalacionesPorCliente(clienteActual)
                .any { it.id == instalacionId }

            currentState.copy(
                instalacionSeleccionadaId = if (esValida) instalacionId else null,
                instalacionError = false
            )
        }
    }

    fun onFechaSeleccionada(millis: Long?) {
        _uiState.update { currentState ->
            currentState.copy(
                fechaSelectedMillis = millis,
                fechaError = millis == null
            )
        }
    }

    fun onNumeroOtCambia(nuevoNumeroOt: String) {
        _uiState.update { currentState ->
            val isErrorNow = currentState.numeroOtError && nuevoNumeroOt.trim().isEmpty()
            currentState.copy(
                numeroOt = nuevoNumeroOt,
                numeroOtError = isErrorNow
            )
        }
    }

    fun onTipoVisitaCambia(tipo: TipoVisita) {
        _uiState.update { currentState ->
            currentState.copy(
                tipoVisita = tipo,
                tipoVisitaError = false
            )
        }
    }

    fun onIngenieroCargoCambia(ingenieroId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                ingenieroCargoId = ingenieroId,
                ingenieroError = false
            )
        }
    }

    fun onTecnicoToggle(tecnicoId: String) {
        _uiState.update { currentState ->
            val nuevosTecnicos = if (currentState.tecnicosSeleccionadosIds.contains(tecnicoId)) {
                currentState.tecnicosSeleccionadosIds - tecnicoId
            } else {
                currentState.tecnicosSeleccionadosIds + tecnicoId
            }
            currentState.copy(tecnicosSeleccionadosIds = nuevosTecnicos)
        }
    }

    fun onObservacionesCambia(nuevasObservaciones: String) {
        _uiState.update { currentState ->
            currentState.copy(observaciones = nuevasObservaciones)
        }
    }

    fun validarDatos() {
        val resultado = ValidadorNuevaVisita.validar(_uiState.value)
        _uiState.update { state ->
            state.copy(
                clienteError = resultado.clienteError,
                instalacionError = resultado.instalacionError,
                fechaError = resultado.fechaError,
                numeroOtError = resultado.numeroOtError,
                tipoVisitaError = resultado.tipoVisitaError,
                ingenieroError = resultado.ingenieroError,
                showValidDialog = resultado.esValido
            )
        }
    }

    fun cerrarDialogo() {
        _uiState.update { currentState ->
            currentState.copy(showValidDialog = false)
        }
    }
}
