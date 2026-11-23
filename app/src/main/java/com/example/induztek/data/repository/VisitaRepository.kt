
package com.example.induztek.data.repository

import com.example.induztek.data.local.dao.VisitaDao
import com.example.induztek.data.local.entity.VisitaEntity
import com.example.induztek.data.remote.terreno.VisitasRemoteDataSource
import com.example.induztek.model.Rol
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import retrofit2.HttpException

class VisitaRepository(
    private val visitaDao: VisitaDao,
    private val remoteDataSource: VisitasRemoteDataSource? = null
) {

    private val actualizaciones = MutableStateFlow(0L)

    suspend fun registrarVisita(
        visita: VisitaEntity,
        tecnicoIds: List<String>
    ): Long {
        if (remoteDataSource != null) {
            val guardada = remoteDataSource.registrarVisita(
                visita,
                tecnicoIds
            )

            actualizaciones.value += 1
            return guardada.id
        }

        return visitaDao.insertarVisitaConTecnicos(
            visita,
            tecnicoIds
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observarVisitas(): Flow<List<VisitaEntity>> {
        return SesionRepository.usuarioActual.flatMapLatest { usuario ->

            if (usuario == null || !usuario.activo) {
                flowOf(emptyList())

            } else if (
                usuario.rol == Rol.CLIENTE &&
                usuario.clienteId.isNullOrBlank()
            ) {
                flowOf(emptyList())

            } else if (remoteDataSource != null) {

                actualizaciones.flatMapLatest {
                    flow {
                        val visitas = remoteDataSource.listarVisitas()

                        val resultado = if (usuario.rol == Rol.CLIENTE) {
                            visitas.filter { visita ->
                                visita.clienteId == usuario.clienteId
                            }
                        } else {
                            visitas
                        }

                        emit(resultado)
                    }
                }

            } else if (usuario.rol == Rol.CLIENTE) {

                visitaDao.obtenerVisitasPorCliente(
                    usuario.clienteId.orEmpty()
                )

            } else {
                visitaDao.obtenerVisitas()
            }
        }
    }

    suspend fun obtenerVisitaPorId(id: Long): VisitaEntity? {
        val usuario = SesionRepository.usuarioActual.value

        if (usuario == null || !usuario.activo) {
            return null
        }

        val visita = if (remoteDataSource != null) {
            try {
                remoteDataSource.obtenerVisita(id)
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    return null
                }
                throw e
            }
        } else {
            visitaDao.obtenerVisitaPorId(id) ?: return null
        }

        if (usuario.rol == Rol.CLIENTE) {
            if (visita.clienteId != usuario.clienteId) {
                return null
            }
        }

        return visita
    }

    suspend fun obtenerTecnicosDeVisita(
        visitaId: Long
    ): List<String> {
        val usuario = SesionRepository.usuarioActual.value

        if (usuario == null || !usuario.activo) {
            return emptyList()
        }

        if (remoteDataSource != null) {
            throw UnsupportedOperationException(
                "La API todavía no permite consultar los técnicos de una visita."
            )
        }

        if (usuario.rol == Rol.CLIENTE) {
            val visita = visitaDao.obtenerVisitaPorId(visitaId)
                ?: return emptyList()

            if (visita.clienteId != usuario.clienteId) {
                return emptyList()
            }
        }

        return visitaDao.obtenerTecnicosDeVisita(visitaId)
    }
}
