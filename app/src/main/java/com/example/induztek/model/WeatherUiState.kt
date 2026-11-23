package com.example.induztek.model

data class WeatherUiState(
    val ciudad: String = "Santiago",
    val temperatura: Double? = null,
    val humedad: Int? = null,
    val viento: Double? = null,
    val codigoClima: Int? = null,
    val cargando: Boolean = false,
    val error: String? = null,
)
