
package com.example.induztek.data.repository

import com.example.induztek.data.local.dao.VisitaDao
import com.example.induztek.data.local.entity.VisitaEntity
import com.example.induztek.data.local.entity.VisitaTecnicoEntity
import com.example.induztek.model.Rol
import com.example.induztek.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
                id = "1",
                nombreCompleto = "Usuario Prueba",
                rut = "11111111-1",
                correo = "prueba@induztek.cl",
                rol = rol,
                activo = activo,
                clienteId = clienteId
            )
        )
    }

    private fun crearVisita(
        id: Long,
        clienteId: String = "CLI-01"
    ) = VisitaEntity(
        id = id,
        clienteId = clienteId,
        instalacionId = "INS-01",
        fechaMillis = 1000L,
        numeroOt = "OT-$id",
        tipoVisita = "SOLO_PRUEBAS",
        ingenieroCargoId = "ING-01",
        observaciones = "Prueba"
    )

    @Test
    fun sinSesion_noObservaVisitas() = runTest {
        dao.agregar(crearVisita(1))

        val visitas = repository.observarVisitas().first()

        assertTrue(visitas.isEmpty())
    }

    @Test
    fun usuarioInactivo_noObservaVisitas() = runTest {
        iniciarSesion(activo = false)
        dao.agregar(crearVisita(1))

        val visitas = repository.observarVisitas().first()

        assertTrue(visitas.isEmpty())
    }

    @Test
    fun clienteSinId_noObservaVisitas() = runTest {
        iniciarSesion(rol = Rol.CLIENTE)
        dao.agregar(crearVisita(1))

        val visitas = repository.observarVisitas().first()

        assertTrue(visitas.isEmpty())
    }

    @Test
    fun clienteSoloObservaSusVisitas() = runTest {
        iniciarSesion(
            rol = Rol.CLIENTE,
            clienteId = "CLI-01"
        )

        dao.agregar(crearVisita(1, "CLI-01"))
        dao.agregar(crearVisita(2, "CLI-02"))

        val visitas = repository.observarVisitas().first()

        assertEquals(1, visitas.size)
        assertEquals("CLI-01", visitas.first().clienteId)
    }

    @Test
    fun tecnicoObservaTodasLasVisitas() = runTest {
        iniciarSesion()

        dao.agregar(crearVisita(1, "CLI-01"))
        dao.agregar(crearVisita(2, "CLI-02"))

        val visitas = repository.observarVisitas().first()

        assertEquals(2, visitas.size)
    }

    @Test
    fun clienteNoPuedeConsultarVisitaAjena() = runTest {
        iniciarSesion(
            rol = Rol.CLIENTE,
            clienteId = "CLI-01"
        )

        dao.agregar(crearVisita(1, "CLI-02"))

        val resultado = repository.obtenerVisitaPorId(1)

        assertNull(resultado)
    }

    @Test
    fun clientePuedeConsultarSuVisita() = runTest {
        iniciarSesion(
            rol = Rol.CLIENTE,
            clienteId = "CLI-01"
        )

        dao.agregar(crearVisita(1, "CLI-01"))

        val resultado = repository.obtenerVisitaPorId(1)

        assertNotNull(resultado)
        assertEquals("CLI-01", resultado?.clienteId)
    }

    @Test
    fun registrarVisita_guardaTecnicos() = runTest {
        val nuevaVisita = crearVisita(0)

        val id = repository.registrarVisita(
            nuevaVisita,
            listOf("TEC-01", "TEC-02")
        )

        assertEquals(1L, id)
        assertEquals(1, dao.visitas.size)
        assertEquals(
            listOf("TEC-01", "TEC-02"),
            dao.obtenerTecnicosDeVisita(1L)
        )
    }

    @Test
    fun sinSesion_noConsultaTecnicos() = runTest {
        dao.agregar(crearVisita(1))

        val tecnicos = repository.obtenerTecnicosDeVisita(1)

        assertTrue(tecnicos.isEmpty())
    }

    private class VisitaDaoPrueba : VisitaDao {

        val visitas = mutableListOf<VisitaEntity>()

        private val flujo =
            MutableStateFlow<List<VisitaEntity>>(emptyList())

        private val asociaciones =
            mutableListOf<VisitaTecnicoEntity>()

        fun agregar(visita: VisitaEntity) {
            visitas.add(visita)
            flujo.value = visitas.toList()
        }

        override suspend fun insertarVisita(
            visita: VisitaEntity
        ): Long {
            val id = visitas.size.toLong() + 1L
            agregar(visita.copy(id = id))
            return id
        }

        override suspend fun insertarVisitaTecnicos(
            visitaTecnicos: List<VisitaTecnicoEntity>
        ) {
            asociaciones.addAll(visitaTecnicos)
        }

        override fun obtenerVisitas(): Flow<List<VisitaEntity>> {
            return flujo
        }

        override fun obtenerVisitasPorCliente(
            clienteId: String
        ): Flow<List<VisitaEntity>> {
            return flujo.map { lista ->
                lista.filter { it.clienteId == clienteId }
            }
        }

        override suspend fun obtenerVisitaPorId(
            id: Long
        ): VisitaEntity? {
            return visitas.firstOrNull { it.id == id }
        }

        override suspend fun obtenerTecnicosDeVisita(
            visitaId: Long
        ): List<String> {
            return asociaciones
                .filter { it.visitaId == visitaId }
                .map { it.tecnicoId }
        }
    }
}
