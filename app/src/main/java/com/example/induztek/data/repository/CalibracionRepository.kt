package com.example.induztek.data.repository

import com.example.induztek.data.local.dao.CalibracionDao
import com.example.induztek.data.local.entity.CalibracionEntity
import com.example.induztek.domain.validation.ValidadorPermisoInstrumentos

class CalibracionRepository(
    private val calibracionDao: CalibracionDao,
) {

    suspend fun registrarCalibracion(calibracion: CalibracionEntity): Long {
        val usuario = SesionRepository.usuarioActual.value
        val errorPermiso = ValidadorPermisoInstrumentos.validarGestion(usuario)
        if (errorPermiso != null) {
            throw SecurityException(errorPermiso)
        }
        return calibracionDao.insertarCalibracion(calibracion)
    }

    suspend fun actualizarCalibracion(calibracion: CalibracionEntity): Int {
        val usuario = SesionRepository.usuarioActual.value
        val errorPermiso = ValidadorPermisoInstrumentos.validarGestion(usuario)
        if (errorPermiso != null) {
            throw SecurityException(errorPermiso)
        }
        return calibracionDao.actualizarCalibracion(calibracion)
    }
}
