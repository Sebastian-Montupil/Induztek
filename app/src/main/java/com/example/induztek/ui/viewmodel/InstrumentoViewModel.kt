package com.example.induztek.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztek.data.local.entity.InstrumentoEntity
import com.example.induztek.data.repository.InstrumentoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InstrumentoViewModel(
    private val repository: InstrumentoRepository,
) : ViewModel() {

    // Lista observable de instrumentos expuesta a la UI mediante StateFlow
    val instrumentos: StateFlow<List<InstrumentoEntity>> = repository.observarInstrumentos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList(),
        )

    // Estado observable sencillo para notificar errores de inserción a la UI sin crashear la app
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Registra un instrumento de manera asíncrona dentro del ciclo de vida del ViewModel
    fun registrarInstrumento(instrumento: InstrumentoEntity) {
        viewModelScope.launch {
            try {
                repository.registrarInstrumento(instrumento)
                _errorMessage.value = null // Limpia error anterior si la operación es exitosa
            } catch (e: CancellationException) {
                // Re-lanza la cancelación para no interferir con la gestión de corrutinas de Kotlin
                throw e
            } catch (e: Exception) {
                // Atrapa excepciones ordinarias y expone el mensaje de error de forma segura
                _errorMessage.value = "Error al registrar el instrumento: ${e.localizedMessage ?: "Desconocido"}"
            }
        }
    }
}
