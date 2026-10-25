package com.example.induztek.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.induztek.data.local.dao.CalibracionDao
import com.example.induztek.data.local.dao.InstrumentoDao
import com.example.induztek.data.local.entity.CalibracionEntity
import com.example.induztek.data.local.entity.InstrumentoEntity

@Database(
    entities = [InstrumentoEntity::class, CalibracionEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun instrumentoDao(): InstrumentoDao
    abstract fun calibracionDao(): CalibracionDao
}
