package com.example.induztek.data.local

import android.content.Context
import androidx.room.Room
import com.example.induztek.data.local.migrations.MIGRATION_1_2
import com.example.induztek.data.local.migrations.MIGRATION_2_3
import com.example.induztek.data.local.migrations.MIGRATION_3_4

object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "induztek_terreno.db",
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
                .also { INSTANCE = it }
        }
    }
}
