package com.example.induztek.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induztek.viewmodel.ClimaViewModel
import java.util.Locale

@Composable
fun ClimaPanel(
    modelo: ClimaViewModel = viewModel()
) {
    val estado by modelo.uiState.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Clima de referencia para terreno",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Santiago, Chile · API externa Open-Meteo",
                style = MaterialTheme.typography.bodySmall
            )

            when {
                estado.cargando -> {
                    CircularProgressIndicator()
                }

                estado.clima != null -> {
                    val clima = estado.clima!!

                    Text(
                        text = String.format(
                            Locale.US,
                            "Temperatura: %.1f °C",
                            clima.temperatura
                        )
                    )

                    Text(
                        text = "Humedad: ${clima.humedad}%"
                    )

                    Text(
                        text = String.format(
                            Locale.US,
                            "Precipitación: %.1f mm",
                            clima.precipitacion
                        )
                    )

                    Text(
                        text = "Actualización: ${clima.fechaHora}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                estado.error != null -> {
                    Text(
                        text = estado.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            TextButton(
                onClick = { modelo.actualizar() },
                enabled = !estado.cargando
            ) {
                Text("Actualizar clima")
            }
        }
    }
}
