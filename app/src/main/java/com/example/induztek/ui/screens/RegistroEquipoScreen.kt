package com.example.induztek.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.induztek.ui.theme.InduztekTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroEquipoScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    var nombre by rememberSaveable { mutableStateOf(value = "") }
    var tipo by rememberSaveable { mutableStateOf(value = "") }
    var marca by rememberSaveable { mutableStateOf(value = "") }
    var modelo by rememberSaveable { mutableStateOf(value = "") }
    var numeroSerie by rememberSaveable { mutableStateOf(value = "") }
    var codigoInterno by rememberSaveable { mutableStateOf(value = "") }
    var observaciones by rememberSaveable { mutableStateOf(value = "") }

    var hasValidated by remember { mutableStateOf(value = false) }
    var expanded by remember { mutableStateOf(value = false) }

    val tiposList = listOf(
        "Interruptor",
        "Transformador",
        "Celda eléctrica",
        "Relé de protección",
        "Otro",
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val isNombreError = hasValidated && nombre.isBlank()
    val isTipoError = hasValidated && tipo.isBlank()
    val isMarcaError = hasValidated && marca.isBlank()
    val isNumeroSerieError = hasValidated && numeroSerie.isBlank()

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

            // Nombre del equipo
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del equipo *") },
                isError = isNombreError,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            if (isNombreError) {
                Text(
                    text = "El nombre del equipo es obligatorio",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tipo de equipo (Dropdown)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = tipo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de equipo *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    isError = isTipoError,
                    modifier = Modifier
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true,
                        )
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    tiposList.forEach { seleccion ->
                        DropdownMenuItem(
                            text = { Text(seleccion) },
                            onClick = {
                                tipo = seleccion
                                expanded = false
                            },
                        )
                    }
                }
            }
            if (isTipoError) {
                Text(
                    text = "Debe seleccionar un tipo de equipo",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Marca
            OutlinedTextField(
                value = marca,
                onValueChange = { marca = it },
                label = { Text("Marca *") },
                isError = isMarcaError,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            if (isMarcaError) {
                Text(
                    text = "La marca es obligatoria",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Modelo (opcional)
            OutlinedTextField(
                value = modelo,
                onValueChange = { modelo = it },
                label = { Text("Modelo (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Número de serie
            OutlinedTextField(
                value = numeroSerie,
                onValueChange = { numeroSerie = it },
                label = { Text("Número de serie *") },
                isError = isNumeroSerieError,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            if (isNumeroSerieError) {
                Text(
                    text = "El número de serie es obligatorio",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Código interno (opcional)
            OutlinedTextField(
                value = codigoInterno,
                onValueChange = { codigoInterno = it },
                label = { Text("Código interno (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Observaciones (opcional)
            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Validar formulario
            Button(
                onClick = {
                    hasValidated = true
                    val isValid = nombre.isNotBlank() &&
                        tipo.isNotBlank() &&
                        marca.isNotBlank() &&
                        numeroSerie.isNotBlank()

                    if (isValid) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                "Formulario válido. El guardado estará disponible próximamente",
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Validar formulario")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistroEquipoScreenPreview() {
    InduztekTheme {
        RegistroEquipoScreen()
    }
}
