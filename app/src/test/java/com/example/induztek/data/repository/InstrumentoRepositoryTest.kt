package com.example.induztek.data.repository

import com.example.induztek.data.local.dao.CalibracionDao
import com.example.induztek.data.local.dao.InstrumentoDao
import com.example.induztek.data.local.entity.CalibracionEntity
import com.example.induztek.data.local.entity.InstrumentoEntity
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

class InstrumentoRepositoryTest {

    private lateinit var instrumentoDao: InstrumentoDaoPrueba
    private lateinit var calibracionDao: CalibracionDaoPrueba
    private lateinit var repository: InstrumentoRepository

    @Before
    fun preparar() {
        SesionRepository.cerrarSesion()
        instrumentoDao = InstrumentoDaoPrueba()
        calibracionDao = CalibracionDaoPrueba()
        repository = InstrumentoRepository(
            instrumentoDao = instrumentoDao,
            calibracionDao = calibracionDao
        )
        iniciarSesion()
    }

    @After
    fun finalizar() {
        SesionRepository.cerrarSesion()
    }

    private fun iniciarSesion(
        rol: Rol = Rol.JEFE_TECNICO,
        activo: Boolean = true
    ) {
        SesionRepository.establecerUsuario(
            Usuario(
                id = "USR-01",
                nombreCompleto = "Usuario de prueba",
                rut = "12345678-9",
                correo = "prueba@induztek.local",
                rol = rol,
                activo = activo
            )
        )
    }

    private fun instrumento(
        id: Long = 0L
    ) = InstrumentoEntity(
        id = id,
        nombre = "Multimetro digital",
        tipoInstrumento = "Multimetro",
        marca = "Fluke",
        modelo = "87V",
        numeroSerie = "INS-001",
        codigoInterno = "INST-001",
        fechaVencimientoCalibracion = "2027-11-23",
        observaciones = "Instrumento de prueba"
    )

    private fun calibracion(
        instrumentoId: Long = 1L,
        fechaCalibracion: String = "2026-11-23",
        fechaVencimiento: String = "2027-11-23",
        id: Long = 0L
    ) = CalibracionEntity(
        id = id,
        instrumentoId = instrumentoId,
        fechaCalibracion = fechaCalibracion,
        fechaVencimiento = fechaVencimiento
    )

    private suspend fun verificarAccesoDenegado(
        accion: suspend () -> Any?
    ) {
        try {
            accion()
            fail("Se esperaba SecurityException")
        } catch (e: SecurityException) {
            assertNotNull(e.message)
        }
    }

    @Test
    fun jefeTecnicoPuedeRegistrar() = runTest {
        val id = repository.registrarInstrumento(instrumento())

        assertEquals(1L, id)
        assertEquals(1, instrumentoDao.registros.size)
        assertEquals("Fluke", instrumentoDao.registros.first().marca)
    }

    @Test
    fun registrarConservaLosDatos() = runTest {
        val id = repository.registrarInstrumento(instrumento())

        val resultado = repository.obtenerInstrumentoPorId(id)

        assertNotNull(resultado)
        assertEquals("INS-001", resultado?.numeroSerie)
        assertEquals("87V", resultado?.modelo)
    }

    @Test
    fun sinSesionNoPuedeRegistrar() = runTest {
        SesionRepository.cerrarSesion()

        verificarAccesoDenegado {
            repository.registrarInstrumento(instrumento())
        }

        assertTrue(instrumentoDao.registros.isEmpty())
    }

    @Test
    fun usuarioInactivoNoPuedeRegistrar() = runTest {
        iniciarSesion(activo = false)

        verificarAccesoDenegado {
            repository.registrarInstrumento(instrumento())
        }
    }

    @Test
    fun tecnicoNoPuedeRegistrar() = runTest {
        iniciarSesion(Rol.TECNICO)

        verificarAccesoDenegado {
            repository.registrarInstrumento(instrumento())
        }
    }

    @Test
    fun ingenieroNoPuedeRegistrar() = runTest {
        iniciarSesion(Rol.INGENIERO)

        verificarAccesoDenegado {
            repository.registrarInstrumento(instrumento())
        }
    }

