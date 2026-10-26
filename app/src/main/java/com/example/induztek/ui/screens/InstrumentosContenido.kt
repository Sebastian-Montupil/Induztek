package com.example.induztek.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induztek.data.repository.SesionRepository
import com.example.induztek.domain.validation.ValidadorPermisoInstrumentos
import com.example.induztek.ui.viewmodel.InstrumentoViewModel
import com.example.induztek.viewmodel.InstrumentoViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstrumentosContenido(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel: InstrumentoViewModel = viewModel(
        factory = InstrumentoViewModelFactory(context),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val instrumentos by viewModel.instrumentos.collectAsStateWithLifecycle()
    val usuarioActual by SesionRepository.usuarioActual.collectAsStateWithLifecycle()

    val errorPermiso = ValidadorPermisoInstrumentos.validarGestion(usuarioActual)
    val esJefeTecnico = errorPermiso == null

    var mostrarFormulario by remember { mutableStateOf(value = false) }
    var expandedTipo by remember { mutableStateOf(value = false) }

    val tiposInstrumentoList = listOf(
        "Multímetro",
        "Pinza amperimétrica",
        "Megóhmetro",
        "Telurímetro",
        "Osciloscopio",
        "Otro",
    )

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensajeError, uiState.mensajeExito) {
        val mensaje = uiState.mensajeError ?: uiState.mensajeExito
        if (mensaje != null) {
            snackbarHostState.showSnackbar(mensaje)
            viewModel.limpiarMensajes()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Gestión de instrumentos")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Módulo de instrumentos de medición y calibración (RN-12).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (esJefeTecnico) {
                item {
                    Button(
                        onClick = { mostrarFormulario = !mostrarFormulario },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = if (mostrarFormulario) "Ocultar formulario de registro" else "Registrar instrumento")
                    }
                }

                if (mostrarFormulario) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    text = "Nuevo instrumento de medición",
                                    style = MaterialTheme.typography.titleMedium,
                                )

                                OutlinedTextField(
                                    value = uiState.nombre,
                                    onValueChange = { viewModel.onNombreCambia(it) },
                                    label = { Text("Nombre del instrumento *") },
                                    isError = uiState.nombreError,
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Build, contentDescription = null)
                                    },
                                    supportingText = {
                                        if (uiState.nombreError) {
                                            Text(text = "El nombre es obligatorio")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )

                                ExposedDropdownMenuBox(
                                    expanded = expandedTipo,
                                    onExpandedChange = { expandedTipo = !expandedTipo },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    OutlinedTextField(
                                        value = uiState.tipoInstrumento,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Tipo de instrumento *") },
                                        isError = uiState.tipoInstrumentoError,
                                        leadingIcon = {
                                            Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = null)
                                        },
                                        supportingText = {
                                            if (uiState.tipoInstrumentoError) {
                                                Text(text = "Debe seleccionar un tipo")
                                            }
                                        },
                                        trailingIcon = {
                                            if (uiState.tipoInstrumentoError) {
                                                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                            } else {
                                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo)
                                            }
                                        },
                                        modifier = Modifier
                                            .menuAnchor(
                                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                                enabled = true,
                                            )
                                            .fillMaxWidth(),
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedTipo,
                                        onDismissRequest = { expandedTipo = false },
                                    ) {
                                        tiposInstrumentoList.forEach { tipo ->
                                            DropdownMenuItem(
                                                text = { Text(tipo) },
                                                onClick = {
                                                    viewModel.onTipoInstrumentoCambia(tipo)
                                                    expandedTipo = false
                                                },
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = uiState.marca,
                                    onValueChange = { viewModel.onMarcaCambia(it) },
                                    label = { Text("Marca *") },
                                    isError = uiState.marcaError,
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Star, contentDescription = null)
                                    },
                                    supportingText = {
                                        if (uiState.marcaError) {
                                            Text(text = "La marca es obligatoria")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )

                                OutlinedTextField(
                                    value = uiState.modelo,
                                    onValueChange = { viewModel.onModeloCambia(it) },
                                    label = { Text("Modelo (opcional)") },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Create, contentDescription = null)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )

                                OutlinedTextField(
                                    value = uiState.numeroSerie,
                                    onValueChange = { viewModel.onNumeroSerieCambia(it) },
                                    label = { Text("Número de serie *") },
                                    isError = uiState.numeroSerieError,
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Info, contentDescription = null)
                                    },
                                    supportingText = {
                                        if (uiState.numeroSerieError) {
                                            Text(text = "El número de serie es obligatorio")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )

                                OutlinedTextField(
                                    value = uiState.codigoInterno,
                                    onValueChange = { viewModel.onCodigoInternoCambia(it) },
                                    label = { Text("Código interno (opcional)") },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Info, contentDescription = null)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )

                                OutlinedTextField(
                                    value = uiState.fechaVencimientoCalibracion,
                                    onValueChange = { viewModel.onFechaVencimientoCalibracionCambia(it) },
                                    label = { Text("Vencimiento calibración (opcional AAAA-MM-DD)") },
                                    isError = uiState.fechaVencimientoError,
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.DateRange, contentDescription = null)
                                    },
                                    supportingText = {
                                        if (uiState.fechaVencimientoError) {
                                            Text(text = "Formato inválido (use AAAA-MM-DD)")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )

                                OutlinedTextField(
                                    value = uiState.observaciones,
                                    onValueChange = { viewModel.onObservacionesCambia(it) },
                                    label = { Text("Observaciones (opcional)") },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Create, contentDescription = null)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                                )

                                Button(
                                    onClick = { viewModel.registrarInstrumento() },
                                    enabled = !uiState.guardando,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    if (uiState.guardando) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.height(24.dp),
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                strokeWidth = 2.dp,
                                            )
                                        }
                                    } else {
                                        Text(text = "Guardar instrumento")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "Nota: Solo el Jefe técnico activo puede registrar nuevos instrumentos (RN-12).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            item {
                Text(
                    text = "Instrumentos registrados",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            if (instrumentos.isEmpty()) {
                item {
                    Text(
                        text = "Todavía no hay instrumentos de medición registrados.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(instrumentos) { instrumento ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                        ) {
                            Text(
                                text = instrumento.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tipo: ${instrumento.tipoInstrumento} | Marca: ${instrumento.marca}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "Modelo: ${instrumento.modelo.ifBlank { "N/A" }} | Serie: ${instrumento.numeroSerie}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (!instrumento.fechaVencimientoCalibracion.isNullOrBlank()) {
                                Text(
                                    text = "Vencimiento calibración: ${instrumento.fechaVencimientoCalibracion}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
