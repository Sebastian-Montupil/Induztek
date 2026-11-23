package com.example.induztek.model

data class IngresoUiState(
    val correo: String = "",
    val contrasena: String = "",
    val cargando: Boolean = false,
    val errorIngreso: String? = null,
)
