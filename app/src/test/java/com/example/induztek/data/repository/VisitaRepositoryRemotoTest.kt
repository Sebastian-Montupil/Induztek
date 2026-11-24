
package com.example.induztek.data.repository

import com.example.induztek.data.local.dao.VisitaDao
import com.example.induztek.data.local.entity.VisitaEntity
import com.example.induztek.data.local.entity.VisitaTecnicoEntity
import com.example.induztek.data.remote.terreno.*
import com.example.induztek.model.Rol
import com.example.induztek.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class VisitaRepositoryRemotoTest {

    private lateinit var api: VisitasApiPrueba
    private lateinit var repository: VisitaRepository

    @Before
    fun preparar() {
        SesionRepository.cerrarSesion()
        api = VisitasApiPrueba()
        repository = VisitaRepository(
            VisitaDaoPrueba(),
            VisitasRemoteDataSource(api)
        )
    }

    @After
    fun finalizar() {
        SesionRepository.cerrarSesion()
    }

    private fun iniciarSesion(
        rol: Rol = Rol.TECNICO,
        clienteId: String? = null
    ) {
        SesionRepository.establecerUsuario(
            Usuario(
                id = "1",
                nombreCompleto = "Usuario Prueba",
                rut = "11111111-1",
                correo = "prueba@induztek.cl",
                rol = rol,
                activo = true,
                clienteId = clienteId
            )
        )
    }

    private fun respuesta(
        id: Long,
        clienteId: String = "CLI-01"
    ) = VisitaResponse(
        id = id,
        clienteId = clienteId,
        instalacionId = "INS-01",
        fechaMillis = 1000L,
        numeroOt = "OT-$id",
        tipoVisita = "SOLO_PRUEBAS",
        ingenieroCargoId = "ING-01",
        observaciones = "Visita remota"
    )

    @Test
    fun registrarVisita_enviaDatosAlBackend() = runTest {
        val visita = VisitaEntity(
            clienteId = "CLI-01",
            instalacionId = "INS-01",
            fechaMillis = 1000L,
            numeroOt = "OT-001",
            tipoVisita = "SOLO_PRUEBAS",
            ingenieroCargoId = "ING-01",
            observaciones = "Prueba remota"
        )

        api.respuestaRegistro = respuesta(10L)

        val id = repository.registrarVisita(
            visita,
            listOf("TEC-01", "TEC-02")
        )

        assertEquals(10L, id)
        assertEquals("CLI-01", api.ultimaSolicitud?.clienteId)
        assertEquals("OT-001", api.ultimaSolicitud?.numeroOt)
        assertEquals(
            listOf("TEC-01", "TEC-02"),
            api.ultimaSolicitud?.tecnicoIds
        )
    }

    @Test
    fun tecnicoObservaListadoRemoto() = runTest {
        iniciarSesion()
        api.visitas = listOf(
            respuesta(1, "CLI-01"),
            respuesta(2, "CLI-02")
        )

        val resultado = repository.observarVisitas().first()

        assertEquals(2, resultado.size)
        assertEquals(1L, resultado.first().id)
    }

    @Test
    fun clienteSoloObservaSusVisitasRemotas() = runTest {
        iniciarSesion(
            rol = Rol.CLIENTE,
            clienteId = "CLI-01"
        )

        api.visitas = listOf(
            respuesta(1, "CLI-01"),
            respuesta(2, "CLI-02")
        )

        val resultado = repository.observarVisitas().first()

        assertEquals(1, resultado.size)
        assertEquals("CLI-01", resultado.first().clienteId)
    }

    @Test
    fun clienteConsultaSuVisitaRemota() = runTest {
        iniciarSesion(
            rol = Rol.CLIENTE,
            clienteId = "CLI-01"
        )

        api.respuestaDetalle = respuesta(5, "CLI-01")

        val resultado = repository.obtenerVisitaPorId(5)

        assertNotNull(resultado)
        assertEquals(5L, resultado?.id)
    }

    @Test
    fun clienteNoConsultaVisitaAjenaRemota() = runTest {
        iniciarSesion(
            rol = Rol.CLIENTE,
            clienteId = "CLI-01"
        )

        api.respuestaDetalle = respuesta(5, "CLI-02")

        val resultado = repository.obtenerVisitaPorId(5)

        assertNull(resultado)
    }

    @Test
    fun visitaRemotaNoEncontrada_retornaNull() = runTest {
        iniciarSesion()
        api.error404 = true

        val resultado = repository.obtenerVisitaPorId(999)

        assertNull(resultado)
    }

    @Test
    fun errorDeConexion_sePropaga() = runTest {
        iniciarSesion()
        api.errorConexion = true

        try {
            repository.obtenerVisitaPorId(1)
            fail("Se esperaba IOException")
        } catch (e: IOException) {
            assertEquals("Sin conexión", e.message)
        }
    }

    @Test
    fun consultarTecnicosRemotos_indicaOperacionNoDisponible() =
        runTest {
            iniciarSesion()

            try {
                repository.obtenerTecnicosDeVisita(1)
                fail("Se esperaba UnsupportedOperationException")
            } catch (e: UnsupportedOperationException) {
                assertTrue(
                    e.message?.contains("todavía no permite") == true
                )
            }
        }

    private class VisitasApiPrueba : VisitasApi {

        var visitas = emptyList<VisitaResponse>()
        var respuestaRegistro: VisitaResponse? = null
        var respuestaDetalle: VisitaResponse? = null
        var ultimaSolicitud: RegistrarVisitaRequest? = null
        var error404 = false
        var errorConexion = false

        override suspend fun listarVisitas(): List<VisitaResponse> {
            if (errorConexion) throw IOException("Sin conexión")
            return visitas
        }

        override suspend fun obtenerVisita(
            id: Long
        ): VisitaResponse {
            if (errorConexion) throw IOException("Sin conexión")

            if (error404) {
                throw HttpException(
                    Response.error<Any>(
                        404,
                        "".toResponseBody()
                    )
                )
            }

            return respuestaDetalle
                ?: throw IllegalStateException("Sin datos de prueba")
        }

        override suspend fun registrarVisita(
            request: RegistrarVisitaRequest
        ): VisitaResponse {
            ultimaSolicitud = request
            if (errorConexion) throw IOException("Sin conexión")

            return respuestaRegistro
                ?: throw IllegalStateException("Sin respuesta simulada")
        }
    }

    private class VisitaDaoPrueba : VisitaDao {

        override suspend fun insertarVisita(
            visita: VisitaEntity
        ): Long = 1L

        override suspend fun insertarVisitaTecnicos(
            visitaTecnicos: List<VisitaTecnicoEntity>
        ) = Unit

        override fun obtenerVisitas(): Flow<List<VisitaEntity>> =
            flowOf(emptyList())

        override fun obtenerVisitasPorCliente(
            clienteId: String
        ): Flow<List<VisitaEntity>> =
            flowOf(emptyList())

        override suspend fun obtenerVisitaPorId(
            id: Long
        ): VisitaEntity? = null

        override suspend fun obtenerTecnicosDeVisita(
            visitaId: Long
        ): List<String> = emptyList()
    }
}
