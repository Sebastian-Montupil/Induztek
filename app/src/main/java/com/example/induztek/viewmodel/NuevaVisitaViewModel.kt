package com.example.induztek.viewmodel

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.induztek.data.local.entity.VisitaEntity
import com.example.induztek.data.repository.VisitaRepository
import com.example.induztek.domain.validation.ValidadorNuevaVisita
import com.example.induztek.model.CatalogoClientes
import com.example.induztek.model.NuevaVisitaUiState
import com.example.induztek.model.TipoVisita
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NuevaVisitaViewModel(
    private val visitaRepository: VisitaRepository
) : ViewModel() {

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
                numeroOtError = isErrorNow,
                errorPersistencia = null
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
        val currentState = _uiState.value
        if (currentState.isSaving || currentState.hasSuccessfullySaved) return

        val resultado = ValidadorNuevaVisita.validar(currentState)
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
        if (resultado.esValido) {
            guardarVisita()
        }
    }

    private fun guardarVisita() {
        val currentState = _uiState.value
        if (currentState.isSaving || currentState.hasSuccessfullySaved) return

        _uiState.update { it.copy(isSaving = true, errorPersistencia = null, showValidDialog = false) }

        viewModelScope.launch {
            try {
                val visita = VisitaEntity(
                    clienteId = currentState.clienteSeleccionadoId ?: "",
                    instalacionId = currentState.instalacionSeleccionadaId ?: "",
                    fechaMillis = currentState.fechaSelectedMillis ?: 0L,
                    numeroOt = currentState.numeroOt.trim(),
                    tipoVisita = currentState.tipoVisita?.name ?: "",
                    ingenieroCargoId = currentState.ingenieroCargoId ?: "",
                    observaciones = currentState.observaciones.trim()
                )
                val tecnicos = currentState.tecnicosSeleccionadosIds.toList()

                visitaRepository.registrarVisita(visita, tecnicos)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        hasSuccessfullySaved = true,
                        showSuccessDialog = true,
                        errorPersistencia = null
                    )
                }
            } catch (e: SQLiteConstraintException) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        numeroOtError = false,
                        errorPersistencia = "Ya existe una visita registrada con este número de OT."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorPersistencia = "Error al guardar la visita: ${e.localizedMessage ?: "Desconocido"}"
                    )
                }
            }
        }
    }

    fun cerrarDialogo() {
        _uiState.update { currentState ->
            currentState.copy(showValidDialog = false)
        }
    }

    fun cerrarDialogoExito() {
        _uiState.update { currentState ->
            currentState.copy(showSuccessDialog = false)
        }
    }
}

class NuevaVisitaViewModelFactory(
    private val visitaRepository: VisitaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NuevaVisitaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NuevaVisitaViewModel(visitaRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
