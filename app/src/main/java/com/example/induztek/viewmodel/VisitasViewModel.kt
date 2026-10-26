package com.example.induztek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.induztek.data.repository.VisitaRepository
import com.example.induztek.model.VisitasUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class VisitasViewModel(
    visitaRepository: VisitaRepository
) : ViewModel() {

    val uiState: StateFlow<VisitasUiState> = visitaRepository.observarVisitas()
        .map { listaVisitas ->
            VisitasUiState(
                visitas = listaVisitas,
                isLoading = false,
                errorMessage = null
            )
        }
        .catch { error ->
            emit(
                VisitasUiState(
                    visitas = emptyList(),
                    isLoading = false,
                    errorMessage = error.localizedMessage ?: "Error al cargar el listado de visitas."
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = VisitasUiState(isLoading = true)
        )
}

class VisitasViewModelFactory(
    private val visitaRepository: VisitaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VisitasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VisitasViewModel(visitaRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
