package com.example.induztek.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogoUsuariosTest {

    @Test
    fun catalogoContieneDiezUsuarios() {
        val usuarios = CatalogoUsuarios.usuarios
        assertEquals(10, usuarios.size)

        val tecnicos = usuarios.count { it.rol == Rol.TECNICO }
        val ingenieros = usuarios.count { it.rol == Rol.INGENIERO }
        val jefes = usuarios.count { it.rol == Rol.JEFE_TECNICO }
        val clientes = usuarios.count { it.rol == Rol.CLIENTE }

        assertEquals(4, tecnicos)
        assertEquals(2, ingenieros)
        assertEquals(1, jefes)
        assertEquals(3, clientes)
    }

    @Test
    fun identificadoresRutYCorreosSonUnicos() {
        val usuarios = CatalogoUsuarios.usuarios
        val ids = usuarios.map { it.id }.toSet()
        val ruts = usuarios.map { it.rut }.toSet()
        val correos = usuarios.map { it.correo }.toSet()

        assertEquals(usuarios.size, ids.size)
        assertEquals(usuarios.size, ruts.size)
        assertEquals(usuarios.size, correos.size)
    }

    @Test
    fun usuariosClientePertenecenAEmpresaCorrecta() {
        val clientesValidos = CatalogoClientes.clientes.map { it.id }.toSet()

        val cliente1 = CatalogoUsuarios.usuarios.find { it.id == "USR-CLI-01" }
        val cliente2 = CatalogoUsuarios.usuarios.find { it.id == "USR-CLI-02" }
        val cliente3 = CatalogoUsuarios.usuarios.find { it.id == "USR-CLI-03" }

        assertNotNull(cliente1)
        assertNotNull(cliente2)
        assertNotNull(cliente3)

        assertEquals("CLI-01", cliente1?.clienteId)
        assertEquals("CLI-04", cliente2?.clienteId)
        assertEquals("CLI-03", cliente3?.clienteId)

        val clientesUsuarios = CatalogoUsuarios.usuarios.filter { it.rol == Rol.CLIENTE }
        for (u in clientesUsuarios) {
            assertTrue(clientesValidos.contains(u.clienteId))
        }
    }

    @Test
    fun trabajadoresInternosNoTienenCliente() {
        val internos = CatalogoUsuarios.usuarios.filter { it.rol != Rol.CLIENTE }
        assertEquals(7, internos.size)

        for (u in internos) {
            assertNull(u.clienteId)
        }

        for (u in CatalogoUsuarios.usuarios) {
            assertTrue(u.activo)
        }
    }

    @Test
    fun personalMantieneIdentificadoresYNombres() {
        val tecnicosCatalogo = CatalogoUsuarios.usuarios.filter { it.rol == Rol.TECNICO }
        val ingenierosCatalogo = CatalogoUsuarios.usuarios.filter { it.rol == Rol.INGENIERO }

        assertEquals(CatalogoPersonal.tecnicos.size, tecnicosCatalogo.size)
        assertEquals(CatalogoPersonal.ingenieros.size, ingenierosCatalogo.size)

        for (personal in CatalogoPersonal.tecnicos) {
            val usuario = tecnicosCatalogo.find { it.id == personal.id }
            assertNotNull(usuario)
            assertEquals(personal.nombreCompleto, usuario?.nombreCompleto)
            assertEquals(Rol.TECNICO, usuario?.rol)
        }

        for (personal in CatalogoPersonal.ingenieros) {
            val usuario = ingenierosCatalogo.find { it.id == personal.id }
            assertNotNull(usuario)
            assertEquals(personal.nombreCompleto, usuario?.nombreCompleto)
            assertEquals(Rol.INGENIERO, usuario?.rol)
        }
    }
}