    @Test
    fun clienteNoPuedeRegistrar() = runTest {
        iniciarSesion(Rol.CLIENTE)

        verificarAccesoDenegado {
            repository.registrarInstrumento(instrumento())
        }
    }

    @Test
    fun jefeTecnicoPuedeActualizar() = runTest {
        val id = repository.registrarInstrumento(instrumento())

        val actualizado = instrumento(id).copy(
            marca = "Hioki"
        )

        val resultado = repository.actualizarInstrumento(actualizado)

        assertEquals(1, resultado)
        assertEquals(
            "Hioki",
            repository.obtenerInstrumentoPorId(id)?.marca
        )
    }

    @Test
    fun actualizarInstrumentoInexistenteDevuelveCero() = runTest {
        val resultado = repository.actualizarInstrumento(
            instrumento(id = 999L)
        )

        assertEquals(0, resultado)
    }

    @Test
    fun sinSesionNoPuedeActualizar() = runTest {
        SesionRepository.cerrarSesion()

        verificarAccesoDenegado {
            repository.actualizarInstrumento(instrumento(1L))
        }
    }

    @Test
    fun tecnicoNoPuedeActualizar() = runTest {
        iniciarSesion(Rol.TECNICO)

        verificarAccesoDenegado {
            repository.actualizarInstrumento(instrumento(1L))
        }
    }

    @Test
    fun inactivoNoPuedeActualizar() = runTest {
        iniciarSesion(activo = false)

        verificarAccesoDenegado {
            repository.actualizarInstrumento(instrumento(1L))
        }
    }

    @Test
    fun observarInstrumentosRegistrados() = runTest {
        repository.registrarInstrumento(instrumento())
        repository.registrarInstrumento(
            instrumento().copy(
                nombre = "Osciloscopio",
                numeroSerie = "INS-002"
            )
        )

        val resultado = repository.observarInstrumentos().first()

        assertEquals(2, resultado.size)
    }

    @Test
    fun observarInstrumentosVacios() = runTest {
        val resultado = repository.observarInstrumentos().first()

        assertTrue(resultado.isEmpty())
    }

    @Test
    fun obtenerInstrumentoExistente() = runTest {
        val id = repository.registrarInstrumento(instrumento())

        val resultado = repository.obtenerInstrumentoPorId(id)

        assertNotNull(resultado)
        assertEquals(id, resultado?.id)
    }

    @Test
    fun obtenerInstrumentoInexistente() = runTest {
        val resultado = repository.obtenerInstrumentoPorId(999L)

        assertNull(resultado)
    }

    @Test
    fun validarInstrumentoInexistente() = runTest {
        val error = repository.validarInstrumentoParaPrueba(
            999L,
            "2026-11-24"
        )

        assertNotNull(error)
        assertTrue(error!!.contains("no existe"))
    }

    @Test
    fun validarInstrumentoSinCalibracion() = runTest {
        val id = repository.registrarInstrumento(instrumento())

        val error = repository.validarInstrumentoParaPrueba(
            id,
            "2026-11-24"
        )

        assertNotNull(error)
        assertTrue(error!!.contains("no cuenta con registros"))
    }

    @Test
    fun validarInstrumentoConCalibracionVigente() = runTest {
        val id = repository.registrarInstrumento(instrumento())

        calibracionDao.registros.add(calibracion(instrumentoId = id))

        val resultado = repository.validarInstrumentoParaPrueba(
            id,
            "2026-11-24"
        )

        assertNull(resultado)
    }

    @Test
    fun validarInstrumentoConCalibracionVencida() = runTest {
        val id = repository.registrarInstrumento(instrumento())

        calibracionDao.registros.add(
            calibracion(
                instrumentoId = id,
                fechaVencimiento = "2026-11-23"
            )
        )

        val error = repository.validarInstrumentoParaPrueba(
            id,
            "2026-11-24"
        )

        assertNotNull(error)
        assertTrue(error!!.contains("vencida"))
    }

