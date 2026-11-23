package com.example.induztek.network

import okhttp3.Credentials

object SesionManager {

    private var correoActual: String? = null
    private var contrasenaActual: String? = null

    fun guardarCredenciales(correo: String, contrasena: String) {
        correoActual = correo.trim()
        contrasenaActual = contrasena
    }

    fun obtenerAutorizacion(): String? {
        val correo = correoActual
        val contrasena = contrasenaActual
        if (!correo.isNullOrBlank() && !contrasena.isNullOrBlank()) {
            return Credentials.basic(correo, contrasena)
        }
        return null
    }

    fun cerrarSesion() {
        correoActual = null
        contrasenaActual = null
    }
}
