package com.example.induztek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztek.data.local.entity.EquipoEntity
import com.example.induztek.data.repository.EquipoRepository
import com.example.induztek.model.CatalogoClientes
import com.example.induztek.model.EquipoUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EquipoViewModel(
    private val equipoRepository: EquipoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EquipoUiState())
    val uiState: StateFlow<EquipoUiState> = _uiState.asStateFlow()

    private var observarEquiposJob: Job? = null

    fun onClienteSeleccionado(clienteId: String?) {
        observarEquiposJob?.cancel()
        observarEquiposJob = null
        _uiState.update { currentState ->
            currentState.copy(
                clienteSeleccionadoId = clienteId,
                instalacionSeleccionadaId = null,
                equipos = emptyList(),
                mensajeError = null,
                mensajeExito = null,
            )
        }
    }

    fun onInstalacionSeleccionada(instalacionId: String?) {
        observarEquiposJob?.cancel()
        observarEquiposJob = null

        _uiState.update { currentState ->
            val clienteActual = currentState.clienteSeleccionadoId
            val esValida = (clienteActual != null && CatalogoClientes.getInstalacionesPorCliente(clienteActual)
                .any { it.id == instalacionId })

            currentState.copy(
                instalacionSeleccionadaId = if (esValida) instalacionId else null,
                equipos = emptyList(),
                mensajeError = null,
                mensajeExito = null,
            )
        }

        if (instalacionId != null && _uiState.value.instalacionSeleccionadaId == instalacionId) {
            iniciarObservacionEquipos(instalacionId)
        }
    }

    private fun iniciarObservacionEquipos(instalacionId: String) {
        observarEquiposJob?.cancel()
        observarEquiposJob = viewModelScope.launch {
            try {
                equipoRepository.observarEquiposPorInstalacion(instalacionId).collect { listaEquipos ->
                    _uiState.update { currentState ->
                        currentState.copy(equipos = listaEquipos)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(mensajeError = "No fue posible cargar los equipos de la instalación.")
                }
            }
        }
    }

    fun onNombreCambia(nombre: String) {
        _uiState.update { it.copy(nombre = nombre) }
    }

    fun onTipoCambia(tipo: String) {
        _uiState.update { it.copy(tipo = tipo) }
    }

    fun onMarcaCambia(marca: String) {
        _uiState.update { it.copy(marca = marca) }
    }

    fun onModeloCambia(modelo: String) {
        _uiState.update { it.copy(modelo = modelo) }
    }

    fun onNumeroSerieCambia(numeroSerie: String) {
        _uiState.update { it.copy(numeroSerie = numeroSerie) }
    }

    fun onCodigoInternoCambia(codigoInterno: String) {
        _uiState.update { it.copy(codigoInterno = codigoInterno) }
    }

    fun onObservacionesCambia(observaciones: String) {
        _uiState.update { it.copy(observaciones = observaciones) }
    }

    fun registrarEquipo() {
        val currentState = _uiState.value
        if (currentState.guardando) {
            return
        }

        if (currentState.clienteSeleccionadoId.isNullOrBlank() || currentState.instalacionSeleccionadaId.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    mensajeError = "Debe seleccionar un cliente y una instalación.",
                    mensajeExito = null,
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                guardando = true,
                mensajeError = null,
                mensajeExito = null,
            )
        }

        viewModelScope.launch {
            try {
                val equipo = EquipoEntity(
                    clienteId = currentState.clienteSeleccionadoId,
                    instalacionId = currentState.instalacionSeleccionadaId,
                    nombre = currentState.nombre,
                    tipo = currentState.tipo,
                    marca = currentState.marca,
                    modelo = currentState.modelo,
                    numeroSerie = currentState.numeroSerie,
                    codigoInterno = currentState.codigoInterno,
                    observaciones = currentState.observaciones,
                )

                equipoRepository.registrarEquipo(equipo)

                _uiState.update {
                    it.copy(
                        guardando = false,
                        nombre = "",
                        tipo = "",
                        marca = "",
                        modelo = "",
                        numeroSerie = "",
                        codigoInterno = "",
                        observaciones = "",
                        mensajeExito = "Equipo registrado exitosamente.",
                        mensajeError = null,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensajeError = e.localizedMessage ?: "Error de validación en el equipo.",
                        mensajeExito = null,
                    )
                }
            } catch (e: Exception) {
                val mensaje = if (e.localizedMessage?.contains("UNIQUE", ignoreCase = true) == true ||
                    e.localizedMessage?.contains("constraint", ignoreCase = true) == true
                ) {
                    "El número de serie ingresado ya se encuentra registrado."
                } else {
                    "Error al registrar el equipo: ${e.localizedMessage ?: "Desconocido"}"
                }
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensajeError = mensaje,
                        mensajeExito = null,
                    )
                }
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.update {
            it.copy(
                mensajeError = null,
                mensajeExito = null,
            )
        }
    }
}
