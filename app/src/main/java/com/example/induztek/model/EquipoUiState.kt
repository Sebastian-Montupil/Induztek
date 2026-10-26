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
    val clienteError: Boolean = false,
    val instalacionError: Boolean = false,
    val nombreError: Boolean = false,
    val tipoError: Boolean = false,
    val marcaError: Boolean = false,
    val numeroSerieError: Boolean = false,
)
