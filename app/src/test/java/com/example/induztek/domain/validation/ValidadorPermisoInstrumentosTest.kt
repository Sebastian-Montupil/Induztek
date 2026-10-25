package com.example.induztek.domain.validation

import com.example.induztek.model.Rol
import com.example.induztek.model.Usuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadorPermisoInstrumentosTest {

    private fun crearUsuarioPrueba(
        rol: Rol,
        activo: Boolean = true,
    ): Usuario {
        return Usuario(
            id = "TEST-01",
            nombreCompleto = "Usuario Prueba",
            rut = "11.111.111-1",
            correo = "prueba@induztek.ejemplo",
            rol = rol,
            activo = activo,
        )
    }

    @Test
    fun sinSesion_retornaError() {
        val resultado = ValidadorPermisoInstrumentos.validarGestion(null)
        assertNotNull(resultado)
        assertEquals("Debe iniciar sesión para gestionar instrumentos y calibraciones.", resultado)
    }

    @Test
    fun jefeTecnicoInactivo_retornaError() {
        val usuario = crearUsuarioPrueba(Rol.JEFE_TECNICO, activo = false)
        val resultado = ValidadorPermisoInstrumentos.validarGestion(usuario)
        assertNotNull(resultado)
        assertEquals("El usuario se encuentra inactivo.", resultado)
    }

    @Test
    fun tecnicoActivo_retornaError() {
        val usuario = crearUsuarioPrueba(Rol.TECNICO, activo = true)
        val resultado = ValidadorPermisoInstrumentos.validarGestion(usuario)
        assertNotNull(resultado)
        assertEquals("Solo el Jefe técnico puede gestionar instrumentos y calibraciones.", resultado)
    }

    @Test
    fun ingenieroActivo_retornaError() {
        val usuario = crearUsuarioPrueba(Rol.INGENIERO, activo = true)
        val resultado = ValidadorPermisoInstrumentos.validarGestion(usuario)
        assertNotNull(resultado)
        assertEquals("Solo el Jefe técnico puede gestionar instrumentos y calibraciones.", resultado)
    }

    @Test
    fun clienteActivo_retornaError() {
        val usuario = crearUsuarioPrueba(Rol.CLIENTE, activo = true)
        val resultado = ValidadorPermisoInstrumentos.validarGestion(usuario)
        assertNotNull(resultado)
        assertEquals("Solo el Jefe técnico puede gestionar instrumentos y calibraciones.", resultado)
    }

    @Test
    fun jefeTecnicoActivo_retornaNull() {
        val usuario = crearUsuarioPrueba(Rol.JEFE_TECNICO, activo = true)
        val resultado = ValidadorPermisoInstrumentos.validarGestion(usuario)
        assertNull(resultado)
    }
}
