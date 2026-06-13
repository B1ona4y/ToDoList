package com.example.todolist.ui.task.components

import android.location.Geocoder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPicker(
    locationName: String,
    latitude: Double?,
    longitude: Double?,
    onLocationSelected: (name: String, lat: Double, lng: Double) -> Unit,
    onLocationCleared: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hasLocation = latitude != null && longitude != null

    Column(modifier = modifier) {
        // Current location display
        if (hasLocation) {
            Surface(
                color  = MaterialTheme.colorScheme.secondaryContainer,
                shape  = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text  = locationName.ifBlank { "Selected location" },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text  = "%.5f, %.5f".format(latitude, longitude),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onLocationCleared) {
                        Icon(Icons.Default.Close, contentDescription = "Clear location")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        OutlinedButton(
            onClick  = { showSheet = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Map, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (hasLocation) "Change location" else "Pick location on map")
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState       = sheetState
        ) {
            MapLocationPicker(
                initialLat  = latitude,
                initialLng  = longitude,
                initialName = locationName,
                onConfirm   = { name, lat, lng ->
                    onLocationSelected(name, lat, lng)
                    showSheet = false
                },
                onDismiss   = { showSheet = false }
            )
        }
    }
}

@Composable
private fun MapLocationPicker(
    initialLat: Double?,
    initialLng: Double?,
    initialName: String,
    onConfirm: (String, Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    val defaultLatLng = LatLng(52.2297, 21.0122) // Warsaw as default
    val initialLatLng = if (initialLat != null && initialLng != null)
        LatLng(initialLat, initialLng) else null

    var selectedLatLng by remember { mutableStateOf(initialLatLng) }
    var selectedName   by remember { mutableStateOf(initialName) }
    var searchQuery    by remember { mutableStateOf(initialName) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            selectedLatLng ?: defaultLatLng,
            if (selectedLatLng != null) 14f else 6f
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        // Search bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { searchQuery = it },
                label         = { Text("Search address") },
                singleLine    = true,
                modifier      = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    scope.launch {
                        Geocoder(context).getFromLocationName(searchQuery, 1) { addresses ->
                            addresses.firstOrNull()?.let { address ->
                                val latLng = LatLng(address.latitude, address.longitude)
                                selectedLatLng = latLng
                                selectedName   = address.getAddressLine(0) ?: searchQuery
                                scope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                                    )
                                }
                            }
                        }
                    }
                }
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }

        // Map
        GoogleMap(
            modifier             = Modifier
                .fillMaxWidth()
                .height(360.dp),
            cameraPositionState  = cameraPositionState,
            onMapClick           = { latLng ->
                selectedLatLng = latLng
                Geocoder(context).getFromLocation(
                    latLng.latitude, latLng.longitude, 1
                ) { addresses ->
                    selectedName = addresses.firstOrNull()
                        ?.getAddressLine(0)
                        ?: "%.5f, %.5f".format(latLng.latitude, latLng.longitude)
                    searchQuery  = selectedName
                }
            }
        ) {
            selectedLatLng?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = selectedName.ifBlank { null }
                )
            }
        }

        // Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick  = onDismiss,
                modifier = Modifier.weight(1f)
            ) { Text("Cancel") }

            Button(
                onClick  = {
                    selectedLatLng?.let { latLng ->
                        onConfirm(selectedName, latLng.latitude, latLng.longitude)
                    }
                },
                enabled  = selectedLatLng != null,
                modifier = Modifier.weight(1f)
            ) { Text("Use this location") }
        }

        Spacer(Modifier.height(8.dp))
    }
}
