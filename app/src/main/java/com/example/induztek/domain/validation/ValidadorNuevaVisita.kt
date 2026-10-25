package com.example.induztek.domain.validation

import com.example.induztek.model.CatalogoClientes
import com.example.induztek.model.NuevaVisitaUiState

object ValidadorNuevaVisita {

    data class ResultadoValidacion(
        val clienteError: Boolean,
        val instalacionError: Boolean,
        val fechaError: Boolean,
        val numeroOtError: Boolean,
        val tipoVisitaError: Boolean,
        val ingenieroError: Boolean,
        val esValido: Boolean
    )

    fun validar(state: NuevaVisitaUiState): ResultadoValidacion {
        val isClienteValid = state.clienteSeleccionadoId != null
        val isInstalacionValid = state.instalacionSeleccionadaId != null &&
                CatalogoClientes.getInstalacionesPorCliente(state.clienteSeleccionadoId)
                    .any { it.id == state.instalacionSeleccionadaId }
        val isFechaValid = state.fechaSelectedMillis != null
        val isNumeroOtValid = state.numeroOt.trim().isNotEmpty()
        val isTipoVisitaValid = state.tipoVisita != null
        val isIngenieroValid = state.ingenieroCargoId != null

        val isValid = isClienteValid && isInstalacionValid && isFechaValid &&
                isNumeroOtValid && isTipoVisitaValid && isIngenieroValid

        return ResultadoValidacion(
            clienteError = !isClienteValid,
            instalacionError = !isInstalacionValid,
            fechaError = !isFechaValid,
            numeroOtError = !isNumeroOtValid,
            tipoVisitaError = !isTipoVisitaValid,
            ingenieroError = !isIngenieroValid,
            esValido = isValid
        )
    }
}
