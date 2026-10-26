package com.example.induztek.domain.validation

import com.example.induztek.model.CatalogoClientes
import com.example.induztek.model.EquipoUiState

data class EquipoValidacionResultado(
    val clienteError: Boolean,
    val instalacionError: Boolean,
    val nombreError: Boolean,
    val tipoError: Boolean,
    val marcaError: Boolean,
    val numeroSerieError: Boolean,
) {
    val esValido: Boolean
        get() = !clienteError && !instalacionError && !nombreError && !tipoError && !marcaError && !numeroSerieError
}

object ValidadorEquipo {

    fun validar(state: EquipoUiState): EquipoValidacionResultado {
        val clienteValido = !state.clienteSeleccionadoId.isNullOrBlank() &&
            CatalogoClientes.clientes.any { it.id == state.clienteSeleccionadoId }

        val instalacionValida = !state.instalacionSeleccionadaId.isNullOrBlank() &&
            !state.clienteSeleccionadoId.isNullOrBlank() &&
            CatalogoClientes.getInstalacionesPorCliente(state.clienteSeleccionadoId)
                .any { it.id == state.instalacionSeleccionadaId }

        val nombreValido = state.nombre.isNotBlank()
        val tipoValido = state.tipo.isNotBlank()
        val marcaValida = state.marca.isNotBlank()
        val numeroSerieValido = state.numeroSerie.isNotBlank()

        return EquipoValidacionResultado(
            clienteError = !clienteValido,
            instalacionError = !instalacionValida,
            nombreError = !nombreValido,
            tipoError = !tipoValido,
            marcaError = !marcaValida,
            numeroSerieError = !numeroSerieValido,
        )
    }
}
