package com.example.induztek.domain.validation

import java.text.SimpleDateFormat
import java.util.Locale

object ValidadorCalibracion {

    private val dateRegex = Regex("""^\d{4}-\d{2}-\d{2}$""")

    fun validarVigencia(
        fechaCalibracion: String?,
        fechaVencimiento: String?,
        fechaPrueba: String,
    ): String? {
        // 1. Rechazar si no existe información de calibración o fecha de prueba vacía
        if (fechaCalibracion.isNullOrBlank() || fechaVencimiento.isNullOrBlank()) {
            return "El instrumento no cuenta con información de calibración registrada."
        }
        if (fechaPrueba.isBlank()) {
            return "La fecha de prueba no puede estar vacía."
        }

        // 2. Validar formato estricto con Expresión Regular (AAAA-MM-DD)
        if (!dateRegex.matches(fechaCalibracion)) {
            return "La fecha de calibración tiene un formato inválido (use AAAA-MM-DD)."
        }
        if (!dateRegex.matches(fechaVencimiento)) {
            return "La fecha de vencimiento tiene un formato inválido (use AAAA-MM-DD)."
        }
        if (!dateRegex.matches(fechaPrueba)) {
            return "La fecha de prueba tiene un formato inválido (use AAAA-MM-DD)."
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            isLenient = false
        }

        // 3. Validar existencia real de las fechas (ej. rechazar 31 de febrero)
        val calDate = try {
            dateFormat.parse(fechaCalibracion)
        } catch (_: Exception) {
            return "La fecha de calibración no es una fecha real (use AAAA-MM-DD)."
        }

        val vencDate = try {
            dateFormat.parse(fechaVencimiento)
        } catch (_: Exception) {
            return "La fecha de vencimiento no es una fecha real (use AAAA-MM-DD)."
        }

        val pruebaDate = try {
            dateFormat.parse(fechaPrueba)
        } catch (_: Exception) {
            return "La fecha de prueba no es una fecha real (use AAAA-MM-DD)."
        }

        if ((calDate == null) || (vencDate == null) || (pruebaDate == null)) {
            return "Error al procesar las fechas de calibración."
        }

        // 4. Rechazar si el vencimiento es anterior a la fecha de calibración
        if (vencDate.before(calDate)) {
            return "La fecha de vencimiento no puede ser anterior a la fecha de calibración."
        }

        // 5. Rechazar si la fecha de prueba es anterior a la calibración
        if (pruebaDate.before(calDate)) {
            return "La fecha de prueba es anterior a la fecha de calibración del instrumento."
        }

        // 6. Rechazar si la fecha de prueba es posterior al vencimiento (Calibración vencida - RN-11)
        if (pruebaDate.after(vencDate)) {
            return "El instrumento tiene la calibración vencida para la fecha de prueba."
        }

        // Si cumple todas las validaciones, retorna null (permitido)
        return null
    }
}
