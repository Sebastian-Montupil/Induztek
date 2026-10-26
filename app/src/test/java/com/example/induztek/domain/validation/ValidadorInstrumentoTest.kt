package com.example.induztek.domain.validation

import com.example.induztek.model.InstrumentoUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorInstrumentoTest {

    @Test
    fun validacion_instrumentoValido_esValido() {
        val state = InstrumentoUiState(
            nombre = "Multímetro Digital",
            tipoInstrumento = "Multímetro",
            marca = "Fluke",
            modelo = "87V",
            numeroSerie = "FLUKE-001",
            codigoInterno = "INS-001",
            fechaVencimientoCalibracion = "2025-12-31",
            observaciones = "Calibrado",
        )
        val resultado = ValidadorInstrumento.validar(state)
        assertTrue(resultado.esValido)
        assertFalse(resultado.nombreError)
        assertFalse(resultado.tipoInstrumentoError)
        assertFalse(resultado.marcaError)
        assertFalse(resultado.numeroSerieError)
        assertFalse(resultado.fechaVencimientoError)
    }

    @Test
    fun validacion_camposObligatoriosVacios_falla() {
        val stateVacio = InstrumentoUiState(
            nombre = "",
            tipoInstrumento = "",
            marca = "",
            numeroSerie = "",
        )
        val resultado = ValidadorInstrumento.validar(stateVacio)
        assertFalse(resultado.esValido)
        assertTrue(resultado.nombreError)
        assertTrue(resultado.tipoInstrumentoError)
        assertTrue(resultado.marcaError)
        assertTrue(resultado.numeroSerieError)

        val stateEspacios = InstrumentoUiState(
            nombre = "   ",
            tipoInstrumento = "   ",
            marca = "   ",
            numeroSerie = "   ",
        )
        val resultadoEspacios = ValidadorInstrumento.validar(stateEspacios)
        assertFalse(resultadoEspacios.esValido)
        assertTrue(resultadoEspacios.nombreError)
        assertTrue(resultadoEspacios.tipoInstrumentoError)
        assertTrue(resultadoEspacios.marcaError)
        assertTrue(resultadoEspacios.numeroSerieError)
    }

    @Test
    fun validacion_fechaOpcionalVacia_esValido() {
        val state = InstrumentoUiState(
            nombre = "Pinza Amperimétrica",
            tipoInstrumento = "Pinza",
            marca = "Fluke",
            numeroSerie = "FLUKE-002",
            fechaVencimientoCalibracion = "",
        )
        val resultado = ValidadorInstrumento.validar(state)
        assertTrue(resultado.esValido)
        assertFalse(resultado.fechaVencimientoError)
    }

    @Test
    fun validacion_fechaFormatoInvalido_falla() {
        val stateInvalido = InstrumentoUiState(
            nombre = "Pinza Amperimétrica",
            tipoInstrumento = "Pinza",
            marca = "Fluke",
            numeroSerie = "FLUKE-003",
            fechaVencimientoCalibracion = "31-12-2025",
        )
        val resultadoInvalido = ValidadorInstrumento.validar(stateInvalido)
        assertFalse(resultadoInvalido.esValido)
        assertTrue(resultadoInvalido.fechaVencimientoError)

        val stateNoReal = InstrumentoUiState(
            nombre = "Pinza Amperimétrica",
            tipoInstrumento = "Pinza",
            marca = "Fluke",
            numeroSerie = "FLUKE-003",
            fechaVencimientoCalibracion = "2025-02-30",
        )
        val resultadoNoReal = ValidadorInstrumento.validar(stateNoReal)
        assertFalse(resultadoNoReal.esValido)
        assertTrue(resultadoNoReal.fechaVencimientoError)
    }
}
