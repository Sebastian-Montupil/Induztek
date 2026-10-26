package com.example.induztek.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.example.induztek.model.TipoVisita
import com.example.induztek.ui.theme.InduztekTheme
import com.example.induztek.viewmodel.NuevaVisitaViewModel
import com.example.induztek.viewmodel.NuevaVisitaViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaVisitaScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: NuevaVisitaViewModel = viewModel(
        factory = NuevaVisitaViewModelFactory(
            VisitaRepository(DatabaseProvider.getDatabase(context).visitaDao())
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var expandedCliente by rememberSaveable { mutableStateOf(false) }
    var expandedInstalacion by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.nueva_visita_top_bar_title))
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.nueva_visita_back_button_content_description)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            val clienteSeleccionado = CatalogoClientes.clientes.find { it.id == uiState.clienteSeleccionadoId }
            ExposedDropdownMenuBox(
                expanded = expandedCliente,
                onExpandedChange = { expandedCliente = !expandedCliente },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = clienteSeleccionado?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(id = R.string.nueva_visita_cliente_label)) },
                    placeholder = { Text(stringResource(id = R.string.nueva_visita_cliente_placeholder)) },
                    isError = uiState.clienteError,
                    supportingText = {
                        if (uiState.clienteError) {
                            Text(stringResource(id = R.string.error_cliente_requerido))
                        }
                    },
                    trailingIcon = {
                        if (uiState.clienteError) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCliente)
                        }
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedCliente,
                    onDismissRequest = { expandedCliente = false }
                ) {
                    CatalogoClientes.clientes.forEach { cliente ->
                        DropdownMenuItem(
                            text = { Text(cliente.nombre) },
                            onClick = {
                                viewModel.onClienteSeleccionado(cliente.id)
                                expandedCliente = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val instalacionesDisponibles = CatalogoClientes.getInstalacionesPorCliente(uiState.clienteSeleccionadoId)
            val instalacionHabilitada = uiState.clienteSeleccionadoId != null && instalacionesDisponibles.isNotEmpty()
            val instalacionSeleccionada = instalacionesDisponibles.find { it.id == uiState.instalacionSeleccionadaId }

            ExposedDropdownMenuBox(
                expanded = expandedInstalacion && instalacionHabilitada,
                onExpandedChange = {
                    if (instalacionHabilitada) {
                        expandedInstalacion = !expandedInstalacion
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = instalacionSeleccionada?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    enabled = instalacionHabilitada,
                    label = { Text(stringResource(id = R.string.nueva_visita_instalacion_label)) },
                    placeholder = { Text(stringResource(id = R.string.nueva_visita_instalacion_placeholder)) },
                    isError = uiState.instalacionError,
                    supportingText = {
                        if (uiState.instalacionError) {
                            Text(stringResource(id = R.string.error_instalacion_requerida))
                        }
                    },
                    trailingIcon = {
                        if (uiState.instalacionError) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInstalacion && instalacionHabilitada)
                        }
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )

                if (instalacionHabilitada) {
                    ExposedDropdownMenu(
                        expanded = expandedInstalacion,
                        onDismissRequest = { expandedInstalacion = false }
                    ) {
                        instalacionesDisponibles.forEach { instalacion ->
                            DropdownMenuItem(
                                text = { Text(instalacion.nombre) },
                                onClick = {
                                    viewModel.onInstalacionSeleccionada(instalacion.id)
                                    expandedInstalacion = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = formatUtcDateMillis(uiState.fechaSelectedMillis),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(id = R.string.nueva_visita_fecha_label)) },
                placeholder = { Text(stringResource(id = R.string.nueva_visita_fecha_placeholder)) },
                isError = uiState.fechaError,
                supportingText = {
                    if (uiState.fechaError) {
                        Text(stringResource(id = R.string.error_fecha_requerida))
                    }
                },
                trailingIcon = {
                    if (uiState.fechaError) {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = stringResource(id = R.string.nueva_visita_fecha_label),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = stringResource(id = R.string.nueva_visita_fecha_label)
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.numeroOt,
                onValueChange = { viewModel.onNumeroOtCambia(it) },
                label = { Text(stringResource(id = R.string.nueva_visita_numero_ot_label)) },
                placeholder = { Text(stringResource(id = R.string.nueva_visita_numero_ot_placeholder)) },
                isError = uiState.numeroOtError || uiState.errorPersistencia != null,
                supportingText = {
                    if (uiState.numeroOtError) {
                        Text(stringResource(id = R.string.error_numero_ot_requerido))
                    } else if (uiState.errorPersistencia != null) {
                        Text(uiState.errorPersistencia ?: "", color = MaterialTheme.colorScheme.error)
                    }
                },
                trailingIcon = {
                    if (uiState.numeroOtError || uiState.errorPersistencia != null) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(id = R.string.nueva_visita_tipo_visita_label),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (uiState.tipoVisitaError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                    )
                    if (uiState.tipoVisitaError) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onTipoVisitaCambia(TipoVisita.SOLO_PRUEBAS) }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = (uiState.tipoVisita == TipoVisita.SOLO_PRUEBAS),
                        onClick = { viewModel.onTipoVisitaCambia(TipoVisita.SOLO_PRUEBAS) }
                    )
                    Text(
                        text = stringResource(id = R.string.tipo_visita_solo_pruebas),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onTipoVisitaCambia(TipoVisita.PRUEBAS_Y_MANT) }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = (uiState.tipoVisita == TipoVisita.PRUEBAS_Y_MANT),
                        onClick = { viewModel.onTipoVisitaCambia(TipoVisita.PRUEBAS_Y_MANT) }
                    )
                    Text(
                        text = stringResource(id = R.string.tipo_visita_pruebas_y_mant),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                AnimatedVisibility(
                    visible = uiState.tipoVisitaError,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        text = stringResource(id = R.string.error_tipo_visita_requerido),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(id = R.string.nueva_visita_ingeniero_label),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (uiState.ingenieroError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                    )
                    if (uiState.ingenieroError) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                CatalogoPersonal.ingenieros.forEach { ingeniero ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onIngenieroCargoCambia(ingeniero.id) }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = (uiState.ingenieroCargoId == ingeniero.id),
                            onClick = { viewModel.onIngenieroCargoCambia(ingeniero.id) }
                        )
                        Text(
                            text = ingeniero.nombreCompleto,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                if (uiState.ingenieroError) {
                    Text(
                        text = stringResource(id = R.string.error_ingeniero_requerido),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.nueva_visita_tecnicos_label),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                CatalogoPersonal.tecnicos.forEach { tecnico ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onTecnicoToggle(tecnico.id) }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = uiState.tecnicosSeleccionadosIds.contains(tecnico.id),
                            onCheckedChange = { viewModel.onTecnicoToggle(tecnico.id) }
                        )
                        Text(
                            text = tecnico.nombreCompleto,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.observaciones,
                onValueChange = { viewModel.onObservacionesCambia(it) },
                label = { Text(stringResource(id = R.string.nueva_visita_observaciones_label)) },
                placeholder = { Text(stringResource(id = R.string.nueva_visita_observaciones_placeholder)) },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.validarDatos() },
                enabled = !uiState.isSaving && !uiState.hasSuccessfullySaved,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Guardando...")
                } else {
                    Text(text = stringResource(id = R.string.nueva_visita_btn_validar))
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.fechaSelectedMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        viewModel.onFechaSeleccionada(selectedMillis)
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(id = R.string.date_picker_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(id = R.string.date_picker_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (uiState.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.cerrarDialogoExito() },
            title = { Text(text = "Éxito") },
            text = { Text(text = "Visita guardada correctamente") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cerrarDialogoExito()
                    }
                ) {
                    Text(text = stringResource(id = R.string.dialog_accept))
                }
            }
        )
    }
}

private fun formatUtcDateMillis(millis: Long?): String {
    if (millis == null) return ""
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}

@Preview(showBackground = true)
@Composable
fun NuevaVisitaScreenPreview() {
    InduztekTheme {
        NuevaVisitaScreen(onBackClick = {})
    }
}
