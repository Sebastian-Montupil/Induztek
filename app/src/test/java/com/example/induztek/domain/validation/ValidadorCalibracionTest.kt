package com.example.induztek.domain.validation

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadorCalibracionTest {

    @Test
    fun calibracionVigente_retornaNull() {
        val resultado = ValidadorCalibracion.validarVigencia(
            fechaCalibracion = "2026-01-01",
            fechaVencimiento = "2026-12-31",
            fechaPrueba = "2026-06-15",
        )
        assertNull(resultado)
    }

    @Test
    fun fechaPruebaIgualCalibracion_retornaNull() {
        val resultado = ValidadorCalibracion.validarVigencia(
            fechaCalibracion = "2026-01-01",
            fechaVencimiento = "2026-12-31",
            fechaPrueba = "2026-01-01",
        )
        assertNull(resultado)
    }

    @Test
    fun fechaPruebaIgualVencimiento_retornaNull() {
        val resultado = ValidadorCalibracion.validarVigencia(
            fechaCalibracion = "2026-01-01",
            fechaVencimiento = "2026-12-31",
            fechaPrueba = "2026-12-31",
        )
        assertNull(resultado)
    }

    @Test
    fun fechaPruebaPosteriorVencimiento_retornaError() {
        val resultado = ValidadorCalibracion.validarVigencia(
            fechaCalibracion = "2026-01-01",
            fechaVencimiento = "2026-12-31",
            fechaPrueba = "2027-01-01",
        )
        assertNotNull(resultado)
    }

    @Test
    fun fechaPruebaAnteriorCalibracion_retornaError() {
        val resultado = ValidadorCalibracion.validarVigencia(
            fechaCalibracion = "2026-01-01",
            fechaVencimiento = "2026-12-31",
            fechaPrueba = "2025-12-31",
        )
        assertNotNull(resultado)
    }

    @Test
    fun vencimientoAnteriorCalibracion_retornaError() {
        val resultado = ValidadorCalibracion.validarVigencia(
            fechaCalibracion = "2026-12-31",
            fechaVencimiento = "2026-01-01",
            fechaPrueba = "2026-06-15",
        )
        assertNotNull(resultado)
    }

    @Test
    fun sinInformacionCalibracion_retornaError() {
        val resultado = ValidadorCalibracion.validarVigencia(
            fechaCalibracion = null,
            fechaVencimiento = null,
            fechaPrueba = "2026-06-15",
        )
        assertNotNull(resultado)
    }

    @Test
    fun fechaPruebaVacia_retornaError() {
        val resultado = ValidadorCalibracion.validarVigencia(
            fechaCalibracion = "2026-01-01",
            fechaVencimiento = "2026-12-31",
            fechaPrueba = "",
        )
        assertNotNull(resultado)
    }

    @Test
    fun formatoIncorrecto_unDigitoMes_retornaError() {
        val resultado = ValidadorCalibracion.validarVigencia(
            fechaCalibracion = "2026-1-24",
            fechaVencimiento = "2026-12-31",
            fechaPrueba = "2026-06-15",
        )
        assertNotNull(resultado)
    }

    @Test
    fun caracteresAdicionales_retornaError() {
        val resultado = ValidadorCalibracion.validarVigencia(
            fechaCalibracion = "2026-10-24abc",
            fechaVencimiento = "2026-12-31",
            fechaPrueba = "2026-11-15",
        )
        assertNotNull(resultado)
    }

    @Test
    fun fechaImposible_febrero31_retornaError() {
        val resultado = ValidadorCalibracion.validarVigencia(
            fechaCalibracion = "2026-02-31",
            fechaVencimiento = "2026-12-31",
            fechaPrueba = "2026-06-15",
        )
        assertNotNull(resultado)
    }
}
