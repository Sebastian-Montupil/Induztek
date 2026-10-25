package com.example.induztek.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
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
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_equipos_numeroSerie` ON `equipos` (`numeroSerie`)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_equipos_clienteId` ON `equipos` (`clienteId`)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_equipos_instalacionId` ON `equipos` (`instalacionId`)",
        )
    }
}
