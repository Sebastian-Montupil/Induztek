package com.example.induztek.model

data class InstrumentoUiState(
    val nombre: String = "",
    val tipoInstrumento: String = "",
    val marca: String = "",
    val modelo: String = "",
    val numeroSerie: String = "",
    val codigoInterno: String = "",
    val fechaVencimientoCalibracion: String = "",
    val observaciones: String = "",
    val guardando: Boolean = false,
    val mensajeError: String? = null,
    val mensajeExito: String? = null,
    val nombreError: Boolean = false,
    val tipoInstrumentoError: Boolean = false,
    val marcaError: Boolean = false,
    val numeroSerieError: Boolean = false,
    val fechaVencimientoError: Boolean = false,
)
