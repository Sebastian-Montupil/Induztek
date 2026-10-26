package com.example.induztek.domain.validation

import com.example.induztek.model.InstrumentoUiState
import java.text.SimpleDateFormat
import java.util.Locale

data class InstrumentoValidacionResultado(
    val nombreError: Boolean,
    val tipoInstrumentoError: Boolean,
    val marcaError: Boolean,
    val numeroSerieError: Boolean,
    val fechaVencimientoError: Boolean,
) {
    val esValido: Boolean
        get() = !nombreError && !tipoInstrumentoError && !marcaError && !numeroSerieError && !fechaVencimientoError
}

object ValidadorInstrumento {

    private val dateRegex = Regex("""^\d{4}-\d{2}-\d{2}$""")

    fun validar(state: InstrumentoUiState): InstrumentoValidacionResultado {
        val nombreValido = state.nombre.isNotBlank()
        val tipoValido = state.tipoInstrumento.isNotBlank()
        val marcaValida = state.marca.isNotBlank()
        val numeroSerieValido = state.numeroSerie.isNotBlank()

        val fechaStr = state.fechaVencimientoCalibracion.trim()
        val fechaValida = if (fechaStr.isEmpty()) {
            true
        } else {
            if (!dateRegex.matches(fechaStr)) {
                false
            } else {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                    isLenient = false
                }
                try {
                    dateFormat.parse(fechaStr) != null
                } catch (_: Exception) {
                    false
                }
            }
        }

        return InstrumentoValidacionResultado(
            nombreError = !nombreValido,
            tipoInstrumentoError = !tipoValido,
            marcaError = !marcaValida,
            numeroSerieError = !numeroSerieValido,
            fechaVencimientoError = !fechaValida,
        )
    }
}
