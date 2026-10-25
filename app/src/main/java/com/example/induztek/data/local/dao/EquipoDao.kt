package com.example.induztek.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.induztek.data.local.entity.EquipoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipoDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarEquipo(equipo: EquipoEntity): Long

    @Query("SELECT * FROM equipos WHERE instalacionId = :instalacionId ORDER BY nombre ASC")
    fun observarEquiposPorInstalacion(instalacionId: String): Flow<List<EquipoEntity>>

    @Query("SELECT * FROM equipos WHERE id = :id LIMIT 1")
    suspend fun obtenerEquipoPorId(id: Long): EquipoEntity?
}
