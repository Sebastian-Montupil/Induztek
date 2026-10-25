package com.example.induztek.data.model

data class InstrumentoMedicion(
    val nombre: String,
    val tipoInstrumento: String,
    val marca: String,
    val modelo: String,
    val numeroSerie: String,
    val codigoInterno: String,
    val fechaVencimientoCalibracion: String? = null,
    val observaciones: String,
)
