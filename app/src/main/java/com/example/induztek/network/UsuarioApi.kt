package com.example.induztek.network

import retrofit2.http.GET

interface UsuarioApi {
    @GET("api/usuarios/me")
    suspend fun obtenerPerfilMe(): UsuarioResponseDto
}
