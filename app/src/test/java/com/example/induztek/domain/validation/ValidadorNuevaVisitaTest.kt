package com.example.induztek.domain.validation

import com.example.induztek.model.NuevaVisitaUiState
import com.example.induztek.model.TipoVisita
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorNuevaVisitaTest {

    @Test
    fun validar_estadoValido_retornaTrue() {
        val state = NuevaVisitaUiState(
            clienteSeleccionadoId = "CLI-01",
            instalacionSeleccionadaId = "INS-01",
            fechaSelectedMillis = 1711929600000L,
            numeroOt = "OT-2026-1187",
            tipoVisita = TipoVisita.SOLO_PRUEBAS,
            ingenieroCargoId = "ING-01"
        )

        val resultado = ValidadorNuevaVisita.validar(state)

        assertTrue(resultado.esValido)
        assertFalse(resultado.clienteError)
        assertFalse(resultado.instalacionError)
        assertFalse(resultado.fechaError)
        assertFalse(resultado.numeroOtError)
        assertFalse(resultado.tipoVisitaError)
        assertFalse(resultado.ingenieroError)
    }

    @Test
    fun validar_clienteNulo_retornaErrorCliente() {
        val state = NuevaVisitaUiState(
            clienteSeleccionadoId = null,
            instalacionSeleccionadaId = "INS-01",
            fechaSelectedMillis = 1711929600000L,
            numeroOt = "OT-2026-1187",
            tipoVisita = TipoVisita.SOLO_PRUEBAS,
            ingenieroCargoId = "ING-01"
        )

        val resultado = ValidadorNuevaVisita.validar(state)

        assertFalse(resultado.esValido)
        assertTrue(resultado.clienteError)
    }

    @Test
    fun validar_instalacionNula_retornaErrorInstalacion() {
        val state = NuevaVisitaUiState(
            clienteSeleccionadoId = "CLI-01",
            instalacionSeleccionadaId = null,
            fechaSelectedMillis = 1711929600000L,
            numeroOt = "OT-2026-1187",
            tipoVisita = TipoVisita.SOLO_PRUEBAS,
            ingenieroCargoId = "ING-01"
        )

        val resultado = ValidadorNuevaVisita.validar(state)

        assertFalse(resultado.esValido)
        assertTrue(resultado.instalacionError)
    }

    @Test
    fun validar_instalacionInvalidaParaCliente_retornaErrorInstalacion() {
        val state = NuevaVisitaUiState(
            clienteSeleccionadoId = "CLI-01",
            instalacionSeleccionadaId = "INS-02",
            fechaSelectedMillis = 1711929600000L,
            numeroOt = "OT-2026-1187",
            tipoVisita = TipoVisita.SOLO_PRUEBAS,
            ingenieroCargoId = "ING-01"
        )

        val resultado = ValidadorNuevaVisita.validar(state)

        assertFalse(resultado.esValido)
        assertTrue(resultado.instalacionError)
    }

    @Test
    fun validar_fechaNula_retornaErrorFecha() {
        val state = NuevaVisitaUiState(
            clienteSeleccionadoId = "CLI-01",
            instalacionSeleccionadaId = "INS-01",
            fechaSelectedMillis = null,
            numeroOt = "OT-2026-1187",
            tipoVisita = TipoVisita.SOLO_PRUEBAS,
            ingenieroCargoId = "ING-01"
        )

        val resultado = ValidadorNuevaVisita.validar(state)

        assertFalse(resultado.esValido)
        assertTrue(resultado.fechaError)
    }

    @Test
    fun validar_numeroOtVacioString_retornaErrorOt() {
        val state = NuevaVisitaUiState(
            clienteSeleccionadoId = "CLI-01",
            instalacionSeleccionadaId = "INS-01",
            fechaSelectedMillis = 1711929600000L,
            numeroOt = "",
            tipoVisita = TipoVisita.SOLO_PRUEBAS,
            ingenieroCargoId = "ING-01"
        )

        val resultado = ValidadorNuevaVisita.validar(state)

        assertFalse(resultado.esValido)
        assertTrue(resultado.numeroOtError)
    }

    @Test
    fun validar_numeroOtSoloEspacios_retornaErrorOt() {
        val state = NuevaVisitaUiState(
            clienteSeleccionadoId = "CLI-01",
            instalacionSeleccionadaId = "INS-01",
            fechaSelectedMillis = 1711929600000L,
            numeroOt = "   ",
            tipoVisita = TipoVisita.SOLO_PRUEBAS,
            ingenieroCargoId = "ING-01"
        )

        val resultado = ValidadorNuevaVisita.validar(state)

        assertFalse(resultado.esValido)
        assertTrue(resultado.numeroOtError)
    }

    @Test
    fun validar_tipoVisitaNulo_retornaErrorTipoVisita() {
        val state = NuevaVisitaUiState(
            clienteSeleccionadoId = "CLI-01",
            instalacionSeleccionadaId = "INS-01",
            fechaSelectedMillis = 1711929600000L,
            numeroOt = "OT-2026-1187",
            tipoVisita = null,
            ingenieroCargoId = "ING-01"
        )

        val resultado = ValidadorNuevaVisita.validar(state)

        assertFalse(resultado.esValido)
        assertTrue(resultado.tipoVisitaError)
    }

    @Test
    fun validar_ingenieroNulo_retornaErrorIngeniero() {
        val state = NuevaVisitaUiState(
            clienteSeleccionadoId = "CLI-01",
            instalacionSeleccionadaId = "INS-01",
            fechaSelectedMillis = 1711929600000L,
            numeroOt = "OT-2026-1187",
            tipoVisita = TipoVisita.SOLO_PRUEBAS,
            ingenieroCargoId = null
        )

        val resultado = ValidadorNuevaVisita.validar(state)

        assertFalse(resultado.esValido)
        assertTrue(resultado.ingenieroError)
    }
}
