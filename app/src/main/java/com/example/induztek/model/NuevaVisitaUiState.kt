package com.example.induztek.model

data class NuevaVisitaUiState(
    val clienteSeleccionadoId: String? = null,
    val instalacionSeleccionadaId: String? = null,
    val fechaSelectedMillis: Long? = null,
    val numeroOt: String = "",
    val tipoVisita: TipoVisita? = null,
    val ingenieroCargoId: String? = null,
    val tecnicosSeleccionadosIds: Set<String> = emptySet(),
    val observaciones: String = "",
    val clienteError: Boolean = false,
    val instalacionError: Boolean = false,
    val fechaError: Boolean = false,
    val numeroOtError: Boolean = false,
    val tipoVisitaError: Boolean = false,
    val ingenieroError: Boolean = false,
    val showValidDialog: Boolean = false
)
