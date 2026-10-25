package com.example.induztek.data.repository

import com.example.induztek.data.local.dao.EquipoDao
import com.example.induztek.data.local.entity.EquipoEntity
import com.example.induztek.model.CatalogoClientes
import kotlinx.coroutines.flow.Flow

class EquipoRepository(
    private val equipoDao: EquipoDao,
) {

    suspend fun registrarEquipo(equipo: EquipoEntity): Long {
        val clienteExiste = CatalogoClientes.clientes.any { it.id == equipo.clienteId }
        if (!clienteExiste) {
            throw IllegalArgumentException("El cliente especificado no existe en el catálogo.")
        }

        val instalacionesCliente = CatalogoClientes.getInstalacionesPorCliente(equipo.clienteId)
        val instalacionExiste = instalacionesCliente.any { it.id == equipo.instalacionId }
        if (!instalacionExiste) {
            throw IllegalArgumentException("La instalación especificada no existe o no pertenece al cliente indicado.")
        }

        if (equipo.nombre.isBlank()) {
            throw IllegalArgumentException("El nombre del equipo no puede estar vacío.")
        }
        if (equipo.tipo.isBlank()) {
            throw IllegalArgumentException("El tipo de equipo no puede estar vacío.")
        }
        if (equipo.marca.isBlank()) {
            throw IllegalArgumentException("La marca del equipo no puede estar vacía.")
        }
        if (equipo.numeroSerie.isBlank()) {
            throw IllegalArgumentException("El número de serie del equipo no puede estar vacío.")
        }

        return equipoDao.insertarEquipo(equipo)
    }

    fun observarEquiposPorInstalacion(instalacionId: String): Flow<List<EquipoEntity>> {
        return equipoDao.observarEquiposPorInstalacion(instalacionId)
    }

    suspend fun obtenerEquipoPorId(id: Long): EquipoEntity? {
        return equipoDao.obtenerEquipoPorId(id)
    }
}
