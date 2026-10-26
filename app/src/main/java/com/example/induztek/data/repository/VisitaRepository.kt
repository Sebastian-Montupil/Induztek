package com.example.induztek.data.repository

import com.example.induztek.data.local.dao.VisitaDao
import com.example.induztek.data.local.entity.VisitaEntity
import com.example.induztek.model.Rol
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class VisitaRepository(
    private val visitaDao: VisitaDao
) {

    suspend fun registrarVisita(visita: VisitaEntity, tecnicoIds: List<String>): Long {
        return visitaDao.insertarVisitaConTecnicos(visita, tecnicoIds)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observarVisitas(): Flow<List<VisitaEntity>> {
        return SesionRepository.usuarioActual.flatMapLatest { usuario ->
            if (usuario == null || !usuario.activo) {
                flowOf(emptyList())
            } else if (usuario.rol == Rol.CLIENTE) {
                val clienteId = usuario.clienteId
                if (clienteId.isNullOrBlank()) {
                    flowOf(emptyList())
                } else {
                    visitaDao.obtenerVisitasPorCliente(clienteId)
                }
            } else {
                visitaDao.obtenerVisitas()
            }
        }
    }

    suspend fun obtenerVisitaPorId(id: Long): VisitaEntity? {
        val usuario = SesionRepository.usuarioActual.value
        if (usuario == null || !usuario.activo) return null
        val visita = visitaDao.obtenerVisitaPorId(id) ?: return null
        if (usuario.rol == Rol.CLIENTE) {
            if (visita.clienteId != usuario.clienteId) return null
        }
        return visita
    }

    suspend fun obtenerTecnicosDeVisita(visitaId: Long): List<String> {
        val usuario = SesionRepository.usuarioActual.value
        if (usuario == null || !usuario.activo) return emptyList()
        if (usuario.rol == Rol.CLIENTE) {
            val visita = visitaDao.obtenerVisitaPorId(visitaId) ?: return emptyList()
            if (visita.clienteId != usuario.clienteId) return emptyList()
        }
        return visitaDao.obtenerTecnicosDeVisita(visitaId)
    }
}
