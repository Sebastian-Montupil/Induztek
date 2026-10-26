package com.example.induztek.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
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
import com.example.induztek.model.CatalogoClientes
import com.example.induztek.viewmodel.EquipoViewModel
import com.example.induztek.viewmodel.EquipoViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroEquipoScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val equipoViewModel: EquipoViewModel = viewModel(
        factory = EquipoViewModelFactory(context),
    )
    val uiState by equipoViewModel.uiState.collectAsStateWithLifecycle()

    var expandedCliente by remember { mutableStateOf(value = false) }
    var expandedInstalacion by remember { mutableStateOf(value = false) }
    var expandedTipo by remember { mutableStateOf(value = false) }

    val tiposList = listOf(
        "Interruptor",
        "Transformador",
        "Celda eléctrica",
        "Relé de protección",
        "Otro",
    )

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensajeError, uiState.mensajeExito) {
        val mensaje = uiState.mensajeError ?: uiState.mensajeExito
        if (mensaje != null) {
            snackbarHostState.showSnackbar(mensaje)
            equipoViewModel.limpiarMensajes()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Registro de equipo")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(
                text = "Ingrese los datos del nuevo equipo o instrumento de terreno.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            val clienteSeleccionado = CatalogoClientes.clientes.find { it.id == uiState.clienteSeleccionadoId }
            ExposedDropdownMenuBox(
                expanded = expandedCliente,
                onExpandedChange = { expandedCliente = !expandedCliente },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = clienteSeleccionado?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cliente *") },
                    placeholder = { Text("Seleccione un cliente") },
                    isError = uiState.clienteError,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null)
                    },
                    supportingText = {
                        if (uiState.clienteError) {
                            Text(text = "Debe seleccionar un cliente válido")
                        }
                    },
                    trailingIcon = {
                        if (uiState.clienteError) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCliente)
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
                    expanded = expandedCliente,
                    onDismissRequest = { expandedCliente = false },
                ) {
                    CatalogoClientes.clientes.forEach { cliente ->
                        DropdownMenuItem(
                            text = { Text(cliente.nombre) },
                            onClick = {
                                equipoViewModel.onClienteSeleccionado(cliente.id)
                                expandedCliente = false
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val instalacionesDisponibles = CatalogoClientes.getInstalacionesPorCliente(uiState.clienteSeleccionadoId)
            val instalacionHabilitada = ((uiState.clienteSeleccionadoId != null) && instalacionesDisponibles.isNotEmpty())
            val instalacionSeleccionada = instalacionesDisponibles.find { it.id == uiState.instalacionSeleccionadaId }

            ExposedDropdownMenuBox(
                expanded = expandedInstalacion && instalacionHabilitada,
                onExpandedChange = {
                    if (instalacionHabilitada) {
                        expandedInstalacion = !expandedInstalacion
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = instalacionSeleccionada?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    enabled = instalacionHabilitada,
                    label = { Text("Instalación *") },
                    placeholder = { Text("Seleccione una instalación") },
                    isError = uiState.instalacionError,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Place, contentDescription = null)
                    },
                    supportingText = {
                        if (uiState.instalacionError) {
                            Text(text = "Debe seleccionar una instalación válida")
                        }
                    },
                    trailingIcon = {
                        if (uiState.instalacionError) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        } else if (instalacionHabilitada) {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInstalacion)
                        }
                    },
                    modifier = Modifier
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = instalacionHabilitada,
                        )
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = expandedInstalacion && instalacionHabilitada,
                    onDismissRequest = { expandedInstalacion = false },
                ) {
                    instalacionesDisponibles.forEach { instalacion ->
                        DropdownMenuItem(
                            text = { Text(instalacion.nombre) },
                            onClick = {
                                equipoViewModel.onInstalacionSeleccionada(instalacion.id)
                                expandedInstalacion = false
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = { equipoViewModel.onNombreCambia(it) },
                label = { Text("Nombre del equipo *") },
                isError = uiState.nombreError,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Build, contentDescription = null)
                },
                supportingText = {
                    if (uiState.nombreError) {
                        Text(text = "El nombre del equipo es obligatorio")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = expandedTipo,
                onExpandedChange = { expandedTipo = !expandedTipo },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = uiState.tipo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de equipo *") },
                    isError = uiState.tipoError,
                    leadingIcon = {
                        Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = null)
                    },
                    supportingText = {
                        if (uiState.tipoError) {
                            Text(text = "Debe seleccionar un tipo de equipo")
                        }
                    },
                    trailingIcon = {
                        if (uiState.tipoError) {
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
                    tiposList.forEach { seleccion ->
                        DropdownMenuItem(
                            text = { Text(seleccion) },
                            onClick = {
                                equipoViewModel.onTipoCambia(seleccion)
                                expandedTipo = false
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.marca,
                onValueChange = { equipoViewModel.onMarcaCambia(it) },
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

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.modelo,
                onValueChange = { equipoViewModel.onModeloCambia(it) },
                label = { Text("Modelo (opcional)") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Create, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.numeroSerie,
                onValueChange = { equipoViewModel.onNumeroSerieCambia(it) },
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

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.codigoInterno,
                onValueChange = { equipoViewModel.onCodigoInternoCambia(it) },
                label = { Text("Código interno (opcional)") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.observaciones,
                onValueChange = { equipoViewModel.onObservacionesCambia(it) },
                label = { Text("Observaciones (opcional)") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Create, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { equipoViewModel.registrarEquipo() },
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
                    Text(text = "Guardar equipo")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
