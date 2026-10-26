package com.example.induztek.model

import com.example.induztek.data.local.entity.VisitaEntity

data class VisitasUiState(
    val visitas: List<VisitaEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
