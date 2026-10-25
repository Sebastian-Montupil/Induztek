package com.example.induztek.domain.validation

import com.example.induztek.model.EquipoUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorEquipoTest {

    @Test
    fun validacion_registroValido_esValido() {
        val state = EquipoUiState(
            clienteSeleccionadoId = "CLI-01",
            instalacionSeleccionadaId = "INS-01",
            nombre = "Tablero General",
            tipo = "Tablero",
            marca = "Schneider",
            numeroSerie = "SN-TEST-001",
        )
        val resultado = ValidadorEquipo.validar(state)
        assertTrue(resultado.esValido)
        assertFalse(resultado.clienteError)
        assertFalse(resultado.instalacionError)
        assertFalse(resultado.nombreError)
        assertFalse(resultado.tipoError)
        assertFalse(resultado.marcaError)
        assertFalse(resultado.numeroSerieError)
    }

    @Test
    fun validacion_clienteInexistenteOnull_falla() {
        val stateNull = EquipoUiState(
            clienteSeleccionadoId = null,
            instalacionSeleccionadaId = "INS-01",
            nombre = "Tablero",
            tipo = "Tablero",
            marca = "Schneider",
            numeroSerie = "SN-TEST-002",
        )
        val resultadoNull = ValidadorEquipo.validar(stateNull)
        assertFalse(resultadoNull.esValido)
        assertTrue(resultadoNull.clienteError)

        val stateInv = EquipoUiState(
            clienteSeleccionadoId = "CLI-999",
            instalacionSeleccionadaId = "INS-01",
            nombre = "Tablero",
            tipo = "Tablero",
            marca = "Schneider",
            numeroSerie = "SN-TEST-002",
        )
        val resultadoInv = ValidadorEquipo.validar(stateInv)
        assertFalse(resultadoInv.esValido)
        assertTrue(resultadoInv.clienteError)
    }

    @Test
    fun validacion_instalacionDeOtroCliente_falla() {
        val state = EquipoUiState(
            clienteSeleccionadoId = "CLI-01",
            instalacionSeleccionadaId = "INS-02",
            nombre = "Tablero",
            tipo = "Tablero",
            marca = "Schneider",
            numeroSerie = "SN-TEST-003",
        )
        val resultado = ValidadorEquipo.validar(state)
        assertFalse(resultado.esValido)
        assertTrue(resultado.instalacionError)
    }

    @Test
    fun validacion_camposObligatoriosVacios_falla() {
        val stateVacio = EquipoUiState(
            clienteSeleccionadoId = "CLI-01",
            instalacionSeleccionadaId = "INS-01",
            nombre = "",
            tipo = "",
            marca = "",
            numeroSerie = "",
        )
        val resultadoVacio = ValidadorEquipo.validar(stateVacio)
        assertFalse(resultadoVacio.esValido)
        assertTrue(resultadoVacio.nombreError)
        assertTrue(resultadoVacio.tipoError)
        assertTrue(resultadoVacio.marcaError)
        assertTrue(resultadoVacio.numeroSerieError)

        val stateEspacios = EquipoUiState(
            clienteSeleccionadoId = "CLI-01",
            instalacionSeleccionadaId = "INS-01",
            nombre = "   ",
            tipo = "   ",
            marca = "   ",
            numeroSerie = "   ",
        )
        val resultadoEspacios = ValidadorEquipo.validar(stateEspacios)
        assertFalse(resultadoEspacios.esValido)
        assertTrue(resultadoEspacios.nombreError)
        assertTrue(resultadoEspacios.tipoError)
        assertTrue(resultadoEspacios.marcaError)
        assertTrue(resultadoEspacios.numeroSerieError)
    }
}
