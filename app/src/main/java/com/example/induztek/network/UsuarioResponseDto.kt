package com.example.induztek.network

data class UsuarioResponseDto(
    val id: Long? = null,
    val nombre: String? = null,
    val correo: String? = null,
    val ciudad: String? = null,
    val rolId: Long? = null,
    val rolNombre: String? = null,
    val avatarUrl: String? = null,
)
