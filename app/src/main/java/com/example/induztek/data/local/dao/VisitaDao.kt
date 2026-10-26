package com.example.induztek.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.induztek.data.local.entity.VisitaEntity
import com.example.induztek.data.local.entity.VisitaTecnicoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitaDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarVisita(visita: VisitaEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarVisitaTecnicos(visitaTecnicos: List<VisitaTecnicoEntity>)

    @Query("SELECT * FROM visitas ORDER BY fechaMillis DESC")
    fun obtenerVisitas(): Flow<List<VisitaEntity>>

    @Query("SELECT * FROM visitas WHERE clienteId = :clienteId ORDER BY fechaMillis DESC")
    fun obtenerVisitasPorCliente(clienteId: String): Flow<List<VisitaEntity>>

    @Query("SELECT * FROM visitas WHERE id = :id LIMIT 1")
    suspend fun obtenerVisitaPorId(id: Long): VisitaEntity?

    @Query("SELECT tecnicoId FROM visita_tecnicos WHERE visitaId = :visitaId")
    suspend fun obtenerTecnicosDeVisita(visitaId: Long): List<String>

    @Transaction
    suspend fun insertarVisitaConTecnicos(visita: VisitaEntity, tecnicoIds: List<String>): Long {
        val visitaId = insertarVisita(visita)
        val asociaciones = tecnicoIds.map { tecnicoId ->
            VisitaTecnicoEntity(visitaId = visitaId, tecnicoId = tecnicoId)
        }
        if (asociaciones.isNotEmpty()) {
            insertarVisitaTecnicos(asociaciones)
        }
        return visitaId
    }
}
