package com.example.induztek.model

data class IngresoUiState(
    val usuariosDisponibles: List<Usuario> = emptyList(),
    val usuarioSeleccionadoId: String? = null,
    val errorIngreso: String? = null
)
