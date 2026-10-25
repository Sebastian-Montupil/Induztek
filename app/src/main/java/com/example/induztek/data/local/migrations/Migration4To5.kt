package com.example.induztek.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `visitas` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `clienteId` TEXT NOT NULL,
                `instalacionId` TEXT NOT NULL,
                `fechaMillis` INTEGER NOT NULL,
                `numeroOt` TEXT NOT NULL,
                `tipoVisita` TEXT NOT NULL,
                `ingenieroCargoId` TEXT NOT NULL,
                `observaciones` TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_visitas_numeroOt` ON `visitas` (`numeroOt`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_visitas_clienteId` ON `visitas` (`clienteId`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_visitas_instalacionId` ON `visitas` (`instalacionId`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_visitas_ingenieroCargoId` ON `visitas` (`ingenieroCargoId`)"
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `visita_tecnicos` (
                `visitaId` INTEGER NOT NULL,
                `tecnicoId` TEXT NOT NULL,
                PRIMARY KEY(`visitaId`, `tecnicoId`),
                FOREIGN KEY(`visitaId`) REFERENCES `visitas`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_visita_tecnicos_tecnicoId` ON `visita_tecnicos` (`tecnicoId`)"
        )
    }
}
