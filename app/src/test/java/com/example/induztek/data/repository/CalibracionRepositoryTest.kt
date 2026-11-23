
package com.example.induztek.data.repository

import com.example.induztek.data.local.dao.CalibracionDao
import com.example.induztek.data.local.entity.CalibracionEntity
import com.example.induztek.model.Rol
import com.example.induztek.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CalibracionRepositoryTest {

    private lateinit var dao: CalibracionDaoPrueba
    private lateinit var repository: CalibracionRepository

    @Before
    fun preparar() {
        dao = CalibracionDaoPrueba()
        repository = CalibracionRepository(dao)
        iniciarSesion(Rol.JEFE_TECNICO)
    }

    @After
    fun finalizar() {
        SesionRepository.cerrarSesion()
    }

    private fun iniciarSesion(
        rol: Rol,
        activo: Boolean = true
    ) {
        SesionRepository.establecerUsuario(
            Usuario(
                id = "1",
                nombreCompleto = "Usuario de prueba",
                rut = "12345678-9",
                correo = "prueba@induztek.local",
                rol = rol,
                activo = activo
            )
        )
    }

    private fun calibracion(
        id: Long = 0L
    ) = CalibracionEntity(
        id = id,
        instrumentoId = 1L,
        fechaCalibracion = "2026-11-23",
        fechaVencimiento = "2027-11-23"
    )

    @Test
    fun jefeTecnicoPuedeRegistrar() = runTest {
        val id = repository.registrarCalibracion(calibracion())

        assertEquals(1L, id)
        assertEquals(1, dao.registros.size)
        assertEquals(1L, dao.registros.first().instrumentoId)
    }

    @Test
    fun jefeTecnicoPuedeActualizar() = runTest {
        val id = repository.registrarCalibracion(calibracion())

        val resultado = repository.actualizarCalibracion(
            calibracion(id).copy(
                fechaVencimiento = "2028-11-23"
            )
        )

        assertEquals(1, resultado)
        assertEquals(
            "2028-11-23",
            dao.registros.first().fechaVencimiento
        )
    }

    @Test
    fun actualizarRegistroInexistenteDevuelveCero() = runTest {
        val resultado = repository.actualizarCalibracion(
            calibracion(99L)
        )

        assertEquals(0, resultado)
    }

    @Test
    fun sinSesionNoPuedeRegistrar() = runTest {
        SesionRepository.cerrarSesion()

        assertThrows(SecurityException::class.java) {
            kotlinx.coroutines.runBlocking {
                repository.registrarCalibracion(calibracion())
            }
        }

        assertTrue(dao.registros.isEmpty())
    }

    @Test
    fun tecnicoNoPuedeRegistrar() = runTest {
        iniciarSesion(Rol.TECNICO)

        assertThrows(SecurityException::class.java) {
            kotlinx.coroutines.runBlocking {
                repository.registrarCalibracion(calibracion())
            }
        }

        assertTrue(dao.registros.isEmpty())
    }

    @Test
    fun ingenieroNoPuedeRegistrar() = runTest {
        iniciarSesion(Rol.INGENIERO)

        assertThrows(SecurityException::class.java) {
            kotlinx.coroutines.runBlocking {
                repository.registrarCalibracion(calibracion())
            }
        }
    }

    @Test
    fun clienteNoPuedeRegistrar() = runTest {
        iniciarSesion(Rol.CLIENTE)

        assertThrows(SecurityException::class.java) {
            kotlinx.coroutines.runBlocking {
                repository.registrarCalibracion(calibracion())
            }
        }
    }

    @Test
    fun usuarioInactivoNoPuedeRegistrar() = runTest {
        iniciarSesion(Rol.JEFE_TECNICO, activo = false)

        assertThrows(SecurityException::class.java) {
            kotlinx.coroutines.runBlocking {
                repository.registrarCalibracion(calibracion())
            }
        }
    }

    @Test
    fun sinSesionNoPuedeActualizar() = runTest {
        SesionRepository.cerrarSesion()

        assertThrows(SecurityException::class.java) {
            kotlinx.coroutines.runBlocking {
                repository.actualizarCalibracion(calibracion(1L))
            }
        }
    }

    @Test
    fun tecnicoNoPuedeActualizar() = runTest {
        iniciarSesion(Rol.TECNICO)

        assertThrows(SecurityException::class.java) {
            kotlinx.coroutines.runBlocking {
                repository.actualizarCalibracion(calibracion(1L))
            }
        }
    }

    @Test
    fun errorAlRegistrarSePropaga() = runTest {
        dao.errorRegistro = IllegalStateException("Error de BD")

        assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking {
                repository.registrarCalibracion(calibracion())
            }
        }
    }

    @Test
    fun errorAlActualizarSePropaga() = runTest {
        dao.errorActualizacion = IllegalStateException("Error al actualizar")

        assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking {
                repository.actualizarCalibracion(calibracion(1L))
            }
        }
    }

    private class CalibracionDaoPrueba : CalibracionDao {

        val registros = mutableListOf<CalibracionEntity>()
        var errorRegistro: Exception? = null
        var errorActualizacion: Exception? = null

        override suspend fun insertarCalibracion(
            calibracion: CalibracionEntity
        ): Long {
            errorRegistro?.let { throw it }

            val id = registros.size.toLong() + 1L
            registros.add(calibracion.copy(id = id))
            return id
        }

        override suspend fun actualizarCalibracion(
            calibracion: CalibracionEntity
        ): Int {
            errorActualizacion?.let { throw it }

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
                registros.filter { it.instrumentoId == instrumentoId }
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
