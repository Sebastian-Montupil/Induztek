package com.example.induztek.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.induztek.data.local.entity.VisitaEntity
import com.example.induztek.data.local.entity.VisitaTecnicoEntity
import com.example.induztek.data.local.migrations.MIGRATION_4_5
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
class Migration4To5Test {

    private val dbName = "test_migration_v4_v5.db"
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
    fun testMigration4To5PreservesOldDataAndAllowsVisitsOperations() {
        runBlocking {
            val sqliteDb = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null)
            try {
                sqliteDb.version = 4
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
                    """.trimIndent()
                )
                sqliteDb.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_instrumentos_medicion_numeroSerie` ON `instrumentos_medicion` (`numeroSerie`)"
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
                    """.trimIndent()
                )
                sqliteDb.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_calibraciones_instrumentoId` ON `calibraciones` (`instrumentoId`)"
                )
                sqliteDb.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `equipos` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `clienteId` TEXT NOT NULL,
                        `instalacionId` TEXT NOT NULL,
                        `nombre` TEXT NOT NULL,
                        `tipo` TEXT NOT NULL,
                        `marca` TEXT NOT NULL,
                        `modelo` TEXT NOT NULL,
                        `numeroSerie` TEXT NOT NULL,
                        `codigoInterno` TEXT NOT NULL,
                        `observaciones` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                sqliteDb.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_equipos_numeroSerie` ON `equipos` (`numeroSerie`)"
                )
                sqliteDb.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_equipos_clienteId` ON `equipos` (`clienteId`)"
                )
                sqliteDb.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_equipos_instalacionId` ON `equipos` (`instalacionId`)"
                )
                sqliteDb.execSQL(
                    """
                    INSERT INTO equipos (id, clienteId, instalacionId, nombre, tipo, marca, modelo, numeroSerie, codigoInterno, observaciones)
                    VALUES (1, 'CLI-01', 'INS-01', 'Equipo V4', 'Celda', 'ABB', 'SM6', 'SN-EQ-V4', 'CEL-01', 'Obs')
                    """.trimIndent()
                )
            } finally {
                sqliteDb.close()
            }

            db = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                dbName
            )
                .addMigrations(MIGRATION_4_5)
                .build()

            val currentVersion = db.openHelper.readableDatabase.version
            assertEquals(5, currentVersion)

            val equipos = db.equipoDao().obtenerEquipoPorId(1L)
            assertNotNull(equipos)
            assertEquals("Equipo V4", equipos?.nombre)

            val visitaId = db.visitaDao().insertarVisitaConTecnicos(
                VisitaEntity(
                    clienteId = "CLI-01",
                    instalacionId = "INS-01",
                    fechaMillis = 1711929600000L,
                    numeroOt = "OT-2026-9999",
                    tipoVisita = "SOLO_PRUEBAS",
                    ingenieroCargoId = "ING-01",
                    observaciones = "Prueba migración"
                ),
                listOf("TEC-01", "TEC-02")
            )
            assertTrue(visitaId > 0)

            val visitaRecuperada = db.visitaDao().obtenerVisitaPorId(visitaId)
            assertNotNull(visitaRecuperada)
            assertEquals("OT-2026-9999", visitaRecuperada?.numeroOt)

            val tecnicos = db.visitaDao().obtenerTecnicosDeVisita(visitaId)
            assertEquals(2, tecnicos.size)
            assertTrue(tecnicos.contains("TEC-01"))
            assertTrue(tecnicos.contains("TEC-02"))
        }
    }

    @Test
    fun testVisitaDaoUniqueOtConstraint() {
        runBlocking {
            db = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                dbName
            )
                .addMigrations(MIGRATION_4_5)
                .build()

            val visita1 = VisitaEntity(
                clienteId = "CLI-01",
                instalacionId = "INS-01",
                fechaMillis = 1711929600000L,
                numeroOt = "OT-UNIQUE-01",
                tipoVisita = "SOLO_PRUEBAS",
                ingenieroCargoId = "ING-01",
                observaciones = "Obs 1"
            )
            val id1 = db.visitaDao().insertarVisita(visita1)
            assertTrue(id1 > 0)

            val visita2 = VisitaEntity(
                clienteId = "CLI-02",
                instalacionId = "INS-02",
                fechaMillis = 1711929600000L,
                numeroOt = "OT-UNIQUE-01",
                tipoVisita = "PRUEBAS_Y_MANT",
                ingenieroCargoId = "ING-02",
                observaciones = "Obs 2"
            )

            assertThrows(SQLiteConstraintException::class.java) {
                runBlocking {
                    db.visitaDao().insertarVisita(visita2)
                }
            }
        }
    }

    @Test
    fun testVisitaCascadeDelete() {
        runBlocking {
            db = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                dbName
            )
                .addMigrations(MIGRATION_4_5)
                .build()

            val visitaId = db.visitaDao().insertarVisitaConTecnicos(
                VisitaEntity(
                    clienteId = "CLI-01",
                    instalacionId = "INS-01",
                    fechaMillis = 1711929600000L,
                    numeroOt = "OT-CASCADE-01",
                    tipoVisita = "SOLO_PRUEBAS",
                    ingenieroCargoId = "ING-01",
                    observaciones = "Cascade test"
                ),
                listOf("TEC-01")
            )

            assertEquals(1, db.visitaDao().obtenerTecnicosDeVisita(visitaId).size)

            val visita = db.visitaDao().obtenerVisitaPorId(visitaId)
            assertNotNull(visita)
            val sqliteDb = db.openHelper.writableDatabase
            sqliteDb.execSQL("DELETE FROM visitas WHERE id = $visitaId")

            assertEquals(0, db.visitaDao().obtenerTecnicosDeVisita(visitaId).size)
        }
    }

    @Test
    fun testVisitaTecnicoCompositePrimaryKeyConstraint() {
        runBlocking {
            db = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                dbName
            )
                .addMigrations(MIGRATION_4_5)
                .build()

            val visitaId = db.visitaDao().insertarVisita(
                VisitaEntity(
                    clienteId = "CLI-01",
                    instalacionId = "INS-01",
                    fechaMillis = 1711929600000L,
                    numeroOt = "OT-PK-01",
                    tipoVisita = "SOLO_PRUEBAS",
                    ingenieroCargoId = "ING-01",
                    observaciones = "PK test"
                )
            )

            db.visitaDao().insertarVisitaTecnicos(
                listOf(VisitaTecnicoEntity(visitaId = visitaId, tecnicoId = "TEC-01"))
            )

            assertThrows(SQLiteConstraintException::class.java) {
                runBlocking {
                    db.visitaDao().insertarVisitaTecnicos(
                        listOf(VisitaTecnicoEntity(visitaId = visitaId, tecnicoId = "TEC-01"))
                    )
                }
            }
        }
    }

    @Test
    fun testInsertarVisitaConTecnicosRollbackOnConflict() {
        runBlocking {
            db = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                dbName
            )
                .addMigrations(MIGRATION_4_5)
                .build()

            val visita = VisitaEntity(
                clienteId = "CLI-01",
                instalacionId = "INS-01",
                fechaMillis = 1711929600000L,
                numeroOt = "OT-ROLLBACK-01",
                tipoVisita = "SOLO_PRUEBAS",
                ingenieroCargoId = "ING-01",
                observaciones = "Rollback test"
            )

            assertThrows(SQLiteConstraintException::class.java) {
                runBlocking {
                    db.visitaDao().insertarVisitaConTecnicos(
                        visita,
                        listOf("TEC-01", "TEC-01")
                    )
                }
            }

            val todasLasVisitas = db.visitaDao().obtenerVisitas().first()
            assertTrue(todasLasVisitas.isEmpty())
        }
    }
}
