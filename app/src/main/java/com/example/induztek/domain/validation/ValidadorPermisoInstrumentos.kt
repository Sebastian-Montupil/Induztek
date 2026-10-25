package com.example.induztek.domain.validation

import com.example.induztek.model.Rol
import com.example.induztek.model.Usuario

object ValidadorPermisoInstrumentos {

    fun validarGestion(usuario: Usuario?): String? {
        if (usuario == null) {
            return "Debe iniciar sesión para gestionar instrumentos y calibraciones."
        }
        if (!usuario.activo) {
            return "El usuario se encuentra inactivo."
        }
        if (usuario.rol != Rol.JEFE_TECNICO) {
            return "Solo el Jefe técnico puede gestionar instrumentos y calibraciones."
        }
        return null
    }
}
