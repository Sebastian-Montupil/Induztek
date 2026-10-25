package com.example.induztek.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.induztek.data.local.AppDatabase
import com.example.induztek.data.local.entity.CalibracionEntity
import com.example.induztek.data.local.entity.InstrumentoEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun instrumentoInexistente_retornaError() = runBlocking {
        val error = repository.validarInstrumentoParaPrueba(999L, "2026-06-15")
        assertNotNull(error)
    }

    @Test
    fun instrumentoSinCalibracion_retornaError() = runBlocking {
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

    @Test
    fun instrumentoConCalibracionVigente_retornaNull() = runBlocking {
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

    @Test
    fun instrumentoConCalibracionVencida_retornaError() = runBlocking {
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

    @Test
    fun fechaPruebaIgualAVencimiento_retornaNull() = runBlocking {
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

    @Test
    fun multiplesCalibraciones_usaLaUltimaHastaFechaPrueba_ignoraFuturas() = runBlocking {
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

        // Calibración antigua (vencida el 2025-01-01)
        db.calibracionDao().insertarCalibracion(
            CalibracionEntity(
                instrumentoId = instrumentoId,
                fechaCalibracion = "2024-01-01",
                fechaVencimiento = "2025-01-01",
            ),
        )

        // Calibración vigente a partir del 2025-06-01
        db.calibracionDao().insertarCalibracion(
            CalibracionEntity(
                instrumentoId = instrumentoId,
                fechaCalibracion = "2025-06-01",
                fechaVencimiento = "2026-12-31",
            ),
        )

        // Calibración futura (posterior a la fecha de prueba 2026-06-15, ej. 2027)
        db.calibracionDao().insertarCalibracion(
            CalibracionEntity(
                instrumentoId = instrumentoId,
                fechaCalibracion = "2027-01-01",
                fechaVencimiento = "2028-01-01",
            ),
        )

        // Probando con fecha de prueba 2026-06-15: debe usar la calibración de 2025-06-01 (vigente), ignorando la de 2027.
        val error = repository.validarInstrumentoParaPrueba(instrumentoId, "2026-06-15")
        assertNull(error)

        // Probando con fecha de prueba 2025-03-15: en esa fecha la calibración de 2024 ya estaba vencida y la de junio de 2025 aún no existía.
        val errorAntiguo = repository.validarInstrumentoParaPrueba(instrumentoId, "2025-03-15")
        assertNotNull(errorAntiguo)
    }
}
