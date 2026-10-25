package com.example.induztek.data.repository

import com.example.induztek.data.local.dao.CalibracionDao
import com.example.induztek.data.local.dao.InstrumentoDao
import com.example.induztek.data.local.entity.InstrumentoEntity
import com.example.induztek.domain.validation.ValidadorCalibracion
import kotlinx.coroutines.flow.Flow

class InstrumentoRepository(
    private val instrumentoDao: InstrumentoDao,
    private val calibracionDao: CalibracionDao,
) {

    suspend fun registrarInstrumento(instrumento: InstrumentoEntity): Long {
        return instrumentoDao.insertarInstrumento(instrumento)
    }

    fun observarInstrumentos(): Flow<List<InstrumentoEntity>> {
        return instrumentoDao.obtenerInstrumentos()
    }

    suspend fun obtenerInstrumentoPorId(id: Long): InstrumentoEntity? {
        return instrumentoDao.obtenerInstrumentoPorId(id)
    }

    suspend fun validarInstrumentoParaPrueba(instrumentoId: Long, fechaPrueba: String): String? {
        // 1. Comprobar que el instrumento existe utilizando InstrumentoDao
        val instrumento = instrumentoDao.obtenerInstrumentoPorId(instrumentoId)
            ?: return "El instrumento especificado no existe en el registro."

        // 2. Consultar con CalibracionDao la última calibración registrada hasta la fecha de prueba
        val ultimaCalibracion = calibracionDao.obtenerUltimaCalibracionHastaFecha(instrumentoId, fechaPrueba)
            ?: return "El instrumento '${instrumento.nombre}' no cuenta con registros de calibración válidos para la fecha de prueba."

        // 3. Validar vigencia mediante ValidadorCalibracion utilizando el historial oficial
        return ValidadorCalibracion.validarVigencia(
            fechaCalibracion = ultimaCalibracion.fechaCalibracion,
            fechaVencimiento = ultimaCalibracion.fechaVencimiento,
            fechaPrueba = fechaPrueba,
        )
    }
}
