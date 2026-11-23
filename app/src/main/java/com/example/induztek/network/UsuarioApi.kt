package com.example.induztek.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface UsuarioApi {
    @GET("api/usuarios/me")
    suspend fun obtenerPerfilMe(): UsuarioResponseDto

    @PUT("api/usuarios/{id}")
    suspend fun actualizarPerfil(
        @Path("id") id: Long,
        @Body request: UsuarioUpdateDto,
    ): UsuarioResponseDto

    @PUT("api/usuarios/{id}/contrasena")
    suspend fun cambiarContrasena(
        @Path("id") id: Long,
        @Body request: CambiarContrasenaDto,
    )
}
