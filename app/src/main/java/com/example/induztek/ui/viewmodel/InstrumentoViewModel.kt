package com.example.induztek.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztek.data.local.entity.InstrumentoEntity
import com.example.induztek.data.repository.InstrumentoRepository
import com.example.induztek.domain.validation.ValidadorInstrumento
import com.example.induztek.model.InstrumentoUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InstrumentoViewModel(
    private val repository: InstrumentoRepository,
) : ViewModel() {

    val instrumentos: StateFlow<List<InstrumentoEntity>> = repository.observarInstrumentos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList(),
        )

    private val _uiState = MutableStateFlow(InstrumentoUiState())
    val uiState: StateFlow<InstrumentoUiState> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun onNombreCambia(nombre: String) {
        _uiState.update {
            it.copy(
                nombre = nombre,
                nombreError = if (nombre.isNotBlank()) false else it.nombreError,
            )
        }
    }

    fun onTipoInstrumentoCambia(tipo: String) {
        _uiState.update {
            it.copy(
                tipoInstrumento = tipo,
                tipoInstrumentoError = if (tipo.isNotBlank()) false else it.tipoInstrumentoError,
            )
        }
    }

    fun onMarcaCambia(marca: String) {
        _uiState.update {
            it.copy(
                marca = marca,
                marcaError = if (marca.isNotBlank()) false else it.marcaError,
            )
        }
    }

    fun onModeloCambia(modelo: String) {
        _uiState.update { it.copy(modelo = modelo) }
    }

    fun onNumeroSerieCambia(numeroSerie: String) {
        _uiState.update {
            it.copy(
                numeroSerie = numeroSerie,
                numeroSerieError = if (numeroSerie.isNotBlank()) false else it.numeroSerieError,
            )
        }
    }

    fun onCodigoInternoCambia(codigoInterno: String) {
        _uiState.update { it.copy(codigoInterno = codigoInterno) }
    }

    fun onFechaVencimientoCalibracionCambia(fecha: String) {
        _uiState.update {
            it.copy(
                fechaVencimientoCalibracion = fecha,
                fechaVencimientoError = false,
            )
        }
    }

    fun onObservacionesCambia(observaciones: String) {
        _uiState.update { it.copy(observaciones = observaciones) }
    }

    fun registrarInstrumento() {
        val currentState = _uiState.value
        if (currentState.guardando) {
            return
        }

        val resultado = ValidadorInstrumento.validar(currentState)
        if (!resultado.esValido) {
            _uiState.update {
                it.copy(
                    nombreError = resultado.nombreError,
                    tipoInstrumentoError = resultado.tipoInstrumentoError,
                    marcaError = resultado.marcaError,
                    numeroSerieError = resultado.numeroSerieError,
                    fechaVencimientoError = resultado.fechaVencimientoError,
                    mensajeError = "Por favor complete los campos obligatorios correctamente.",
                    mensajeExito = null,
                )
            }
            _errorMessage.value = "Por favor complete los campos obligatorios correctamente."
            return
        }

        _uiState.update {
            it.copy(
                guardando = true,
                nombreError = false,
                tipoInstrumentoError = false,
                marcaError = false,
                numeroSerieError = false,
                fechaVencimientoError = false,
                mensajeError = null,
                mensajeExito = null,
            )
        }
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val fechaVencimiento = currentState.fechaVencimientoCalibracion.ifBlank { null }
                val instrumento = InstrumentoEntity(
                    nombre = currentState.nombre,
                    tipoInstrumento = currentState.tipoInstrumento,
                    marca = currentState.marca,
                    modelo = currentState.modelo,
                    numeroSerie = currentState.numeroSerie,
                    codigoInterno = currentState.codigoInterno,
                    fechaVencimientoCalibracion = fechaVencimiento,
                    observaciones = currentState.observaciones,
                )

                repository.registrarInstrumento(instrumento)

                _uiState.update {
                    it.copy(
                        guardando = false,
                        nombre = "",
                        tipoInstrumento = "",
                        marca = "",
                        modelo = "",
                        numeroSerie = "",
                        codigoInterno = "",
                        fechaVencimientoCalibracion = "",
                        observaciones = "",
                        mensajeExito = "Instrumento registrado exitosamente.",
                        mensajeError = null,
                    )
                }
                _errorMessage.value = null
            } catch (e: CancellationException) {
                throw e
            } catch (e: SecurityException) {
                val mensaje = e.localizedMessage ?: "No autorizado para registrar instrumentos."
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensajeError = mensaje,
                        mensajeExito = null,
                    )
                }
                _errorMessage.value = mensaje
            } catch (e: Exception) {
                val mensaje = if (e.localizedMessage?.contains("UNIQUE", ignoreCase = true) == true ||
                    e.localizedMessage?.contains("constraint", ignoreCase = true) == true
                ) {
                    "El número de serie ingresado ya se encuentra registrado."
                } else {
                    "Error al registrar el instrumento: ${e.localizedMessage ?: "Desconocido"}"
                }
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensajeError = mensaje,
                        mensajeExito = null,
                    )
                }
                _errorMessage.value = mensaje
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
        _errorMessage.value = null
    }
}
