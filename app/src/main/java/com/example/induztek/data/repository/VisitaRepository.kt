package com.example.induztek.data.repository

import com.example.induztek.data.local.dao.VisitaDao
import com.example.induztek.data.local.entity.VisitaEntity
import kotlinx.coroutines.flow.Flow

class VisitaRepository(
    private val visitaDao: VisitaDao
) {

    suspend fun registrarVisita(visita: VisitaEntity, tecnicoIds: List<String>): Long {
        return visitaDao.insertarVisitaConTecnicos(visita, tecnicoIds)
    }

    fun observarVisitas(): Flow<List<VisitaEntity>> {
        return visitaDao.obtenerVisitas()
    }

    suspend fun obtenerVisitaPorId(id: Long): VisitaEntity? {
        return visitaDao.obtenerVisitaPorId(id)
    }

    suspend fun obtenerTecnicosDeVisita(visitaId: Long): List<String> {
        return visitaDao.obtenerTecnicosDeVisita(visitaId)
    }
}
