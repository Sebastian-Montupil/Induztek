package com.example.induztek.model

import com.example.induztek.data.local.entity.EquipoEntity

data class EquipoUiState(
    val clienteSeleccionadoId: String? = null,
    val instalacionSeleccionadaId: String? = null,
    val nombre: String = "",
    val tipo: String = "",
    val marca: String = "",
    val modelo: String = "",
    val numeroSerie: String = "",
    val codigoInterno: String = "",
    val observaciones: String = "",
    val equipos: List<EquipoEntity> = emptyList(),
    val guardando: Boolean = false,
    val mensajeError: String? = null,
    val mensajeExito: String? = null,
)
