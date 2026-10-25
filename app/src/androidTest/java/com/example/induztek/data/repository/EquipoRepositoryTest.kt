package com.example.induztek.data.repository

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.induztek.data.local.AppDatabase
import com.example.induztek.data.local.entity.EquipoEntity
import com.example.induztek.model.Rol
import com.example.induztek.model.Usuario
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EquipoRepositoryTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: EquipoRepository

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = EquipoRepository(db.equipoDao())
        SesionRepository.cerrarSesion()
    }

    @After
    fun tearDown() {
        SesionRepository.cerrarSesion()
        db.close()
    }

    @Test
    fun registrarEquipo_valido_guardaEnRoom() {
        runBlocking {
            val equipo = EquipoEntity(
                clienteId = "CLI-01",
                instalacionId = "INS-01",
                nombre = "Tablero General",
                tipo = "Tablero",
                marca = "Schneider",
                modelo = "Prisma",
                numeroSerie = "SN-EQ-VAL-01",
                codigoInterno = "TG-01",
                observaciones = "Operativo",
            )

            val id = repository.registrarEquipo(equipo)
            assertTrue(id > 0)

            val guardado = repository.obtenerEquipoPorId(id)
            assertNotNull(guardado)
            assertEquals("Tablero General", guardado?.nombre)
            assertEquals("CLI-01", guardado?.clienteId)
            assertEquals("INS-01", guardado?.instalacionId)
        }
    }

    @Test
    fun registrarEquipo_clienteInexistente_lanzaExcepcion() {
        runBlocking {
            val equipo = EquipoEntity(
                clienteId = "CLI-999",
                instalacionId = "INS-01",
                nombre = "Tablero",
                tipo = "Tablero",
                marca = "ABB",
                modelo = "M1",
                numeroSerie = "SN-CLI-INV",
                codigoInterno = "T-01",
                observaciones = "Test",
            )

            assertThrows(IllegalArgumentException::class.java) {
                runBlocking {
                    repository.registrarEquipo(equipo)
                }
            }

            val lista = repository.observarEquiposPorInstalacion("INS-01").first()
            assertEquals(0, lista.size)
        }
    }

    @Test
    fun registrarEquipo_instalacionDeOtroCliente_lanzaExcepcion() {
        runBlocking {
            val equipo = EquipoEntity(
                clienteId = "CLI-01",
                instalacionId = "INS-02",
                nombre = "Tablero",
                tipo = "Tablero",
                marca = "ABB",
                modelo = "M1",
                numeroSerie = "SN-INST-INV",
                codigoInterno = "T-02",
                observaciones = "Test",
            )

            assertThrows(IllegalArgumentException::class.java) {
                runBlocking {
                    repository.registrarEquipo(equipo)
                }
            }

            val lista = repository.observarEquiposPorInstalacion("INS-02").first()
            assertEquals(0, lista.size)
        }
    }

    @Test
    fun registrarEquipo_camposObligatoriosVacios_lanzaExcepcion() {
        runBlocking {
            val baseEquipo = EquipoEntity(
                clienteId = "CLI-01",
                instalacionId = "INS-01",
                nombre = "Nombre",
                tipo = "Tipo",
                marca = "Marca",
                modelo = "Modelo",
                numeroSerie = "SN-BLANK-TEST",
                codigoInterno = "C-01",
                observaciones = "Obs",
            )

            val nombresInvalidos = listOf("", "   ")
            for (n in nombresInvalidos) {
                assertThrows(IllegalArgumentException::class.java) {
                    runBlocking {
                        repository.registrarEquipo(baseEquipo.copy(nombre = n))
                    }
                }
            }

            val tiposInvalidos = listOf("", "   ")
            for (t in tiposInvalidos) {
                assertThrows(IllegalArgumentException::class.java) {
                    runBlocking {
                        repository.registrarEquipo(baseEquipo.copy(tipo = t))
                    }
                }
            }

            val marcasInvalidas = listOf("", "   ")
            for (m in marcasInvalidas) {
                assertThrows(IllegalArgumentException::class.java) {
                    runBlocking {
                        repository.registrarEquipo(baseEquipo.copy(marca = m))
                    }
                }
            }

            val seriesInvalidas = listOf("", "   ")
            for (s in seriesInvalidas) {
                assertThrows(IllegalArgumentException::class.java) {
                    runBlocking {
                        repository.registrarEquipo(baseEquipo.copy(numeroSerie = s))
                    }
                }
            }

            val lista = repository.observarEquiposPorInstalacion("INS-01").first()
            assertEquals(0, lista.size)
        }
    }

    @Test
    fun registrarEquipo_numeroSerieDuplicado_esRechazado() {
        runBlocking {
            val equipo1 = EquipoEntity(
                clienteId = "CLI-01",
                instalacionId = "INS-01",
                nombre = "Equipo 1",
                tipo = "Transformador",
                marca = "ABB",
                modelo = "M1",
                numeroSerie = "SN-DUP-999",
                codigoInterno = "E-01",
                observaciones = "1",
            )
            val id1 = repository.registrarEquipo(equipo1)
            assertTrue(id1 > 0)

            val equipo2 = EquipoEntity(
                clienteId = "CLI-01",
                instalacionId = "INS-01",
                nombre = "Equipo 2",
                tipo = "Transformador",
                marca = "ABB",
                modelo = "M1",
                numeroSerie = "SN-DUP-999",
                codigoInterno = "E-02",
                observaciones = "2",
            )

            assertThrows(SQLiteConstraintException::class.java) {
                runBlocking {
                    repository.registrarEquipo(equipo2)
                }
            }

            val lista = repository.observarEquiposPorInstalacion("INS-01").first()
            assertEquals(1, lista.size)
            assertEquals("Equipo 1", lista[0].nombre)
        }
    }

    @Test
    fun observarEquiposPorInstalacion_filtraCorrectamente() {
        runBlocking {
            val eq1 = EquipoEntity(
                clienteId = "CLI-01",
                instalacionId = "INS-01",
                nombre = "Equipo Ins 1",
                tipo = "Tablero",
                marca = "Schneider",
                modelo = "M1",
                numeroSerie = "SN-INS-01",
                codigoInterno = "I1",
                observaciones = "1",
            )
            val eq2 = EquipoEntity(
                clienteId = "CLI-02",
                instalacionId = "INS-02",
                nombre = "Equipo Ins 2",
                tipo = "Tablero",
                marca = "Schneider",
                modelo = "M2",
                numeroSerie = "SN-INS-02",
                codigoInterno = "I2",
                observaciones = "2",
            )

            repository.registrarEquipo(eq1)
            repository.registrarEquipo(eq2)

            val listaIns1 = repository.observarEquiposPorInstalacion("INS-01").first()
            assertEquals(1, listaIns1.size)
            assertEquals("Equipo Ins 1", listaIns1[0].nombre)

            val listaIns2 = repository.observarEquiposPorInstalacion("INS-02").first()
            assertEquals(1, listaIns2.size)
            assertEquals("Equipo Ins 2", listaIns2[0].nombre)
        }
    }

    @Test
    fun tecnicoActivo_puedeRegistrarEquipo() {
        runBlocking {
            SesionRepository.establecerUsuario(
                Usuario(
                    id = "TEC-01",
                    nombreCompleto = "Matías Godoy Herrera",
                    rut = "17.284.519-3",
                    correo = "mgodoy@induztek.ejemplo",
                    rol = Rol.TECNICO,
                    activo = true,
                ),
            )

            val equipo = EquipoEntity(
                clienteId = "CLI-01",
                instalacionId = "INS-01",
                nombre = "Equipo Tecnico",
                tipo = "Tablero",
                marca = "ABB",
                modelo = "M1",
                numeroSerie = "SN-TEC-OK",
                codigoInterno = "T-01",
                observaciones = "OK",
            )

            val id = repository.registrarEquipo(equipo)
            assertTrue(id > 0)

            val lista = repository.observarEquiposPorInstalacion("INS-01").first()
            assertEquals(1, lista.size)
        }
    }
}
