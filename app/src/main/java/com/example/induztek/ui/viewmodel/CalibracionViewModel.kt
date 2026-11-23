package com.example.induztek.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztek.data.local.entity.CalibracionEntity
import com.example.induztek.data.remote.terreno.ServiciosTerreno
import com.example.induztek.data.repository.SesionRepository
import com.example.induztek.domain.validation.ValidadorCalibracion
import com.example.induztek.domain.validation.ValidadorPermisoInstrumentos
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CalibracionViewModel : ViewModel() {

    private val remoto = ServiciosTerreno.calibraciones

    private val _calibraciones =
        MutableStateFlow<List<CalibracionEntity>>(emptyList())
    val calibraciones = _calibraciones.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando = _cargando.asStateFlow()

    private val _guardando = MutableStateFlow(false)
    val guardando = _guardando.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje = _mensaje.asStateFlow()

    init {
        cargarCalibraciones()
    }

    fun cargarCalibraciones() {
        viewModelScope.launch {
            _cargando.value = true

            try {
                _calibraciones.value = remoto.listarCalibraciones()
                _mensaje.value = null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensaje.value =
                    "Error al cargar calibraciones: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun registrarCalibracion(
        instrumentoId: Long,
        fechaCalibracion: String,
        fechaVencimiento: String
    ) {
        if (_guardando.value) return

        val errorPermiso =
            ValidadorPermisoInstrumentos.validarGestion(
                SesionRepository.usuarioActual.value
            )

        if (errorPermiso != null) {
            _mensaje.value = errorPermiso
            return
        }

        if (instrumentoId <= 0L) {
            _mensaje.value = "Selecciona un instrumento válido."
            return
        }

        val fecha = fechaCalibracion.trim()
        val vencimiento = fechaVencimiento.trim()

        val errorFecha = ValidadorCalibracion.validarVigencia(
            fechaCalibracion = fecha,
            fechaVencimiento = vencimiento,
            fechaPrueba = fecha
        )

        if (errorFecha != null) {
            _mensaje.value = errorFecha
            return
        }

        _guardando.value = true
        _mensaje.value = null

        viewModelScope.launch {
            try {
                remoto.registrarCalibracion(
                    CalibracionEntity(
                        instrumentoId = instrumentoId,
                        fechaCalibracion = fecha,
                        fechaVencimiento = vencimiento
                    )
                )

                _calibraciones.value = remoto.listarCalibraciones()
                _mensaje.value = "Calibración registrada exitosamente."

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensaje.value =
                    "Error al registrar calibración: ${e.message}"
            } finally {
                _guardando.value = false
            }
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
