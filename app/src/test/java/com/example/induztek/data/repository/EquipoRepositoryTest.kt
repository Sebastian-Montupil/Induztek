
package com.example.induztek.data.repository

import com.example.induztek.data.local.dao.EquipoDao
import com.example.induztek.data.local.entity.EquipoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class EquipoRepositoryTest {

    private lateinit var dao: EquipoDaoPrueba
    private lateinit var repository: EquipoRepository

    @Before
    fun preparar() {
        dao = EquipoDaoPrueba()
        repository = EquipoRepository(dao)
    }

    private fun crearEquipo() = EquipoEntity(
        clienteId = "CLI-01",
        instalacionId = "INS-01",
        nombre = "Multimetro",
        tipo = "Medicion",
        marca = "Fluke",
        modelo = "87V",
        numeroSerie = "SER-001",
        codigoInterno = "EQ-001",
        observaciones = "Equipo de prueba"
    )

    private suspend fun verificarError(equipo: EquipoEntity) {
        try {
            repository.registrarEquipo(equipo)
            fail("Se esperaba IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertNotNull(e.message)
        }
    }

    @Test
    fun clienteInexistente_rechazaRegistro() = runTest {
        verificarError(crearEquipo().copy(clienteId = "CLI-999"))
        assertTrue(dao.equipos.isEmpty())
    }

    @Test
    fun instalacionIncorrecta_rechazaRegistro() = runTest {
        verificarError(crearEquipo().copy(instalacionId = "INS-02"))
        assertTrue(dao.equipos.isEmpty())
    }

    @Test
    fun nombreVacio_rechazaRegistro() = runTest {
        verificarError(crearEquipo().copy(nombre = " "))
    }

    @Test
    fun tipoVacio_rechazaRegistro() = runTest {
        verificarError(crearEquipo().copy(tipo = ""))
    }

    @Test
    fun marcaVacia_rechazaRegistro() = runTest {
        verificarError(crearEquipo().copy(marca = ""))
    }

    @Test
    fun numeroSerieVacio_rechazaRegistro() = runTest {
        verificarError(crearEquipo().copy(numeroSerie = ""))
    }

    @Test
    fun equipoValido_seRegistraCorrectamente() = runTest {
        val id = repository.registrarEquipo(crearEquipo())

        assertEquals(1L, id)
        assertEquals(1, dao.equipos.size)
        assertEquals("Multimetro", dao.equipos.first().nombre)
    }

    @Test
    fun observarEquipos_filtraPorInstalacion() = runTest {
        repository.registrarEquipo(crearEquipo())

        val resultado = repository
            .observarEquiposPorInstalacion("INS-01")
            .first()

        assertEquals(1, resultado.size)
        assertEquals("SER-001", resultado.first().numeroSerie)
    }

    @Test
    fun instalacionSinEquipos_retornaListaVacia() = runTest {
        val resultado = repository
            .observarEquiposPorInstalacion("INS-02")
            .first()

        assertTrue(resultado.isEmpty())
    }

    @Test
    fun buscarEquipoExistente_retornaEquipo() = runTest {
        val id = repository.registrarEquipo(crearEquipo())

        val resultado = repository.obtenerEquipoPorId(id)

        assertNotNull(resultado)
        assertEquals("Fluke", resultado?.marca)
    }

    @Test
    fun buscarEquipoInexistente_retornaNull() = runTest {
        val resultado = repository.obtenerEquipoPorId(999L)

        assertNull(resultado)
    }

    private class EquipoDaoPrueba : EquipoDao {

        val equipos = mutableListOf<EquipoEntity>()

        override suspend fun insertarEquipo(equipo: EquipoEntity): Long {
            val id = equipos.size.toLong() + 1
            equipos.add(equipo.copy(id = id))
            return id
        }

        override fun observarEquiposPorInstalacion(
            instalacionId: String
        ): Flow<List<EquipoEntity>> {
            return flowOf(
                equipos.filter { it.instalacionId == instalacionId }
            )
        }

        override suspend fun obtenerEquipoPorId(
            id: Long
        ): EquipoEntity? {
            return equipos.firstOrNull { it.id == id }
        }
    }
}
