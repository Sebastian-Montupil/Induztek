package com.example.induztek.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.induztek.data.local.AppDatabase
import com.example.induztek.data.local.entity.CalibracionEntity
import com.example.induztek.data.local.entity.InstrumentoEntity
import com.example.induztek.model.Rol
import com.example.induztek.model.Usuario
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalibracionRepositoryTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: CalibracionRepository
    private var instrumentoId: Long = 0L

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CalibracionRepository(db.calibracionDao())
        SesionRepository.cerrarSesion()

        runBlocking {
            instrumentoId = db.instrumentoDao().insertarInstrumento(
                InstrumentoEntity(
                    nombre = "Multímetro Prueba",
                    tipoInstrumento = "Instrumento",
                    marca = "Fluke",
                    modelo = "87V",
                    numeroSerie = "SN-TEST-CAL",
                    codigoInterno = "CAL-01",
                    observaciones = "Prueba calibración",
                ),
            )
        }
    }

    @After
    fun tearDown() {
        SesionRepository.cerrarSesion()
        db.close()
    }

    private fun configurarUsuario(rol: Rol, activo: Boolean) {
        SesionRepository.establecerUsuario(
            Usuario(
                id = "TEST-USER",
                nombreCompleto = "Usuario Test",
                rut = "11.111.111-1",
                correo = "test@induztek.ejemplo",
                rol = rol,
                activo = activo,
            ),
        )
    }

    @Test
    fun jefeTecnicoActivo_puedeRegistrarCalibracion() {
        runBlocking {
            configurarUsuario(Rol.JEFE_TECNICO, true)

            val calibracion = CalibracionEntity(
                instrumentoId = instrumentoId,
                fechaCalibracion = "2026-01-01",
                fechaVencimiento = "2026-12-31",
            )

            val id = repository.registrarCalibracion(calibracion)
            assertTrue(id > 0)

            val lista = db.calibracionDao().observarCalibracionesDeInstrumento(instrumentoId).first()
            assertEquals(1, lista.size)
        }
    }

    @Test
    fun sinSesion_noPuedeRegistrarCalibracion() {
        runBlocking {
            SesionRepository.cerrarSesion()

            val calibracion = CalibracionEntity(
                instrumentoId = instrumentoId,
                fechaCalibracion = "2026-01-01",
                fechaVencimiento = "2026-12-31",
            )

            var exceptionThrown = false
            try {
                repository.registrarCalibracion(calibracion)
            } catch (_: SecurityException) {
                exceptionThrown = true
            }
            assertTrue(exceptionThrown)

            val lista = db.calibracionDao().observarCalibracionesDeInstrumento(instrumentoId).first()
            assertEquals(0, lista.size)
        }
    }

    @Test
    fun tecnicoActivo_noPuedeRegistrarCalibracion() {
        runBlocking {
            configurarUsuario(Rol.TECNICO, true)

            val calibracion = CalibracionEntity(
                instrumentoId = instrumentoId,
                fechaCalibracion = "2026-01-01",
                fechaVencimiento = "2026-12-31",
            )

            var exceptionThrown = false
            try {
                repository.registrarCalibracion(calibracion)
            } catch (_: SecurityException) {
                exceptionThrown = true
            }
            assertTrue(exceptionThrown)

            val lista = db.calibracionDao().observarCalibracionesDeInstrumento(instrumentoId).first()
            assertEquals(0, lista.size)
        }
    }

    @Test
    fun ingenieroActivo_noPuedeRegistrarCalibracion() {
        runBlocking {
            configurarUsuario(Rol.INGENIERO, true)

            val calibracion = CalibracionEntity(
                instrumentoId = instrumentoId,
                fechaCalibracion = "2026-01-01",
                fechaVencimiento = "2026-12-31",
            )

            var exceptionThrown = false
            try {
                repository.registrarCalibracion(calibracion)
            } catch (_: SecurityException) {
                exceptionThrown = true
            }
            assertTrue(exceptionThrown)

            val lista = db.calibracionDao().observarCalibracionesDeInstrumento(instrumentoId).first()
            assertEquals(0, lista.size)
        }
    }

    @Test
    fun clienteActivo_noPuedeRegistrarCalibracion() {
        runBlocking {
            configurarUsuario(Rol.CLIENTE, true)

            val calibracion = CalibracionEntity(
                instrumentoId = instrumentoId,
                fechaCalibracion = "2026-01-01",
                fechaVencimiento = "2026-12-31",
            )

            var exceptionThrown = false
            try {
                repository.registrarCalibracion(calibracion)
            } catch (_: SecurityException) {
                exceptionThrown = true
            }
            assertTrue(exceptionThrown)

            val lista = db.calibracionDao().observarCalibracionesDeInstrumento(instrumentoId).first()
            assertEquals(0, lista.size)
        }
    }

    @Test
    fun jefeTecnicoInactivo_noPuedeRegistrarCalibracion() {
        runBlocking {
            configurarUsuario(Rol.JEFE_TECNICO, false)

            val calibracion = CalibracionEntity(
                instrumentoId = instrumentoId,
                fechaCalibracion = "2026-01-01",
                fechaVencimiento = "2026-12-31",
            )

            var exceptionThrown = false
            try {
                repository.registrarCalibracion(calibracion)
            } catch (_: SecurityException) {
                exceptionThrown = true
            }
            assertTrue(exceptionThrown)

            val lista = db.calibracionDao().observarCalibracionesDeInstrumento(instrumentoId).first()
            assertEquals(0, lista.size)
        }
    }

    @Test
    fun jefeTecnicoActivo_puedeActualizarCalibracion() {
        runBlocking {
            configurarUsuario(Rol.JEFE_TECNICO, true)

            val calibracion = CalibracionEntity(
                instrumentoId = instrumentoId,
                fechaCalibracion = "2026-01-01",
                fechaVencimiento = "2026-12-31",
            )
            val id = repository.registrarCalibracion(calibracion)
            val guardada = db.calibracionDao().observarCalibracionesDeInstrumento(instrumentoId).first().first { it.id == id }

            val actualizada = guardada.copy(fechaVencimiento = "2027-06-30")
            val filas = repository.actualizarCalibracion(actualizada)

            assertEquals(1, filas)
            val consultada = db.calibracionDao().observarCalibracionesDeInstrumento(instrumentoId).first().first { it.id == id }
            assertEquals("2027-06-30", consultada.fechaVencimiento)
        }
    }

    @Test
    fun sinSesion_noPuedeActualizarCalibracion() {
        runBlocking {
            configurarUsuario(Rol.JEFE_TECNICO, true)
            val calibracion = CalibracionEntity(
                instrumentoId = instrumentoId,
                fechaCalibracion = "2026-01-01",
                fechaVencimiento = "2026-12-31",
            )
            val id = repository.registrarCalibracion(calibracion)
            val guardada = db.calibracionDao().observarCalibracionesDeInstrumento(instrumentoId).first().first { it.id == id }

            SesionRepository.cerrarSesion()
            val actualizada = guardada.copy(fechaVencimiento = "2027-06-30")

            var exceptionThrown = false
            try {
                repository.actualizarCalibracion(actualizada)
            } catch (_: SecurityException) {
                exceptionThrown = true
            }
            assertTrue(exceptionThrown)

            val consultada = db.calibracionDao().observarCalibracionesDeInstrumento(instrumentoId).first().first { it.id == id }
            assertEquals("2026-12-31", consultada.fechaVencimiento)
        }
    }

    @Test
    fun tecnicoActivo_noPuedeActualizarCalibracion() {
        runBlocking {
            configurarUsuario(Rol.JEFE_TECNICO, true)
            val calibracion = CalibracionEntity(
                instrumentoId = instrumentoId,
                fechaCalibracion = "2026-01-01",
                fechaVencimiento = "2026-12-31",
            )
            val id = repository.registrarCalibracion(calibracion)
            val guardada = db.calibracionDao().observarCalibracionesDeInstrumento(instrumentoId).first().first { it.id == id }

            configurarUsuario(Rol.TECNICO, true)
            val actualizada = guardada.copy(fechaVencimiento = "2027-06-30")

            var exceptionThrown = false
            try {
                repository.actualizarCalibracion(actualizada)
            } catch (_: SecurityException) {
                exceptionThrown = true
            }
            assertTrue(exceptionThrown)

            val consultada = db.calibracionDao().observarCalibracionesDeInstrumento(instrumentoId).first().first { it.id == id }
            assertEquals("2026-12-31", consultada.fechaVencimiento)
        }
    }

    @Test
    fun jefeTecnicoInactivo_noPuedeActualizarCalibracion() {
        runBlocking {
            configurarUsuario(Rol.JEFE_TECNICO, true)
            val calibracion = CalibracionEntity(
                instrumentoId = instrumentoId,
                fechaCalibracion = "2026-01-01",
                fechaVencimiento = "2026-12-31",
            )
            val id = repository.registrarCalibracion(calibracion)
            val guardada = db.calibracionDao().observarCalibracionesDeInstrumento(instrumentoId).first().first { it.id == id }

            configurarUsuario(Rol.JEFE_TECNICO, false)
            val actualizada = guardada.copy(fechaVencimiento = "2027-06-30")

            var exceptionThrown = false
            try {
                repository.actualizarCalibracion(actualizada)
            } catch (_: SecurityException) {
                exceptionThrown = true
            }
            assertTrue(exceptionThrown)

            val consultada = db.calibracionDao().observarCalibracionesDeInstrumento(instrumentoId).first().first { it.id == id }
            assertEquals("2026-12-31", consultada.fechaVencimiento)
        }
    }
}
