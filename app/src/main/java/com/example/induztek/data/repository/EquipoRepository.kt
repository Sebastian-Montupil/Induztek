
package com.example.induztek.data.repository

import com.example.induztek.data.local.dao.EquipoDao
import com.example.induztek.data.local.entity.EquipoEntity
import com.example.induztek.data.remote.terreno.EquiposRemoteDataSource
import com.example.induztek.model.CatalogoClientes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

class EquipoRepository(
    private val equipoDao: EquipoDao,
    private val remoteDataSource: EquiposRemoteDataSource? = null
) {

    private val actualizaciones = MutableStateFlow(0L)

    suspend fun registrarEquipo(equipo: EquipoEntity): Long {

        val clienteExiste = CatalogoClientes.clientes.any {
            it.id == equipo.clienteId
        }

        if (!clienteExiste) {
            throw IllegalArgumentException(
                "El cliente especificado no existe en el catálogo."
            )
        }

        val instalacionesCliente =
            CatalogoClientes.getInstalacionesPorCliente(equipo.clienteId)

        val instalacionExiste = instalacionesCliente.any {
            it.id == equipo.instalacionId
        }

        if (!instalacionExiste) {
            throw IllegalArgumentException(
                "La instalación especificada no existe o no pertenece al cliente indicado."
            )
        }

        if (equipo.nombre.isBlank()) {
            throw IllegalArgumentException(
                "El nombre del equipo no puede estar vacío."
            )
        }

        if (equipo.tipo.isBlank()) {
            throw IllegalArgumentException(
                "El tipo de equipo no puede estar vacío."
            )
        }

        if (equipo.marca.isBlank()) {
            throw IllegalArgumentException(
                "La marca del equipo no puede estar vacía."
            )
        }

        if (equipo.numeroSerie.isBlank()) {
            throw IllegalArgumentException(
                "El número de serie del equipo no puede estar vacío."
            )
        }

        if (remoteDataSource != null) {
            val guardado = remoteDataSource.registrarEquipo(equipo)

            actualizaciones.value += 1

            return guardado.id
        }

        return equipoDao.insertarEquipo(equipo)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observarEquiposPorInstalacion(
        instalacionId: String
    ): Flow<List<EquipoEntity>> {

        if (remoteDataSource == null) {
            return equipoDao.observarEquiposPorInstalacion(
                instalacionId
            )
        }

        return actualizaciones.flatMapLatest {
            flow {
                val clienteId = CatalogoClientes.instalaciones
                    .firstOrNull { it.id == instalacionId }
                    ?.clienteId
                    ?: throw IllegalArgumentException(
                        "La instalación seleccionada no existe."
                    )

                val equipos = remoteDataSource.listarEquipos(
                    clienteId = clienteId,
                    instalacionId = instalacionId
                )

                emit(equipos)
            }
        }
    }

    suspend fun obtenerEquipoPorId(id: Long): EquipoEntity? {

        if (remoteDataSource == null) {
            return equipoDao.obtenerEquipoPorId(id)
        }

        return try {
            remoteDataSource.obtenerEquipo(id)
        } catch (e: HttpException) {
            if (e.code() == 404) {
                null
            } else {
                throw e
            }
        }
    }
}
