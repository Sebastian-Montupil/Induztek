package com.example.induztek.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.induztek.data.local.entity.CalibracionEntity
import com.example.induztek.data.local.migrations.MIGRATION_1_2
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration1To2Test {

    private val dbName = "test_migration_v1_v2.db"
    private lateinit var context: Context
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(dbName)
    }

    @After
    fun tearDown() {
        if (::db.isInitialized) {
            db.close()
        }
        context.deleteDatabase(dbName)
    }

    @Test
    fun testMigration1To2PreservesDataAndCreatesCalibracionesTable() = runBlocking {
        // 1. Preparar una base de datos versión 1 usando SQLite nativo
        val sqliteDb = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null)
        sqliteDb.version = 1
        sqliteDb.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `instrumentos_medicion` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `nombre` TEXT NOT NULL,
                `tipoInstrumento` TEXT NOT NULL,
                `marca` TEXT NOT NULL,
                `modelo` TEXT NOT NULL,
                `numeroSerie` TEXT NOT NULL,
                `codigoInterno` TEXT NOT NULL,
                `fechaVencimientoCalibracion` TEXT,
                `observaciones` TEXT NOT NULL
            )
            """.trimIndent(),
        )
        sqliteDb.execSQL(
            """
            INSERT INTO instrumentos_medicion (nombre, tipoInstrumento, marca, modelo, numeroSerie, codigoInterno, fechaVencimientoCalibracion, observaciones)
            VALUES ('Multímetro Digital', 'Instrumento', 'Fluke', '87V', 'SN98765', 'INS-001', '2025-12-31', 'Ninguna')
            """.trimIndent(),
        )
        sqliteDb.close()

        // 2. Abrir la base de datos con AppDatabase v2 aplicando MIGRATION_1_2
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            dbName,
        )
            .addMigrations(MIGRATION_1_2)
            .build()

        // 3. Comprobar que el instrumento antiguo sigue existiendo y sus datos son correctos
        val instrumentos = db.instrumentoDao().obtenerInstrumentos().first()
        assertEquals(1, instrumentos.size)
        val instrumento = instrumentos[0]
        assertEquals("Multímetro Digital", instrumento.nombre)
        assertEquals("SN98765", instrumento.numeroSerie)
        assertEquals("2025-12-31", instrumento.fechaVencimientoCalibracion)

        // 4. Comprobar que la tabla calibraciones existe y es posible registrar una calibración asociada
        val calibracion = CalibracionEntity(
            instrumentoId = instrumento.id,
            fechaCalibracion = "2024-01-15",
            fechaVencimiento = "2025-01-15",
        )
        val calibracionId = db.calibracionDao().insertarCalibracion(calibracion)
        assertTrue(calibracionId > 0)

        // 5. Consultar las calibraciones del instrumento
        val calibraciones = db.calibracionDao().observarCalibracionesDeInstrumento(instrumento.id).first()
        assertEquals(1, calibraciones.size)
        assertEquals("2024-01-15", calibraciones[0].fechaCalibracion)
        assertEquals("2025-01-15", calibraciones[0].fechaVencimiento)
    }
}
