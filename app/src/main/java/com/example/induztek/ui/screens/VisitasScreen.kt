package com.example.induztek.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induztek.R
import com.example.induztek.data.local.DatabaseProvider
import com.example.induztek.data.repository.VisitaRepository
import com.example.induztek.model.CatalogoClientes
import com.example.induztek.model.CatalogoPersonal
import com.example.induztek.ui.theme.InduztekTheme
import com.example.induztek.viewmodel.VisitasViewModel
import com.example.induztek.viewmodel.VisitasViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitasScreen(
    onBackClick: () -> Unit,
    onNavigateToNuevaVisita: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: VisitasViewModel = viewModel(
        factory = VisitasViewModelFactory(
            VisitaRepository(DatabaseProvider.getDatabase(context).visitaDao())
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.visitas_top_bar_title))
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.visitas_back_button_content_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.visitas_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.visitas.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.visitas_empty_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.visitas) { visita ->
                        val clienteNombre = CatalogoClientes.clientes.find { it.id == visita.clienteId }?.nombre ?: visita.clienteId
                        val instalacionNombre = CatalogoClientes.getInstalacionesPorCliente(visita.clienteId).find { it.id == visita.instalacionId }?.nombre ?: visita.instalacionId
                        val ingenieroNombre = CatalogoPersonal.ingenieros.find { it.id == visita.ingenieroCargoId }?.nombreCompleto ?: visita.ingenieroCargoId
                        val fechaFormateada = formatUtcDateMillis(visita.fechaMillis)
                        val tipoVisitaTexto = formatTipoVisita(visita.tipoVisita)

                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "OT: ${visita.numeroOt}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Cliente: $clienteNombre",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Instalación: $instalacionNombre",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Fecha: $fechaFormateada",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Tipo: $tipoVisitaTexto",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Ingeniero: $ingenieroNombre",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToNuevaVisita,
                enabled = true,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.visitas_btn_nueva_visita))
            }
        }
    }
}

private fun formatUtcDateMillis(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}

private fun formatTipoVisita(tipo: String): String {
    return when (tipo) {
        "SOLO_PRUEBAS" -> "Solo pruebas"
        "PRUEBAS_Y_MANT" -> "Pruebas y mantenimiento"
        else -> tipo
    }
}

@Preview(showBackground = true)
@Composable
fun VisitasScreenPreview() {
    InduztekTheme {
        VisitasScreen(onBackClick = {})
    }
}
