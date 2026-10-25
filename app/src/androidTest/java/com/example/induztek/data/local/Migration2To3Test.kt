package com.example.induztek.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.induztek.data.local.entity.InstrumentoEntity
import com.example.induztek.data.local.migrations.MIGRATION_2_3
import com.example.induztek.data.local.migrations.MIGRATION_3_4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration2To3Test {

    private val dbNameA = "test_migration_v2_v4_success.db"
    private val dbNameB = "test_migration_v2_v4_fail.db"
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(dbNameA)
        context.deleteDatabase(dbNameB)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(dbNameA)
        context.deleteDatabase(dbNameB)
    }

    @Test
    fun testMigration2To3SuccessPreservesDataAndEnforcesUniqueIndex() {
        runBlocking {
            val sqliteDb = context.openOrCreateDatabase(dbNameA, Context.MODE_PRIVATE, null)
            try {
                sqliteDb.version = 2
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
                    CREATE TABLE IF NOT EXISTS `calibraciones` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `instrumentoId` INTEGER NOT NULL,
                        `fechaCalibracion` TEXT NOT NULL,
                        `fechaVencimiento` TEXT NOT NULL,
                        FOREIGN KEY(`instrumentoId`) REFERENCES `instrumentos_medicion`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent(),
                )
                sqliteDb.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_calibraciones_instrumentoId ON calibraciones(instrumentoId)",
                )
                sqliteDb.execSQL(
                    """
                    INSERT INTO instrumentos_medicion (id, nombre, tipoInstrumento, marca, modelo, numeroSerie, codigoInterno, observaciones)
                    VALUES (1, 'Instrumento A', 'Tipo A', 'Marca A', 'Modelo A', 'SERIE-001', 'COD-01', 'Obs A')
                    """.trimIndent(),
                )
            } finally {
                sqliteDb.close()
            }

            val db = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                dbNameA,
            )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                .build()

            try {
                val list = db.instrumentoDao().obtenerInstrumentos().first()
                assertEquals(1, list.size)
                assertEquals("SERIE-001", list[0].numeroSerie)

                val duplicate = InstrumentoEntity(
                    nombre = "Instrumento B",
                    tipoInstrumento = "Tipo B",
                    marca = "Marca B",
                    modelo = "Modelo B",
                    numeroSerie = "SERIE-001",
                    codigoInterno = "COD-02",
                    observaciones = "Obs B",
                )
                assertThrows(SQLiteConstraintException::class.java) {
                    runBlocking {
                        db.instrumentoDao().insertarInstrumento(duplicate)
                    }
                }
            } finally {
                db.close()
            }
        }
    }

    @Test
    fun testMigration2To3FailureWithDuplicatesPreservesDataAndVersion() {
        runBlocking {
            val sqliteDb = context.openOrCreateDatabase(dbNameB, Context.MODE_PRIVATE, null)
            try {
                sqliteDb.version = 2
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
                    CREATE TABLE IF NOT EXISTS `calibraciones` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `instrumentoId` INTEGER NOT NULL,
                        `fechaCalibracion` TEXT NOT NULL,
                        `fechaVencimiento` TEXT NOT NULL,
                        FOREIGN KEY(`instrumentoId`) REFERENCES `instrumentos_medicion`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent(),
                )
                sqliteDb.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_calibraciones_instrumentoId ON calibraciones(instrumentoId)",
                )
                sqliteDb.execSQL(
                    """
                    INSERT INTO instrumentos_medicion (id, nombre, tipoInstrumento, marca, modelo, numeroSerie, codigoInterno, observaciones)
                    VALUES (1, 'Instrumento A', 'Tipo A', 'Marca A', 'Modelo A', 'DUPLICATE-SERIES', 'COD-01', 'Obs A')
                    """.trimIndent(),
                )
                sqliteDb.execSQL(
                    """
                    INSERT INTO instrumentos_medicion (id, nombre, tipoInstrumento, marca, modelo, numeroSerie, codigoInterno, observaciones)
                    VALUES (2, 'Instrumento B', 'Tipo B', 'Marca B', 'Modelo B', 'DUPLICATE-SERIES', 'COD-02', 'Obs B')
                    """.trimIndent(),
                )
            } finally {
                sqliteDb.close()
            }

            val exception = assertThrows(IllegalStateException::class.java) {
                val failedDb = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    dbNameB,
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                try {
                    failedDb.openHelper.writableDatabase
                } finally {
                    failedDb.close()
                }
            }
            assertTrue(exception.message?.contains("números de serie duplicados") == true)

            val checkDb = context.openOrCreateDatabase(dbNameB, Context.MODE_PRIVATE, null)
            try {
                assertEquals(2, checkDb.version)
                val cursor = checkDb.rawQuery("SELECT COUNT(*) FROM instrumentos_medicion", null)
                cursor.use {
                    assertTrue(it.moveToFirst())
                    assertEquals(2, it.getInt(0))
                }
            } finally {
                checkDb.close()
            }
        }
    }
}
