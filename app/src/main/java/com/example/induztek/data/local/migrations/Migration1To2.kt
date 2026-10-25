package com.example.induztek.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
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
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_calibraciones_instrumentoId` ON `calibraciones` (`instrumentoId`)",
        )
    }
}
