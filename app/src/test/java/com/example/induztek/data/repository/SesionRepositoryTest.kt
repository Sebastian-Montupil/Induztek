package com.example.induztek.data.repository

import com.example.induztek.model.Rol
import com.example.induztek.model.Usuario
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SesionRepositoryTest {

    @Before
    fun setUp() {
        SesionRepository.cerrarSesion()
    }

    @After
    fun tearDown() {
        SesionRepository.cerrarSesion()
    }

    @Test
    fun sesionInicial_retornaNull() {
        assertNull(SesionRepository.usuarioActual.value)
    }

    @Test
    fun establecerUsuario_actualizaSesion() {
        val usuarioPrueba = Usuario(
            id = "U-01",
            nombreCompleto = "Juan Pérez",
            rut = "12.345.678-9",
            correo = "juan.perez@induztek.cl",
            rol = Rol.TECNICO,
            activo = true,
        )

        SesionRepository.establecerUsuario(usuarioPrueba)

        assertEquals(usuarioPrueba, SesionRepository.usuarioActual.value)
    }

    @Test
    fun cerrarSesion_retornaNull() {
        val usuarioPrueba = Usuario(
            id = "U-02",
            nombreCompleto = "María González",
            rut = "98.765.432-1",
            correo = "maria.gonzalez@induztek.cl",
            rol = Rol.JEFE_TECNICO,
            activo = true,
        )

        SesionRepository.establecerUsuario(usuarioPrueba)
        SesionRepository.cerrarSesion()

        assertNull(SesionRepository.usuarioActual.value)
    }

    @Test
    fun establecerOtroUsuario_reemplazaSesion() {
        val usuarioUno = Usuario(
            id = "U-01",
            nombreCompleto = "Juan Pérez",
            rut = "12.345.678-9",
            correo = "juan.perez@induztek.cl",
            rol = Rol.TECNICO,
            activo = true,
        )

        val usuarioDos = Usuario(
            id = "U-03",
            nombreCompleto = "Carlos Soto",
            rut = "11.222.333-4",
            correo = "carlos.soto@induztek.cl",
            rol = Rol.INGENIERO,
            activo = true,
        )

        SesionRepository.establecerUsuario(usuarioUno)
        SesionRepository.establecerUsuario(usuarioDos)

        assertEquals(usuarioDos, SesionRepository.usuarioActual.value)
    }
}
