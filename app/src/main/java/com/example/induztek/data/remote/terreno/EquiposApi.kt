package com.example.induztek.data.remote.terreno

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EquiposApi {

    @GET("api/equipos")
    suspend fun listarEquipos(
        @Query("clienteId") clienteId: String? = null,
        @Query("instalacionId") instalacionId: String? = null
    ): List<EquipoResponse>

    @GET("api/equipos/{id}")
    suspend fun obtenerEquipo(
        @Path("id") id: Long
    ): EquipoResponse

    @POST("api/equipos")
    suspend fun registrarEquipo(
        @Body request: RegistrarEquipoRequest
    ): EquipoResponse
}

interface InstrumentosApi {

    @GET("api/instrumentos")
    suspend fun listarInstrumentos(): List<InstrumentoResponse>

    @GET("api/instrumentos/{id}")
    suspend fun obtenerInstrumento(
        @Path("id") id: Long
    ): InstrumentoResponse

    @POST("api/instrumentos")
    suspend fun registrarInstrumento(
        @Body request: RegistrarInstrumentoRequest
    ): InstrumentoResponse
}

interface CalibracionesApi {

    @GET("api/calibraciones")
    suspend fun listarCalibraciones(
        @Query("instrumentoId") instrumentoId: Long? = null
    ): List<CalibracionResponse>

    @GET("api/calibraciones/{id}")
    suspend fun obtenerCalibracion(
        @Path("id") id: Long
    ): CalibracionResponse

    @POST("api/calibraciones")
    suspend fun registrarCalibracion(
        @Body request: RegistrarCalibracionRequest
    ): CalibracionResponse
}
