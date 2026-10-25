package com.example.induztek.model

data class Usuario(
    val id: String,
    val nombreCompleto: String,
    val rut: String,
    val correo: String,
    val rol: Rol,
    val activo: Boolean,
    val clienteId: String? = null,
    val cargo: String? = null,
)
