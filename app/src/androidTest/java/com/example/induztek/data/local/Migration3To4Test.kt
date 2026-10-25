package com.example.induztek.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.induztek.data.local.entity.EquipoEntity
import com.example.induztek.data.local.migrations.MIGRATION_3_4
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
class Migration3To4Test {

    private val dbName = "test_migration_v3_v4.db"
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
    fun testMigration3To4PreservesOldDataAndAllowsEquipmentOperations() {
        runBlocking {
            val sqliteDb = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null)
            try {
                sqliteDb.version = 3
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
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_instrumentos_medicion_numeroSerie` ON `instrumentos_medicion` (`numeroSerie`)",
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
                    "CREATE INDEX IF NOT EXISTS `index_calibraciones_instrumentoId` ON `calibraciones` (`instrumentoId`)",
                )
                sqliteDb.execSQL(
                    """
                    INSERT INTO instrumentos_medicion (id, nombre, tipoInstrumento, marca, modelo, numeroSerie, codigoInterno, fechaVencimientoCalibracion, observaciones)
                    VALUES (1, 'Multímetro V3', 'Instrumento', 'Fluke', '87V', 'SN-V3-001', 'MUL-01', '2026-12-31', 'Ninguna')
                    """.trimIndent(),
                )
                sqliteDb.execSQL(
                    """
                    INSERT INTO calibraciones (id, instrumentoId, fechaCalibracion, fechaVencimiento)
                    VALUES (1, 1, '2025-01-01', '2026-01-01')
                    """.trimIndent(),
                )
            } finally {
                sqliteDb.close()
            }

            db = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                dbName,
            )
                .addMigrations(MIGRATION_3_4)
                .build()

            val currentVersion = db.openHelper.readableDatabase.version
            assertEquals(4, currentVersion)

            val instrumentos = db.instrumentoDao().obtenerInstrumentos().first()
            assertEquals(1, instrumentos.size)
            assertEquals("Multímetro V3", instrumentos[0].nombre)
            assertEquals("2026-12-31", instrumentos[0].fechaVencimientoCalibracion)

            val calibraciones = db.calibracionDao().observarCalibracionesDeInstrumento(1L).first()
            assertEquals(1, calibraciones.size)
            assertEquals("2025-01-01", calibraciones[0].fechaCalibracion)

            val equipoId = db.equipoDao().insertarEquipo(
                EquipoEntity(
                    clienteId = "CLI-01",
                    instalacionId = "INS-01",
                    nombre = "Transformador Principal",
                    tipo = "Transformador",
                    marca = "ABB",
                    modelo = "TR-500",
                    numeroSerie = "SN-EQ-001",
                    codigoInterno = "TR-01",
                    observaciones = "Nuevo equipo",
                ),
            )
            assertTrue(equipoId > 0)

            val equipoRecuperado = db.equipoDao().obtenerEquipoPorId(equipoId)
            assertNotNull(equipoRecuperado)
            assertEquals("Transformador Principal", equipoRecuperado?.nombre)
            assertEquals("CLI-01", equipoRecuperado?.clienteId)
            assertEquals("INS-01", equipoRecuperado?.instalacionId)
        }
    }

    @Test
    fun testMigration3To4EnforcesUniqueIndexOnEquipos() {
        runBlocking {
            val sqliteDb = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null)
            try {
                sqliteDb.version = 3
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
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_instrumentos_medicion_numeroSerie` ON `instrumentos_medicion` (`numeroSerie`)",
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
                    "CREATE INDEX IF NOT EXISTS `index_calibraciones_instrumentoId` ON `calibraciones` (`instrumentoId`)",
                )
            } finally {
                sqliteDb.close()
            }

            db = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                dbName,
            )
                .addMigrations(MIGRATION_3_4)
                .build()

            val equipo1 = EquipoEntity(
                clienteId = "CLI-01",
                instalacionId = "INS-01",
                nombre = "Equipo 1",
                tipo = "Celda",
                marca = "Schneider",
                modelo = "SM6",
                numeroSerie = "SN-DUPLICATE",
                codigoInterno = "CEL-01",
                observaciones = "Obs 1",
            )
            val id1 = db.equipoDao().insertarEquipo(equipo1)
            assertTrue(id1 > 0)

            val equipo2 = EquipoEntity(
                clienteId = "CLI-02",
                instalacionId = "INS-02",
                nombre = "Equipo 2",
                tipo = "Celda",
                marca = "Schneider",
                modelo = "SM6",
                numeroSerie = "SN-DUPLICATE",
                codigoInterno = "CEL-02",
                observaciones = "Obs 2",
            )

            assertThrows(SQLiteConstraintException::class.java) {
                runBlocking {
                    db.equipoDao().insertarEquipo(equipo2)
                }
            }
        }
    }
}
