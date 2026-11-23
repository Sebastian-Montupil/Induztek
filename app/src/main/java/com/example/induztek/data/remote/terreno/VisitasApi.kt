package com.example.induztek.data.remote.terreno

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface VisitasApi {

    @GET("api/visitas")
    suspend fun listarVisitas(): List<VisitaResponse>

    @GET("api/visitas/{id}")
    suspend fun obtenerVisita(
        @Path("id") id: Long
    ): VisitaResponse

    @POST("api/visitas")
    suspend fun registrarVisita(
        @Body request: RegistrarVisitaRequest
    ): VisitaResponse
}
