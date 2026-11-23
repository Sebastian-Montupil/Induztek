package com.example.induztek.data.remote.terreno

data class RegistrarVisitaRequest(
    val clienteId: String,
    val instalacionId: String,
    val fechaMillis: Long,
    val numeroOt: String,
    val tipoVisita: String,
    val ingenieroCargoId: String,
    val observaciones: String,
    val tecnicoIds: List<String>
)

data class VisitaResponse(
    val id: Long,
    val clienteId: String,
    val instalacionId: String,
    val fechaMillis: Long,
    val numeroOt: String,
    val tipoVisita: String,
    val ingenieroCargoId: String,
    val observaciones: String?
)

data class RegistrarEquipoRequest(
    val clienteId: String,
    val instalacionId: String,
    val nombre: String,
    val tipo: String,
    val marca: String,
    val modelo: String,
    val numeroSerie: String,
    val codigoInterno: String,
    val observaciones: String
)

data class EquipoResponse(
    val id: Long,
    val clienteId: String,
    val instalacionId: String,
    val nombre: String,
    val tipo: String,
    val marca: String,
    val modelo: String?,
    val numeroSerie: String,
    val codigoInterno: String?,
    val observaciones: String?
)

data class RegistrarInstrumentoRequest(
    val nombre: String,
    val tipoInstrumento: String,
    val marca: String,
    val modelo: String,
    val numeroSerie: String,
    val codigoInterno: String,
    val fechaVencimientoCalibracion: String?,
    val observaciones: String
)

data class InstrumentoResponse(
    val id: Long,
    val nombre: String,
    val tipoInstrumento: String,
    val marca: String,
    val modelo: String?,
    val numeroSerie: String,
    val codigoInterno: String?,
    val fechaVencimientoCalibracion: String?,
    val observaciones: String?
)

data class RegistrarCalibracionRequest(
    val instrumentoId: Long,
    val fechaCalibracion: String,
    val fechaVencimiento: String
)

data class CalibracionResponse(
    val id: Long,
    val instrumentoId: Long,
    val fechaCalibracion: String,
    val fechaVencimiento: String
)
