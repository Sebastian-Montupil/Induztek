
package com.example.induztek.data.repository

import com.example.induztek.data.local.dao.CalibracionDao
import com.example.induztek.data.local.dao.InstrumentoDao
import com.example.induztek.data.local.entity.InstrumentoEntity
import com.example.induztek.data.remote.terreno.InstrumentosRemoteDataSource
import com.example.induztek.domain.validation.ValidadorCalibracion
import com.example.induztek.domain.validation.ValidadorPermisoInstrumentos
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

class InstrumentoRepository(
    private val instrumentoDao: InstrumentoDao,
    private val calibracionDao: CalibracionDao,
    private val remoteDataSource: InstrumentosRemoteDataSource? = null,
) {

    private val actualizaciones = MutableStateFlow(0L)

    suspend fun registrarInstrumento(
        instrumento: InstrumentoEntity
    ): Long {
        val usuario = SesionRepository.usuarioActual.value
        val errorPermiso =
            ValidadorPermisoInstrumentos.validarGestion(usuario)

        if (errorPermiso != null) {
            throw SecurityException(errorPermiso)
        }

        if (remoteDataSource != null) {
            val guardado =
                remoteDataSource.registrarInstrumento(instrumento)

            actualizaciones.value += 1L

            return guardado.id
        }

        return instrumentoDao.insertarInstrumento(instrumento)
    }

    suspend fun actualizarInstrumento(
        instrumento: InstrumentoEntity
    ): Int {
        val usuario = SesionRepository.usuarioActual.value
        val errorPermiso =
            ValidadorPermisoInstrumentos.validarGestion(usuario)

        if (errorPermiso != null) {
            throw SecurityException(errorPermiso)
        }

        if (remoteDataSource != null) {
            throw UnsupportedOperationException(
                "La actualización remota de instrumentos aún no está disponible."
            )
        }

        return instrumentoDao.actualizarInstrumento(instrumento)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observarInstrumentos(): Flow<List<InstrumentoEntity>> {
        if (remoteDataSource == null) {
            return instrumentoDao.obtenerInstrumentos()
        }

        return actualizaciones.flatMapLatest {
            flow {
                emit(remoteDataSource.listarInstrumentos())
            }
        }
    }

    suspend fun obtenerInstrumentoPorId(
        id: Long
    ): InstrumentoEntity? {
        if (remoteDataSource == null) {
            return instrumentoDao.obtenerInstrumentoPorId(id)
        }

        return try {
            remoteDataSource.obtenerInstrumento(id)
        } catch (e: HttpException) {
            if (e.code() == 404) null else throw e
        }
    }

    suspend fun validarInstrumentoParaPrueba(
        instrumentoId: Long,
        fechaPrueba: String
    ): String? {
        // Esta validación todavía utiliza calibraciones locales.
        // Se integrará con el backend en la siguiente etapa.
        val instrumento =
            instrumentoDao.obtenerInstrumentoPorId(instrumentoId)
                ?: return "El instrumento especificado no existe en el registro."

        val ultimaCalibracion =
            calibracionDao.obtenerUltimaCalibracionHastaFecha(
                instrumentoId,
                fechaPrueba
            ) ?: return "El instrumento '${instrumento.nombre}' no cuenta con registros de calibración válidos para la fecha de prueba."

        return ValidadorCalibracion.validarVigencia(
            fechaCalibracion = ultimaCalibracion.fechaCalibracion,
            fechaVencimiento = ultimaCalibracion.fechaVencimiento,
            fechaPrueba = fechaPrueba,
        )
    }
}
