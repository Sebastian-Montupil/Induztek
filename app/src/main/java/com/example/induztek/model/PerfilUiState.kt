package com.example.induztek.model

data class PerfilUiState(
    val nombre: String = "",
    val correo: String = "",
    val ciudad: String = "",
    val rol: String = "",
    val contrasenaActual: String = "",
    val contrasenaNueva: String = "",
    val confirmarContrasena: String = "",
    val cargando: Boolean = false,
    val guardandoContrasena: Boolean = false,
    val mensajeError: String? = null,
    val mensajeExito: String? = null,
    val mensajeErrorContrasena: String? = null,
    val mensajeExitoContrasena: String? = null,
)
