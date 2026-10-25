package com.example.induztek.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.induztek.data.local.entity.InstrumentoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstrumentoDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarInstrumento(instrumento: InstrumentoEntity): Long

    @Query("SELECT * FROM instrumentos_medicion ORDER BY nombre ASC")
    fun obtenerInstrumentos(): Flow<List<InstrumentoEntity>>

    @Query("SELECT * FROM instrumentos_medicion WHERE id = :id LIMIT 1")
    suspend fun obtenerInstrumentoPorId(id: Long): InstrumentoEntity?
}
