package com.example.induztek.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induztek.data.local.EquipoFotoStorage
import com.example.induztek.model.CatalogoClientes
import com.example.induztek.viewmodel.EquipoViewModel
import com.example.induztek.viewmodel.EquipoViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquiposScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onAgregarEquipo: () -> Unit = {},
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    if (selectedTabIndex == 1) {
        InstrumentosContenido(
            modifier = modifier,
        ) {
            selectedTabIndex = 0
        }
        return
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val equipoViewModel: EquipoViewModel = viewModel(
        factory = EquipoViewModelFactory(context),
    )
    val uiState by equipoViewModel.uiState.collectAsStateWithLifecycle()

    var expandedCliente by remember { mutableStateOf(value = false) }
    var expandedInstalacion by remember { mutableStateOf(value = false) }

    val fotoStorage = remember { EquipoFotoStorage(context) }
    var equipoExpandidoId by rememberSaveable { mutableStateOf<Long?>(null) }
    var equipoCapturaId by rememberSaveable { mutableStateOf<Long?>(null) }
    var procesandoFoto by remember { mutableStateOf(value = false) }

    var fotoCamaraActual by remember { mutableStateOf<Bitmap?>(null) }
    var fotoGaleriaActual by remember { mutableStateOf<Bitmap?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        val idObjetivo = equipoCapturaId
        if (((bitmap != null) && (idObjetivo != null)) && (idObjetivo > 0L)) {
            procesandoFoto = true
            coroutineScope.launch {
                try {
                    fotoStorage.guardarFotoCamara(idObjetivo, bitmap)
                    fotoCamaraActual = fotoStorage.cargarFotoCamara(idObjetivo)
                    snackbarHostState.showSnackbar("Fotografía de cámara guardada exitosamente.")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Error al guardar la foto de cámara: ${e.localizedMessage ?: "Desconocido"}")
                } finally {
                    procesandoFoto = false
                }
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        val idObjetivo = equipoCapturaId
        if (((uri != null) && (idObjetivo != null)) && (idObjetivo > 0L)) {
            procesandoFoto = true
            coroutineScope.launch {
                try {
                    fotoStorage.guardarFotoGaleria(idObjetivo, uri)
                    fotoGaleriaActual = fotoStorage.cargarFotoGaleria(idObjetivo)
                    snackbarHostState.showSnackbar("Fotografía de galería guardada exitosamente.")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Error al guardar la foto de galería: ${e.localizedMessage ?: "Desconocido"}")
                } finally {
                    procesandoFoto = false
                }
            }
        }
    }

    LaunchedEffect(equipoExpandidoId) {
        val id = equipoExpandidoId
        if ((id != null) && (id > 0L)) {
            fotoCamaraActual = fotoStorage.cargarFotoCamara(id)
            fotoGaleriaActual = fotoStorage.cargarFotoGaleria(id)
        } else {
            fotoCamaraActual = null
            fotoGaleriaActual = null
        }
    }

    val tabs = listOf("Equipos", "Instrumentos")

    LaunchedEffect(uiState.mensajeError) {
        val error = uiState.mensajeError
        if (error != null) {
            snackbarHostState.showSnackbar(error)
            equipoViewModel.limpiarMensajes()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Equipos e instrumentos")
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
                .padding(16.dp),
        ) {
            Text(
                text = "Módulo de gestión de equipos e instrumentos de medición utilizados en terreno.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title) },
                    )
                }
            }

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
                    label = { Text("Cliente") },
                    placeholder = { Text("Seleccione un cliente") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCliente) },
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

            Spacer(modifier = Modifier.height(12.dp))

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
                    label = { Text("Instalación") },
                    placeholder = { Text("Seleccione una instalación") },
                    trailingIcon = {
                        if (instalacionHabilitada) {
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if ((uiState.clienteSeleccionadoId == null) || (uiState.instalacionSeleccionadaId == null)) {
                    Text(
                        text = "Seleccione un cliente y una instalación para consultar los equipos.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else if (uiState.equipos.isEmpty()) {
                    Text(
                        text = "Todavía no hay equipos registrados en esta instalación.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.equipos) { equipo ->
                            val expandido = equipoExpandidoId == equipo.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize()
                                    .clickable {
                                        equipoExpandidoId = if (expandido) null else equipo.id
                                    },
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                ) {
                                    Text(
                                        text = equipo.nombre,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tipo: ${equipo.tipo} | Marca: ${equipo.marca}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = "Modelo: ${equipo.modelo.ifBlank { "N/A" }} | Serie: ${equipo.numeroSerie}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )

                                    if (expandido) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Button(
                                                onClick = {
                                                    equipoCapturaId = equipo.id
                                                    cameraLauncher.launch(null)
                                                },
                                                enabled = !procesandoFoto,
                                                modifier = Modifier.weight(1f),
                                            ) {
                                                Text(text = "Tomar foto")
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    equipoCapturaId = equipo.id
                                                    galleryLauncher.launch("image/*")
                                                },
                                                enabled = !procesandoFoto,
                                                modifier = Modifier.weight(1f),
                                            ) {
                                                Text(text = "Galería")
                                            }
                                        }

                                        val camBitmap = fotoCamaraActual
                                        val galBitmap = fotoGaleriaActual

                                        if ((camBitmap != null) || (galBitmap != null)) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                if (camBitmap != null) {
                                                    Column(
                                                        modifier = Modifier.weight(1f),
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                    ) {
                                                        Text(
                                                            text = "Cámara",
                                                            style = MaterialTheme.typography.labelSmall,
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Image(
                                                            bitmap = camBitmap.asImageBitmap(),
                                                            contentDescription = null,
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier
                                                                .size(100.dp)
                                                                .clip(RoundedCornerShape(8.dp)),
                                                        )
                                                    }
                                                }
                                                if (galBitmap != null) {
                                                    Column(
                                                        modifier = Modifier.weight(1f),
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                    ) {
                                                        Text(
                                                            text = "Galería",
                                                            style = MaterialTheme.typography.labelSmall,
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Image(
                                                            bitmap = galBitmap.asImageBitmap(),
                                                            contentDescription = null,
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier
                                                                .size(100.dp)
                                                                .clip(RoundedCornerShape(8.dp)),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAgregarEquipo,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Agregar equipo")
            }
        }
    }
}
