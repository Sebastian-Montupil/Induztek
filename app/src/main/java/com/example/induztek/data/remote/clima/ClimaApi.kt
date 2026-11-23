package com.example.induztek.data.remote.clima

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class ClimaResponse(
    val current: ClimaActual
)

data class ClimaActual(
    @SerializedName("temperature_2m")
    val temperatura: Double,

    @SerializedName("relative_humidity_2m")
    val humedad: Int,

    @SerializedName("precipitation")
    val precipitacion: Double,

    @SerializedName("time")
    val fechaHora: String
)

interface ClimaApi {

    @GET("v1/forecast")
    suspend fun obtenerClima(
        @Query("latitude") latitud: Double = -33.45,
        @Query("longitude") longitud: Double = -70.66,
        @Query("current") variables: String =
            "temperature_2m,relative_humidity_2m,precipitation",
        @Query("timezone") zonaHoraria: String =
            "America/Santiago"
    ): ClimaResponse
}
