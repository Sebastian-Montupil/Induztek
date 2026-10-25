package com.example.induztek.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.induztek.data.local.dao.CalibracionDao
import com.example.induztek.data.local.dao.EquipoDao
import com.example.induztek.data.local.dao.InstrumentoDao
import com.example.induztek.data.local.dao.VisitaDao
import com.example.induztek.data.local.entity.CalibracionEntity
import com.example.induztek.data.local.entity.EquipoEntity
import com.example.induztek.data.local.entity.InstrumentoEntity
import com.example.induztek.data.local.entity.VisitaEntity
import com.example.induztek.data.local.entity.VisitaTecnicoEntity

@Database(
    entities = [
        InstrumentoEntity::class,
        CalibracionEntity::class,
        EquipoEntity::class,
        VisitaEntity::class,
        VisitaTecnicoEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun instrumentoDao(): InstrumentoDao
    abstract fun calibracionDao(): CalibracionDao
    abstract fun equipoDao(): EquipoDao
    abstract fun visitaDao(): VisitaDao
}
