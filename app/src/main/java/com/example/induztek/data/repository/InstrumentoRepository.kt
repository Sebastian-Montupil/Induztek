package com.example.induztek.data.repository

import com.example.induztek.data.local.dao.InstrumentoDao
import com.example.induztek.data.local.entity.InstrumentoEntity
import kotlinx.coroutines.flow.Flow

class InstrumentoRepository(
    private val instrumentoDao: InstrumentoDao,
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
}
