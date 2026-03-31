@file:OptIn(ExperimentalMaterial3Api::class)

package edu.nd.pmcburne.hello

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import edu.nd.pmcburne.hello.ui.theme.VirginiaGroundsMapsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val groundsViewModel: MainViewModel = viewModel()
            val interfaceState by groundsViewModel.uiState.collectAsState()

            VirginiaGroundsMapsTheme(darkModeEnabled = interfaceState.darkModeEnabled) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        GroundsAppTopBar(
                            darkModeEnabled = interfaceState.darkModeEnabled,
                            switchToggleForMode = { groundsViewModel.darkModeToggle() }
                        )
                    }
                ) { innerPadding ->
                    GroundsMapInterface(
                        interfaceState = interfaceState,
                        groundsViewModel = groundsViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun GroundsAppTopBar(
    darkModeEnabled: Boolean,
    switchToggleForMode: () -> Unit
) {
    var showSettings by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "UVA Grounds",
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            Box {
                IconButton(onClick = { showSettings = true }) {
                    Text(
                        text = "⚙",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = showSettings,
                    onDismissRequest = { showSettings = false },
                    modifier = Modifier.width(220.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Dark Mode")
                                Switch(
                                    checked = darkModeEnabled,
                                    onCheckedChange = {
                                        switchToggleForMode()
                                        showSettings = false
                                    }
                                )
                            }
                        },
                        onClick = {
                            switchToggleForMode()
                            showSettings = false
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun GroundsMapInterface(
    interfaceState: GroundsUIState,
    groundsViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val appContext = LocalContext.current
    var openTagMenu by remember { mutableStateOf(false) }

    val groundsCenter = LatLng(38.03567, -78.50365)

    val mapCameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(groundsCenter, 14.5f)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                shape = RoundedCornerShape(22.dp),
                tonalElevation = 4.dp,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = interfaceState.searchText,
                        onValueChange = groundsViewModel::updateSearchQuery,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Search locations or tags") },
                        placeholder = { Text("Ex: Newcomb Hall or bookstore") }
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { openTagMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Tag Filter: ${interfaceState.activeTag}")
                        }

                        DropdownMenu(
                            expanded = openTagMenu,
                            onDismissRequest = { openTagMenu = false },
                            modifier = Modifier.heightIn(max = 320.dp)
                        ) {
                            interfaceState.allTags.forEach { tag ->
                                DropdownMenuItem(
                                    text = { Text(tag) },
                                    onClick = {
                                        groundsViewModel.selectTag(tag)
                                        openTagMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        text = "${interfaceState.visibleLocations.size} locations shown",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = mapCameraState,
                    properties = MapProperties(
                        isMyLocationEnabled = false
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        mapToolbarEnabled = false
                    )
                ) {
                    interfaceState.visibleLocations.forEach { location ->
                        val position = LatLng(location.locationLat, location.locationLong)

                        Marker(
                            state = rememberUpdatedMarkerState(position = position),
                            title = location.locationName,
                            snippet = location.locationDetails,
                            onClick = {
                                groundsViewModel.locationChosen(location)
                                true
                            }
                        )
                    }
                }

                if (interfaceState.isSyncing && interfaceState.visibleLocations.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                interfaceState.syncError?.let { message ->
                    Surface(
                        tonalElevation = 4.dp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        interfaceState.activeLocation?.let { location ->
            ModalBottomSheet(
                onDismissRequest = { groundsViewModel.closeLocationWindow() }
            ) {
                GroundsLocationDetails(
                    location = location,
                    closeDetails = { groundsViewModel.closeLocationWindow() },
                    forLocationDirection = {
                        launchLocationDirection(appContext, location)
                    }
                )
            }
        }
    }
}

@Composable
fun GroundsLocationDetails(
    location: GroundsLocationEntity,
    closeDetails: () -> Unit,
    forLocationDirection: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location.locationName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = location.locationTags.joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(onClick = closeDetails) {
                Text("Close")
            }
        }

        Text(
            text = location.locationDetails,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Button(
            onClick = forLocationDirection,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Directions")
        }
    }
}

fun launchLocationDirection(
    context: Context,
    location: GroundsLocationEntity
) {
    val googleMapsIntent = Intent(
        Intent.ACTION_VIEW,
        "google.navigation:q=${location.locationLat},${location.locationLong}".toUri()
    ).apply {
        setPackage("com.google.android.apps.maps")
    }

    val fallbackIntent = Intent(
        Intent.ACTION_VIEW,
        "geo:${location.locationLat},${location.locationLong}?q=${location.locationLat},${location.locationLong}(${Uri.encode(location.locationName)})".toUri()
    )

    try {
        context.startActivity(googleMapsIntent)
    } catch (_: Exception) {
        context.startActivity(fallbackIntent)
    }
}