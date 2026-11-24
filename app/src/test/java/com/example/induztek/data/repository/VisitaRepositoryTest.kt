package com.example.induztek.data.repository

import com.example.induztek.data.local.dao.VisitaDao
import com.example.induztek.data.local.entity.VisitaEntity
import com.example.induztek.data.local.entity.VisitaTecnicoEntity
import com.example.induztek.model.Rol
import com.example.induztek.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class VisitaRepositoryTest {

    private lateinit var dao: VisitaDaoPrueba
    private lateinit var repository: VisitaRepository

    @Before
    fun preparar() {
        SesionRepository.cerrarSesion()
        dao = VisitaDaoPrueba()
        repository = VisitaRepository(dao)
        iniciarSesion()
    }

    @After
    fun finalizar() {
        SesionRepository.cerrarSesion()
    }

    private fun iniciarSesion(
        rol: Rol = Rol.TECNICO,
        activo: Boolean = true,
        clienteId: String? = null
    ) {
        SesionRepository.establecerUsuario(
            Usuario(
                id = "USR-01",
                nombreCompleto = "Usuario de prueba",
                rut = "12345678-9",
                correo = "prueba@induztek.local",
                rol = rol,
                activo = activo,
                clienteId = clienteId
            )
        )
    }

    private fun visita(
        id: Long = 0L,
        clienteId: String = "CLI-01"
    ) = VisitaEntity(
        id = id,
        clienteId = clienteId,
        instalacionId = "INS-01",
        fechaMillis = 1795392000000L,
        numeroOt = "OT-${if (id == 0L) 1 else id}",
        tipoVisita = "SOLO_PRUEBAS",
        ingenieroCargoId = "ING-01",
        observaciones = "Visita de prueba"
    )

    @Test
    fun registrarVisitaGuardaDatos() = runTest {
        val id = repository.registrarVisita(
            visita(),
            listOf("TEC-01")
        )

        assertEquals(1L, id)
        assertEquals(1, dao.visitas.size)
        assertEquals("CLI-01", dao.visitas.first().clienteId)
    }

    @Test
    fun registrarVisitaGuardaTecnicos() = runTest {
        val id = repository.registrarVisita(
            visita(),
            listOf("TEC-01", "TEC-02")
        )

        val tecnicos = repository.obtenerTecnicosDeVisita(id)

        assertEquals(2, tecnicos.size)
        assertTrue(tecnicos.contains("TEC-01"))
        assertTrue(tecnicos.contains("TEC-02"))
    }

    @Test
    fun registrarVisitaSinTecnicos() = runTest {
        val id = repository.registrarVisita(
            visita(),
            emptyList()
        )

        assertEquals(1L, id)
        assertTrue(dao.tecnicos.isEmpty())
    }

    @Test
    fun registrarVariasVisitas() = runTest {
        repository.registrarVisita(visita(), emptyList())
        repository.registrarVisita(
            visita(clienteId = "CLI-02"),
            emptyList()
        )

        assertEquals(2, dao.visitas.size)
    }

    @Test
    fun tecnicoPuedeVerTodasLasVisitas() = runTest {
        dao.agregar(
            visita(1L, "CLI-01"),
            visita(2L, "CLI-02")
        )

        val resultado = repository.observarVisitas().first()

        assertEquals(2, resultado.size)
    }

    @Test
    fun ingenieroPuedeVerTodasLasVisitas() = runTest {
        iniciarSesion(Rol.INGENIERO)

        dao.agregar(
            visita(1L, "CLI-01"),
            visita(2L, "CLI-02")
        )

        assertEquals(2, repository.observarVisitas().first().size)
    }

    @Test
    fun clienteVeSoloSusVisitas() = runTest {
        iniciarSesion(Rol.CLIENTE, clienteId = "CLI-01")

        dao.agregar(
            visita(1L, "CLI-01"),
            visita(2L, "CLI-02")
        )

        val resultado = repository.observarVisitas().first()

        assertEquals(1, resultado.size)
        assertEquals("CLI-01", resultado.first().clienteId)
    }

    @Test
    fun clienteSinIdNoVeVisitas() = runTest {
        iniciarSesion(Rol.CLIENTE)

        dao.agregar(visita(1L))

        assertTrue(repository.observarVisitas().first().isEmpty())
    }

    @Test
    fun clienteConIdVacioNoVeVisitas() = runTest {
        iniciarSesion(Rol.CLIENTE, clienteId = " ")

        dao.agregar(visita(1L))

        assertTrue(repository.observarVisitas().first().isEmpty())
    }

    @Test
    fun sinSesionNoVeVisitas() = runTest {
        SesionRepository.cerrarSesion()

        dao.agregar(visita(1L))

        assertTrue(repository.observarVisitas().first().isEmpty())
    }

    @Test
    fun usuarioInactivoNoVeVisitas() = runTest {
        iniciarSesion(activo = false)

        dao.agregar(visita(1L))

        assertTrue(repository.observarVisitas().first().isEmpty())
    }

    @Test
    fun obtenerVisitaExistente() = runTest {
        dao.agregar(visita(1L))

        val resultado = repository.obtenerVisitaPorId(1L)

        assertNotNull(resultado)
        assertEquals("CLI-01", resultado?.clienteId)
    }

    @Test
    fun obtenerVisitaInexistente() = runTest {
        val resultado = repository.obtenerVisitaPorId(999L)

        assertNull(resultado)
    }

    @Test
    fun sinSesionNoPuedeConsultarVisita() = runTest {
        dao.agregar(visita(1L))

        SesionRepository.cerrarSesion()

        assertNull(repository.obtenerVisitaPorId(1L))
    }

    @Test
    fun usuarioInactivoNoPuedeConsultarVisita() = runTest {
        dao.agregar(visita(1L))

        iniciarSesion(activo = false)

        assertNull(repository.obtenerVisitaPorId(1L))
    }

    @Test
    fun clientePuedeConsultarSuVisita() = runTest {
        iniciarSesion(Rol.CLIENTE, clienteId = "CLI-01")
        dao.agregar(visita(1L, "CLI-01"))

        assertNotNull(repository.obtenerVisitaPorId(1L))
    }

    @Test
    fun clienteNoPuedeConsultarVisitaAjena() = runTest {
        iniciarSesion(Rol.CLIENTE, clienteId = "CLI-01")
        dao.agregar(visita(1L, "CLI-02"))

        assertNull(repository.obtenerVisitaPorId(1L))
    }

    @Test
    fun sinSesionNoPuedeConsultarTecnicos() = runTest {
        val id = repository.registrarVisita(
            visita(),
            listOf("TEC-01")
        )

        SesionRepository.cerrarSesion()

        assertTrue(repository.obtenerTecnicosDeVisita(id).isEmpty())
    }

    @Test
    fun usuarioInactivoNoPuedeConsultarTecnicos() = runTest {
        val id = repository.registrarVisita(
            visita(),
            listOf("TEC-01")
        )

        iniciarSesion(activo = false)

        assertTrue(repository.obtenerTecnicosDeVisita(id).isEmpty())
    }

    @Test
    fun clientePuedeConsultarTecnicosDeSuVisita() = runTest {
        val id = repository.registrarVisita(
            visita(clienteId = "CLI-01"),
            listOf("TEC-01")
        )

        iniciarSesion(Rol.CLIENTE, clienteId = "CLI-01")

        assertEquals(
            listOf("TEC-01"),
            repository.obtenerTecnicosDeVisita(id)
        )
    }

    @Test
    fun clienteNoPuedeConsultarTecnicosDeVisitaAjena() = runTest {
        val id = repository.registrarVisita(
            visita(clienteId = "CLI-02"),
            listOf("TEC-01")
        )

        iniciarSesion(Rol.CLIENTE, clienteId = "CLI-01")

        assertTrue(repository.obtenerTecnicosDeVisita(id).isEmpty())
    }

    @Test
    fun clienteNoPuedeConsultarTecnicosDeVisitaInexistente() = runTest {
        iniciarSesion(Rol.CLIENTE, clienteId = "CLI-01")

        assertTrue(repository.obtenerTecnicosDeVisita(999L).isEmpty())
    }

    private class VisitaDaoPrueba : VisitaDao {

        val visitas = mutableListOf<VisitaEntity>()
        val tecnicos = mutableListOf<VisitaTecnicoEntity>()

        fun agregar(vararg registros: VisitaEntity) {
            visitas.addAll(registros)
        }

        override suspend fun insertarVisita(
            visita: VisitaEntity
        ): Long {
            val id = (visitas.maxOfOrNull { it.id } ?: 0L) + 1L
            visitas.add(visita.copy(id = id))
            return id
        }

        override suspend fun insertarVisitaTecnicos(
            visitaTecnicos: List<VisitaTecnicoEntity>
        ) {
            tecnicos.addAll(visitaTecnicos)
        }

        override fun obtenerVisitas(): Flow<List<VisitaEntity>> {
            return flowOf(visitas.toList())
        }

        override fun obtenerVisitasPorCliente(
            clienteId: String
        ): Flow<List<VisitaEntity>> {
            return flowOf(
                visitas.filter { it.clienteId == clienteId }
            )
        }

        override suspend fun obtenerVisitaPorId(
            id: Long
        ): VisitaEntity? {
            return visitas.firstOrNull { it.id == id }
        }

        override suspend fun obtenerTecnicosDeVisita(
            visitaId: Long
        ): List<String> {
            return tecnicos
                .filter { it.visitaId == visitaId }
                .map { it.tecnicoId }
        }
    }
}
