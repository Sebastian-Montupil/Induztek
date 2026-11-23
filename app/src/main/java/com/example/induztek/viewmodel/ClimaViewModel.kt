package com.example.induztek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztek.data.remote.clima.ClimaActual
import com.example.induztek.data.repository.ClimaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClimaUiState(
    val cargando: Boolean = false,
    val clima: ClimaActual? = null,
    val error: String? = null
)

class ClimaViewModel : ViewModel() {

    private val repository = ClimaRepository()

    private val _uiState = MutableStateFlow(ClimaUiState())
    val uiState = _uiState.asStateFlow()

    init {
        actualizar()
    }

    fun actualizar() {
        viewModelScope.launch {
            _uiState.value = ClimaUiState(cargando = true)

            try {
                val resultado = repository.obtenerClimaActual()

                _uiState.value = ClimaUiState(
                    clima = resultado
                )
            } catch (e: Exception) {
                _uiState.value = ClimaUiState(
                    error = "No se pudo consultar la información meteorológica."
                )
            }
        }
    }
}
