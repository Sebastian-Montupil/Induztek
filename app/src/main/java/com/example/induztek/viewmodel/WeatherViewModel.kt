package com.example.induztek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztek.data.repository.WeatherRepository
import com.example.induztek.model.WeatherUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val weatherRepository: WeatherRepository = WeatherRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        cargarClima()
    }

    fun cargarClima() {
        if (_uiState.value.cargando) {
            return
        }

        _uiState.update { it.copy(cargando = true, error = null) }

        viewModelScope.launch {
            try {
                val response = weatherRepository.consultarClima()
                val current = response.current
                if (current != null) {
                    _uiState.update {
                        it.copy(
                            temperatura = current.temperature2m,
                            humedad = current.relativeHumidity2m,
                            viento = current.windSpeed10m,
                            codigoClima = current.weatherCode,
                            cargando = false,
                            error = null,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            error = "Respuesta meteorológica incompleta.",
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No se pudo actualizar el clima: ${e.localizedMessage ?: "Error de red"}",
                    )
                }
            }
        }
    }

    fun traducirCodigoClima(code: Int?): String {
        return when (code) {
            0 -> "Despejado"
            1, 2, 3 -> "Parcialmente nublado"
            45, 48 -> "Neblina"
            51, 53, 55, 56, 57 -> "Llovizna"
            61, 63, 65, 66, 67 -> "Lluvia"
            71, 73, 75, 77 -> "Nieve"
            80, 81, 82 -> "Chubascos"
            95, 96, 99 -> "Tormenta eléctrica"
            else -> "Condición variable"
        }
    }
}
