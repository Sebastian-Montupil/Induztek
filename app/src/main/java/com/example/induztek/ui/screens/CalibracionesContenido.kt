package com.example.induztek.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induztek.data.local.entity.InstrumentoEntity
import com.example.induztek.ui.viewmodel.CalibracionViewModel

@Composable
fun CalibracionesContenido(
    instrumentos: List<InstrumentoEntity>,
    esJefeTecnico: Boolean
) {
    val vm: CalibracionViewModel = viewModel()

    val calibraciones by vm.calibraciones.collectAsStateWithLifecycle()
    val cargando by vm.cargando.collectAsStateWithLifecycle()
    val guardando by vm.guardando.collectAsStateWithLifecycle()
    val mensaje by vm.mensaje.collectAsStateWithLifecycle()

    var instrumentoId by remember { mutableStateOf<Long?>(null) }
    var fechaCalibracion by remember { mutableStateOf("") }
    var fechaVencimiento by remember { mutableStateOf("") }
    var pagina by remember { mutableStateOf(0) }

    val registros = calibraciones.sortedByDescending { it.id }
    val porPagina = 3
    val ultimaPagina = ((registros.size - 1) / porPagina).coerceAtLeast(0)
    val paginaActual = pagina.coerceIn(0, ultimaPagina)

    val visibles = registros
        .drop(paginaActual * porPagina)
        .take(porPagina)

    LaunchedEffect(calibraciones.size) {
        pagina = 0
    }

    LaunchedEffect(mensaje) {
        if (mensaje == "Calibración registrada exitosamente.") {
            fechaCalibracion = ""
            fechaVencimiento = ""
            pagina = 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider()

        Text(
            text = "Calibraciones",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Calibraciones registradas (${registros.size})",
            style = MaterialTheme.typography.titleSmall
        )

        if (cargando && registros.isEmpty()) {
            Text("Cargando calibraciones...")
        } else if (registros.isEmpty()) {
            Text("No hay calibraciones registradas.")
        } else {
            visibles.forEach { calibracion ->
                val instrumento = instrumentos.firstOrNull {
                    it.id == calibracion.instrumentoId
                }

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = instrumento?.nombre
                                ?: "Instrumento ${calibracion.instrumentoId}",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Text(
                            text = "Serie: ${instrumento?.numeroSerie ?: "N/A"}"
                        )

                        Text(
                            text = "Calibración: ${calibracion.fechaCalibracion}"
                        )

                        Text(
                            text = "Vencimiento: ${calibracion.fechaVencimiento}"
                        )

                        Text(
                            text = "Registro N.º ${calibracion.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = "Página ${paginaActual + 1} de ${ultimaPagina + 1}",
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        pagina = (paginaActual - 1).coerceAtLeast(0)
                    },
                    enabled = paginaActual > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Anterior")
                }

                OutlinedButton(
                    onClick = {
                        pagina = (paginaActual + 1)
                            .coerceAtMost(ultimaPagina)
                    },
                    enabled = paginaActual < ultimaPagina,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Siguiente")
                }
            }
        }

        Button(
            onClick = {
                vm.cargarCalibraciones()
                pagina = 0
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (cargando) "Cargando..."
                else "Actualizar calibraciones"
            )
        }

        if (esJefeTecnico) {
            HorizontalDivider()

            Text(
                text = "Registrar nueva calibración",
                style = MaterialTheme.typography.titleMedium
            )

            Text("Selecciona un instrumento:")

            instrumentos.forEach { instrumento ->
                OutlinedButton(
                    onClick = {
                        instrumentoId = instrumento.id
                        vm.limpiarMensaje()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val seleccionado =
                        instrumentoId == instrumento.id

                    Text(
                        "${if (seleccionado) "✓ " else ""}" +
                                "${instrumento.nombre} — ${instrumento.numeroSerie}"
                    )
                }
            }

            if (instrumentoId != null) {
                OutlinedTextField(
                    value = fechaCalibracion,
                    onValueChange = {
                        fechaCalibracion = it
                        vm.limpiarMensaje()
                    },
                    label = {
                        Text("Fecha calibración AAAA-MM-DD")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = fechaVencimiento,
                    onValueChange = {
                        fechaVencimiento = it
                        vm.limpiarMensaje()
                    },
                    label = {
                        Text("Fecha vencimiento AAAA-MM-DD")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                mensaje?.let { texto ->
                    val esExito =
                        texto == "Calibración registrada exitosamente."

                    Text(
                        text = if (esExito) "✓ $texto" else texto,
                        color = if (esExito) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }

                Button(
                    onClick = {
                        instrumentoId?.let { id ->
                            vm.registrarCalibracion(
                                instrumentoId = id,
                                fechaCalibracion = fechaCalibracion,
                                fechaVencimiento = fechaVencimiento
                            )
                        }
                    },
                    enabled = !guardando &&
                            fechaCalibracion.isNotBlank() &&
                            fechaVencimiento.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (guardando) "Guardando..."
                        else "Guardar calibración"
                    )
                }
            }
        }

        if (mensaje != null && (!esJefeTecnico || instrumentoId == null)) {
            Text(
                text = mensaje.orEmpty(),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
