package com.example.induztek.data.remote.terreno

import com.example.induztek.data.local.entity.CalibracionEntity
import com.example.induztek.data.local.entity.EquipoEntity
import com.example.induztek.data.local.entity.InstrumentoEntity
import com.example.induztek.data.local.entity.VisitaEntity

class VisitasRemoteDataSource(
    private val api: VisitasApi
) {

    suspend fun registrarVisita(
        visita: VisitaEntity,
        tecnicoIds: List<String>
    ): VisitaEntity {
        val request = RegistrarVisitaRequest(
            clienteId = visita.clienteId,
            instalacionId = visita.instalacionId,
            fechaMillis = visita.fechaMillis,
            numeroOt = visita.numeroOt,
            tipoVisita = visita.tipoVisita,
            ingenieroCargoId = visita.ingenieroCargoId,
            observaciones = visita.observaciones,
            tecnicoIds = tecnicoIds
        )

        return api.registrarVisita(request).toEntity()
    }

    suspend fun listarVisitas(): List<VisitaEntity> {
        return api.listarVisitas().map { it.toEntity() }
    }

    suspend fun obtenerVisita(id: Long): VisitaEntity {
        return api.obtenerVisita(id).toEntity()
    }

    private fun VisitaResponse.toEntity() = VisitaEntity(
        id = id,
        clienteId = clienteId,
        instalacionId = instalacionId,
        fechaMillis = fechaMillis,
        numeroOt = numeroOt,
        tipoVisita = tipoVisita,
        ingenieroCargoId = ingenieroCargoId,
        observaciones = observaciones.orEmpty()
    )
}

class EquiposRemoteDataSource(
    private val api: EquiposApi
) {

    suspend fun registrarEquipo(
        equipo: EquipoEntity
    ): EquipoEntity {
        val request = RegistrarEquipoRequest(
            clienteId = equipo.clienteId,
            instalacionId = equipo.instalacionId,
            nombre = equipo.nombre,
            tipo = equipo.tipo,
            marca = equipo.marca,
            modelo = equipo.modelo,
            numeroSerie = equipo.numeroSerie,
            codigoInterno = equipo.codigoInterno,
            observaciones = equipo.observaciones
        )

        return api.registrarEquipo(request).toEntity()
    }

    suspend fun listarEquipos(
        clienteId: String? = null,
        instalacionId: String? = null
    ): List<EquipoEntity> {
        return api.listarEquipos(
            clienteId = clienteId,
            instalacionId = instalacionId
        ).map { it.toEntity() }
    }

    suspend fun obtenerEquipo(id: Long): EquipoEntity {
        return api.obtenerEquipo(id).toEntity()
    }

    private fun EquipoResponse.toEntity() = EquipoEntity(
        id = id,
        clienteId = clienteId,
        instalacionId = instalacionId,
        nombre = nombre,
        tipo = tipo,
        marca = marca,
        modelo = modelo.orEmpty(),
        numeroSerie = numeroSerie,
        codigoInterno = codigoInterno.orEmpty(),
        observaciones = observaciones.orEmpty()
    )
}

class InstrumentosRemoteDataSource(
    private val api: InstrumentosApi
) {

    suspend fun registrarInstrumento(
        instrumento: InstrumentoEntity
    ): InstrumentoEntity {
        val request = RegistrarInstrumentoRequest(
            nombre = instrumento.nombre,
            tipoInstrumento = instrumento.tipoInstrumento,
            marca = instrumento.marca,
            modelo = instrumento.modelo,
            numeroSerie = instrumento.numeroSerie,
            codigoInterno = instrumento.codigoInterno,
            fechaVencimientoCalibracion =
                instrumento.fechaVencimientoCalibracion,
            observaciones = instrumento.observaciones
        )

        return api.registrarInstrumento(request).toEntity()
    }

    suspend fun listarInstrumentos(): List<InstrumentoEntity> {
        return api.listarInstrumentos().map { it.toEntity() }
    }

    suspend fun obtenerInstrumento(
        id: Long
    ): InstrumentoEntity {
        return api.obtenerInstrumento(id).toEntity()
    }

    private fun InstrumentoResponse.toEntity() =
        InstrumentoEntity(
            id = id,
            nombre = nombre,
            tipoInstrumento = tipoInstrumento,
            marca = marca,
            modelo = modelo.orEmpty(),
            numeroSerie = numeroSerie,
            codigoInterno = codigoInterno.orEmpty(),
            fechaVencimientoCalibracion =
                fechaVencimientoCalibracion,
            observaciones = observaciones.orEmpty()
        )
}

class CalibracionesRemoteDataSource(
    private val api: CalibracionesApi
) {

    suspend fun registrarCalibracion(
        calibracion: CalibracionEntity
    ): CalibracionEntity {
        val request = RegistrarCalibracionRequest(
            instrumentoId = calibracion.instrumentoId,
            fechaCalibracion = calibracion.fechaCalibracion,
            fechaVencimiento = calibracion.fechaVencimiento
        )

        return api.registrarCalibracion(request).toEntity()
    }

    suspend fun listarCalibraciones(
        instrumentoId: Long? = null
    ): List<CalibracionEntity> {
        return api.listarCalibraciones(
            instrumentoId = instrumentoId
        ).map { it.toEntity() }
    }

    suspend fun obtenerCalibracion(
        id: Long
    ): CalibracionEntity {
        return api.obtenerCalibracion(id).toEntity()
    }

    private fun CalibracionResponse.toEntity() =
        CalibracionEntity(
            id = id,
            instrumentoId = instrumentoId,
            fechaCalibracion = fechaCalibracion,
            fechaVencimiento = fechaVencimiento
        )
}
