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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InstrumentoRepositoryTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: InstrumentoRepository

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = InstrumentoRepository(db.instrumentoDao(), db.calibracionDao())
        SesionRepository.establecerUsuario(
            Usuario(
                id = "JEF-01",
                nombreCompleto = "Ricardo Espinoza Lagos",
                rut = "13.871.905-7",
                correo = "respinoza@induztek.ejemplo",
                rol = Rol.JEFE_TECNICO,
                activo = true,
            ),
        )
    }

    @After
    fun tearDown() {
        SesionRepository.cerrarSesion()
        db.close()
    }

    @Test
    fun instrumentoInexistente_retornaError() {
        runBlocking {
            val error = repository.validarInstrumentoParaPrueba(999L, "2026-06-15")
            assertNotNull(error)
        }
    }

    @Test
    fun instrumentoSinCalibracion_retornaError() {
        runBlocking {
            val instrumentoId = repository.registrarInstrumento(
                InstrumentoEntity(
                    nombre = "Transformador de Corriente",
                    tipoInstrumento = "Transformador",
                    marca = "ABB",
                    modelo = "TC-100",
                    numeroSerie = "SN-TRANS-01",
                    codigoInterno = "TC-01",
                    observaciones = "Sin calibración previa",
                ),
            )

            val error = repository.validarInstrumentoParaPrueba(instrumentoId, "2026-06-15")
            assertNotNull(error)
        }
    }

    @Test
    fun instrumentoConCalibracionVigente_retornaNull() {
        runBlocking {
            val instrumentoId = repository.registrarInstrumento(
                InstrumentoEntity(
                    nombre = "Multímetro Digital",
                    tipoInstrumento = "Instrumento",
                    marca = "Fluke",
                    modelo = "87V",
                    numeroSerie = "SN-MULT-01",
                    codigoInterno = "MUL-01",
                    observaciones = "Operativo",
                ),
            )

            db.calibracionDao().insertarCalibracion(
                CalibracionEntity(
                    instrumentoId = instrumentoId,
                    fechaCalibracion = "2026-01-01",
                    fechaVencimiento = "2026-12-31",
                ),
            )

            val error = repository.validarInstrumentoParaPrueba(instrumentoId, "2026-06-15")
            assertNull(error)
        }
    }

    @Test
    fun instrumentoConCalibracionVencida_retornaError() {
        runBlocking {
            val instrumentoId = repository.registrarInstrumento(
                InstrumentoEntity(
                    nombre = "Megohmetro",
                    tipoInstrumento = "Instrumento",
                    marca = "Megger",
                    modelo = "MIT515",
                    numeroSerie = "SN-MEG-01",
                    codigoInterno = "MEG-01",
                    observaciones = "Vencido",
                ),
            )

            db.calibracionDao().insertarCalibracion(
                CalibracionEntity(
                    instrumentoId = instrumentoId,
                    fechaCalibracion = "2025-01-01",
                    fechaVencimiento = "2025-12-31",
                ),
            )

            val error = repository.validarInstrumentoParaPrueba(instrumentoId, "2026-06-15")
            assertNotNull(error)
        }
    }

    @Test
    fun fechaPruebaIgualAVencimiento_retornaNull() {
        runBlocking {
            val instrumentoId = repository.registrarInstrumento(
                InstrumentoEntity(
                    nombre = "Telurómetro",
                    tipoInstrumento = "Instrumento",
                    marca = "Chauvin Arnoux",
                    modelo = "6472",
                    numeroSerie = "SN-TEL-01",
                    codigoInterno = "TEL-01",
                    observaciones = "Último día",
                ),
            )

            db.calibracionDao().insertarCalibracion(
                CalibracionEntity(
                    instrumentoId = instrumentoId,
                    fechaCalibracion = "2026-01-01",
                    fechaVencimiento = "2026-06-15",
                ),
            )

            val error = repository.validarInstrumentoParaPrueba(instrumentoId, "2026-06-15")
            assertNull(error)
        }
    }

    @Test
    fun multiplesCalibraciones_usaLaUltimaHastaFechaPrueba_ignoraFuturas() {
        runBlocking {
            val instrumentoId = repository.registrarInstrumento(
                InstrumentoEntity(
                    nombre = "Pinza Amperimétrica",
                    tipoInstrumento = "Instrumento",
                    marca = "Fluke",
                    modelo = "376",
                    numeroSerie = "SN-PINZA-01",
                    codigoInterno = "PIN-01",
                    observaciones = "Varias calibraciones",
                ),
            )

            db.calibracionDao().insertarCalibracion(
                CalibracionEntity(
                    instrumentoId = instrumentoId,
                    fechaCalibracion = "2024-01-01",
                    fechaVencimiento = "2025-01-01",
                ),
            )

            db.calibracionDao().insertarCalibracion(
                CalibracionEntity(
                    instrumentoId = instrumentoId,
                    fechaCalibracion = "2025-06-01",
                    fechaVencimiento = "2026-12-31",
                ),
            )

            db.calibracionDao().insertarCalibracion(
                CalibracionEntity(
                    instrumentoId = instrumentoId,
                    fechaCalibracion = "2027-01-01",
                    fechaVencimiento = "2028-01-01",
                ),
            )

            val error = repository.validarInstrumentoParaPrueba(instrumentoId, "2026-06-15")
            assertNull(error)

            val errorAntiguo = repository.validarInstrumentoParaPrueba(instrumentoId, "2025-03-15")
            assertNotNull(errorAntiguo)
        }
    }

    @Test
    fun registrarInstrumento_sinSesion_lanzaSecurityException() {
        runBlocking {
            SesionRepository.cerrarSesion()

            var exceptionThrown = false
            try {
                repository.registrarInstrumento(
                    InstrumentoEntity(
                        nombre = "Equipo Sin Sesión",
                        tipoInstrumento = "Instrumento",
                        marca = "Marca",
                        modelo = "Modelo",
                        numeroSerie = "SN-NO-SESSION",
                        codigoInterno = "NS-01",
                        observaciones = "Prueba sin sesión",
                    ),
                )
            } catch (_: SecurityException) {
                exceptionThrown = true
            }
            assertTrue(exceptionThrown)

            val lista = repository.observarInstrumentos().first()
            assertEquals(0, lista.size)
        }
    }

    @Test
    fun registrarInstrumento_tecnicoActivo_lanzaSecurityException() {
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

            var exceptionThrown = false
            try {
                repository.registrarInstrumento(
                    InstrumentoEntity(
                        nombre = "Equipo Con Técnico",
                        tipoInstrumento = "Instrumento",
                        marca = "Marca",
                        modelo = "Modelo",
                        numeroSerie = "SN-TEC-AUTH",
                        codigoInterno = "TEC-01",
                        observaciones = "Prueba técnico",
                    ),
                )
            } catch (_: SecurityException) {
                exceptionThrown = true
            }
            assertTrue(exceptionThrown)

            val lista = repository.observarInstrumentos().first()
            assertEquals(0, lista.size)
        }
    }

    @Test
    fun jefeTecnicoActivo_puedeActualizarInstrumento() {
        runBlocking {
            val instrumentoId = repository.registrarInstrumento(
                InstrumentoEntity(
                    nombre = "Instrumento Original",
                    tipoInstrumento = "Instrumento",
                    marca = "Marca",
                    modelo = "Modelo",
                    numeroSerie = "SN-ACT-01",
                    codigoInterno = "ACT-01",
                    observaciones = "Original",
                ),
            )

            val guardado = repository.obtenerInstrumentoPorId(instrumentoId)!!
            val actualizado = guardado.copy(nombre = "Instrumento Modificado")
            val filas = repository.actualizarInstrumento(actualizado)

            assertEquals(1, filas)
            val consultado = repository.obtenerInstrumentoPorId(instrumentoId)!!
            assertEquals("Instrumento Modificado", consultado.nombre)
        }
    }

    @Test
    fun sinSesion_noPuedeActualizarInstrumento() {
        runBlocking {
            val instrumentoId = repository.registrarInstrumento(
                InstrumentoEntity(
                    nombre = "Instrumento Original",
                    tipoInstrumento = "Instrumento",
                    marca = "Marca",
                    modelo = "Modelo",
                    numeroSerie = "SN-ACT-02",
                    codigoInterno = "ACT-02",
                    observaciones = "Original",
                ),
            )

            SesionRepository.cerrarSesion()
            val guardado = repository.obtenerInstrumentoPorId(instrumentoId)!!
            val actualizado = guardado.copy(nombre = "Intento Modificación")

            var exceptionThrown = false
            try {
                repository.actualizarInstrumento(actualizado)
            } catch (_: SecurityException) {
                exceptionThrown = true
            }
            assertTrue(exceptionThrown)

            val consultado = repository.obtenerInstrumentoPorId(instrumentoId)!!
            assertEquals("Instrumento Original", consultado.nombre)
        }
    }

    @Test
    fun tecnicoActivo_noPuedeActualizarInstrumento() {
        runBlocking {
            val instrumentoId = repository.registrarInstrumento(
                InstrumentoEntity(
                    nombre = "Instrumento Original",
                    tipoInstrumento = "Instrumento",
                    marca = "Marca",
                    modelo = "Modelo",
                    numeroSerie = "SN-ACT-03",
                    codigoInterno = "ACT-03",
                    observaciones = "Original",
                ),
            )

            SesionRepository.establecerUsuario(
                Usuario(
                    id = "TEC-02",
                    nombreCompleto = "Javiera Tapia Muñoz",
                    rut = "19.412.087-7",
                    correo = "jtapia@induztek.ejemplo",
                    rol = Rol.TECNICO,
                    activo = true,
                ),
            )

            val guardado = repository.obtenerInstrumentoPorId(instrumentoId)!!
            val actualizado = guardado.copy(nombre = "Intento Modificación")

            var exceptionThrown = false
            try {
                repository.actualizarInstrumento(actualizado)
            } catch (_: SecurityException) {
                exceptionThrown = true
            }
            assertTrue(exceptionThrown)

            val consultado = repository.obtenerInstrumentoPorId(instrumentoId)!!
            assertEquals("Instrumento Original", consultado.nombre)
        }
    }

    @Test
    fun jefeTecnicoInactivo_noPuedeActualizarInstrumento() {
        runBlocking {
            val instrumentoId = repository.registrarInstrumento(
                InstrumentoEntity(
                    nombre = "Instrumento Original",
                    tipoInstrumento = "Instrumento",
                    marca = "Marca",
                    modelo = "Modelo",
                    numeroSerie = "SN-ACT-04",
                    codigoInterno = "ACT-04",
                    observaciones = "Original",
                ),
            )

            SesionRepository.establecerUsuario(
                Usuario(
                    id = "JEF-01",
                    nombreCompleto = "Ricardo Espinoza Lagos",
                    rut = "13.871.905-7",
                    correo = "respinoza@induztek.ejemplo",
                    rol = Rol.JEFE_TECNICO,
                    activo = false,
                ),
            )

            val guardado = repository.obtenerInstrumentoPorId(instrumentoId)!!
            val actualizado = guardado.copy(nombre = "Intento Modificación")

            var exceptionThrown = false
            try {
                repository.actualizarInstrumento(actualizado)
            } catch (_: SecurityException) {
                exceptionThrown = true
            }
            assertTrue(exceptionThrown)

            val consultado = repository.obtenerInstrumentoPorId(instrumentoId)!!
            assertEquals("Instrumento Original", consultado.nombre)
        }
    }
}
