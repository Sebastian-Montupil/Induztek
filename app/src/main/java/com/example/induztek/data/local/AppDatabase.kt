package com.example.induztek.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.induztek.data.local.dao.InstrumentoDao
import com.example.induztek.data.local.entity.InstrumentoEntity

@Database(
    entities = [InstrumentoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun instrumentoDao(): InstrumentoDao
}
