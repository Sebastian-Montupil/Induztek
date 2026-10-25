package com.example.induztek.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val cursor = db.query(
            "SELECT numeroSerie, COUNT(*) as count FROM instrumentos_medicion GROUP BY numeroSerie HAVING count > 1",
        )
        cursor.use {
            if (it.moveToFirst()) {
                throw IllegalStateException(
                    "Migración de versión 2 a 3 abortada: Se detectaron números de serie duplicados en la base de datos. " +
                        "No se puede aplicar la restricción UNIQUE sin resolver los duplicados manualmente.",
                )
            }
        }

        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_instrumentos_medicion_numeroSerie` ON `instrumentos_medicion` (`numeroSerie`)",
        )
    }
}