    @Test
    fun validarUtilizaUltimaCalibracion() = runTest {
        val id = repository.registrarInstrumento(instrumento())

        calibracionDao.registros.add(
            calibracion(
                instrumentoId = id,
                fechaCalibracion = "2025-01-01",
                fechaVencimiento = "2025-12-31",
                id = 1L
            )
        )

        calibracionDao.registros.add(
            calibracion(
                instrumentoId = id,
                fechaCalibracion = "2026-11-23",
                fechaVencimiento = "2027-11-23",
                id = 2L
            )
        )

        val resultado = repository.validarInstrumentoParaPrueba(
            id,
            "2026-11-24"
        )

        assertNull(resultado)
    }

    @Test
    fun calibracionFuturaNoEsValidaParaPruebaAnterior() = runTest {
        val id = repository.registrarInstrumento(instrumento())

        calibracionDao.registros.add(
            calibracion(
                instrumentoId = id,
                fechaCalibracion = "2027-01-01",
                fechaVencimiento = "2028-01-01"
            )
        )

        val resultado = repository.validarInstrumentoParaPrueba(
            id,
            "2026-11-24"
        )

        assertNotNull(resultado)
    }

    @Test
    fun errorAlRegistrarSePropaga() = runTest {
        instrumentoDao.errorRegistro =
            IllegalStateException("Error de base de datos")

        try {
            repository.registrarInstrumento(instrumento())
            fail("Se esperaba error")
        } catch (e: IllegalStateException) {
            assertEquals("Error de base de datos", e.message)
        }
    }

    @Test
    fun errorAlActualizarSePropaga() = runTest {
        instrumentoDao.errorActualizacion =
            IllegalStateException("Error al actualizar")

        try {
            repository.actualizarInstrumento(instrumento(1L))
            fail("Se esperaba error")
        } catch (e: IllegalStateException) {
            assertEquals("Error al actualizar", e.message)
        }
    }

    private class InstrumentoDaoPrueba : InstrumentoDao {

        val registros = mutableListOf<InstrumentoEntity>()

        var errorRegistro: Exception? = null
        var errorActualizacion: Exception? = null

        override suspend fun insertarInstrumento(
            instrumento: InstrumentoEntity
        ): Long {
            errorRegistro?.let { throw it }

            val id = (registros.maxOfOrNull { it.id } ?: 0L) + 1L
            registros.add(instrumento.copy(id = id))
            return id
        }

        override suspend fun actualizarInstrumento(
            instrumento: InstrumentoEntity
        ): Int {
            errorActualizacion?.let { throw it }

            val indice = registros.indexOfFirst {
                it.id == instrumento.id
            }

            if (indice == -1) return 0

            registros[indice] = instrumento
            return 1
        }

        override fun obtenerInstrumentos(): Flow<List<InstrumentoEntity>> {
            return flowOf(registros.sortedBy { it.nombre })
        }

        override suspend fun obtenerInstrumentoPorId(
            id: Long
        ): InstrumentoEntity? {
            return registros.firstOrNull { it.id == id }
        }
    }

    private class CalibracionDaoPrueba : CalibracionDao {

        val registros = mutableListOf<CalibracionEntity>()

        override suspend fun insertarCalibracion(
            calibracion: CalibracionEntity
        ): Long {
            val id = (registros.maxOfOrNull { it.id } ?: 0L) + 1L
            registros.add(calibracion.copy(id = id))
            return id
        }

        override suspend fun actualizarCalibracion(
            calibracion: CalibracionEntity
        ): Int {
            val indice = registros.indexOfFirst {
                it.id == calibracion.id
            }

            if (indice == -1) return 0

            registros[indice] = calibracion
            return 1
        }

        override fun observarCalibracionesDeInstrumento(
            instrumentoId: Long
        ): Flow<List<CalibracionEntity>> {
            return flowOf(
                registros.filter {
                    it.instrumentoId == instrumentoId
                }
            )
        }

        override suspend fun obtenerUltimaCalibracionHastaFecha(
            instrumentoId: Long,
            fechaPrueba: String
        ): CalibracionEntity? {
            return registros
                .filter {
                    it.instrumentoId == instrumentoId &&
                            it.fechaCalibracion <= fechaPrueba
                }
                .maxWithOrNull(
                    compareBy<CalibracionEntity> {
                        it.fechaCalibracion
                    }.thenBy { it.id }
                )
        }
    }
}
