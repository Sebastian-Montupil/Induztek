package com.example.induztek.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.induztek.data.local.entity.CalibracionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalibracionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarCalibracion(calibracion: CalibracionEntity): Long

    @Query("SELECT * FROM calibraciones WHERE instrumentoId = :instrumentoId ORDER BY fechaCalibracion DESC, id DESC")
    fun observarCalibracionesDeInstrumento(instrumentoId: Long): Flow<List<CalibracionEntity>>

    @Query("SELECT * FROM calibraciones WHERE instrumentoId = :instrumentoId AND fechaCalibracion <= :fechaPrueba ORDER BY fechaCalibracion DESC, id DESC LIMIT 1")
    suspend fun obtenerUltimaCalibracionHastaFecha(instrumentoId: Long, fechaPrueba: String): CalibracionEntity?
}
