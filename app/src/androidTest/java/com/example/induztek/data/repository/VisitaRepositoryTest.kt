package com.example.induztek.data.repository

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.induztek.data.local.AppDatabase
import com.example.induztek.data.local.entity.VisitaEntity
import com.example.induztek.model.Rol
import com.example.induztek.model.Usuario
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VisitaRepositoryTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: VisitaRepository

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = VisitaRepository(db.visitaDao())
        SesionRepository.establecerUsuario(
            Usuario(
                id = "ING-01",
                nombreCompleto = "Andrea Contreras Silva",
                rut = "15.906.334-8",
                correo = "acontreras@induztek.ejemplo",
                rol = Rol.INGENIERO,
                activo = true
            )
        )
    }

    @After
    fun tearDown() {
        SesionRepository.cerrarSesion()
        db.close()
    }

    @Test
    fun registrarVisita_conTecnicos_guardaCorrectamente() {
        runBlocking {
            val visita = VisitaEntity(
                clienteId = "CLI-01",
                instalacionId = "INS-01",
                fechaMillis = 1711929600000L,
                numeroOt = "OT-REP-01",
                tipoVisita = "SOLO_PRUEBAS",
                ingenieroCargoId = "ING-01",
                observaciones = "Prueba repositorio"
            )

            val id = repository.registrarVisita(visita, listOf("TEC-01", "TEC-02"))
            assertTrue(id > 0)

            val recuperada = repository.obtenerVisitaPorId(id)
            assertNotNull(recuperada)
            assertEquals("OT-REP-01", recuperada?.numeroOt)

            val tecnicos = repository.obtenerTecnicosDeVisita(id)
            assertEquals(2, tecnicos.size)
            assertTrue(tecnicos.contains("TEC-01"))
            assertTrue(tecnicos.contains("TEC-02"))

            val todas = repository.observarVisitas().first()
            assertEquals(1, todas.size)
        }
    }

    @Test
    fun registrarVisita_otDuplicada_lanzaExcepcion() {
        runBlocking {
            val visita1 = VisitaEntity(
                clienteId = "CLI-01",
                instalacionId = "INS-01",
                fechaMillis = 1711929600000L,
                numeroOt = "OT-REP-DUP",
                tipoVisita = "SOLO_PRUEBAS",
                ingenieroCargoId = "ING-01",
                observaciones = "Obs 1"
            )
            repository.registrarVisita(visita1, emptyList())

            val visita2 = VisitaEntity(
                clienteId = "CLI-02",
                instalacionId = "INS-02",
                fechaMillis = 1711929600000L,
                numeroOt = "OT-REP-DUP",
                tipoVisita = "PRUEBAS_Y_MANT",
                ingenieroCargoId = "ING-02",
                observaciones = "Obs 2"
            )

            assertThrows(SQLiteConstraintException::class.java) {
                runBlocking {
                    repository.registrarVisita(visita2, emptyList())
                }
            }
        }
    }

    @Test
    fun cliente_soloConsultaVisitasDeSuCliente() {
        runBlocking {
            val visita1 = VisitaEntity(
                clienteId = "CLI-01",
                instalacionId = "INS-01",
                fechaMillis = 1711929600000L,
                numeroOt = "OT-CLI-01",
                tipoVisita = "SOLO_PRUEBAS",
                ingenieroCargoId = "ING-01",
                observaciones = "Obs 1"
            )
            val visita2 = VisitaEntity(
                clienteId = "CLI-02",
                instalacionId = "INS-02",
                fechaMillis = 1711929600000L,
                numeroOt = "OT-CLI-02",
                tipoVisita = "SOLO_PRUEBAS",
                ingenieroCargoId = "ING-01",
                observaciones = "Obs 2"
            )
            repository.registrarVisita(visita1, emptyList())
            repository.registrarVisita(visita2, emptyList())

            SesionRepository.establecerUsuario(
                Usuario(
                    id = "USR-CLI-01",
                    nombreCompleto = "Marcela Fuentes",
                    rut = "14.226.791-8",
                    correo = "mfuentes@envpacifico.ejemplo",
                    rol = Rol.CLIENTE,
                    activo = true,
                    clienteId = "CLI-01"
                )
            )

            val visitasCliente = repository.observarVisitas().first()
            assertEquals(1, visitasCliente.size)
            assertEquals("OT-CLI-01", visitasCliente[0].numeroOt)
            assertNotNull(repository.obtenerVisitaPorId(1L))
            assertNull(repository.obtenerVisitaPorId(2L))
        }
    }

    @Test
    fun sesionInactivaONula_retornaListaVacia() {
        runBlocking {
            val visita = VisitaEntity(
                clienteId = "CLI-01",
                instalacionId = "INS-01",
                fechaMillis = 1711929600000L,
                numeroOt = "OT-INACTIVA",
                tipoVisita = "SOLO_PRUEBAS",
                ingenieroCargoId = "ING-01",
                observaciones = "Obs"
            )
            repository.registrarVisita(visita, emptyList())

            SesionRepository.cerrarSesion()
            val visitasNulas = repository.observarVisitas().first()
            assertTrue(visitasNulas.isEmpty())
            assertNull(repository.obtenerVisitaPorId(1L))
            assertTrue(repository.obtenerTecnicosDeVisita(1L).isEmpty())

            SesionRepository.establecerUsuario(
                Usuario(
                    id = "TEC-INACTIVO",
                    nombreCompleto = "Inactivo",
                    rut = "11.111.111-1",
                    correo = "inactivo@induztek.ejemplo",
                    rol = Rol.TECNICO,
                    activo = false
                )
            )
            val visitasInactivas = repository.observarVisitas().first()
            assertTrue(visitasInactivas.isEmpty())
        }
    }
}
