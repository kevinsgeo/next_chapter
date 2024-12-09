package com.cs407.next_chapter

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(navController: NavHostController) {
    // Define the required permissions
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Request permissions on first launch
    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
    }

    Scaffold(
        bottomBar = { NavigationBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (permissionsState.allPermissionsGranted) {
                // Obtain the current context
                val context = LocalContext.current

                // State for the address input
                var address by remember { mutableStateOf("") }

                // Mutable list of markers, initialized with hardcoded markers
                val markers = remember {
                    mutableStateListOf(
                        LatLng(37.779, -122.419), // Marker 1: Near center
                        LatLng(37.784, -122.409), // Marker 2: Northeast of center
                        LatLng(37.764, -122.429), // Marker 3: Southwest of center
                        LatLng(37.774, -122.435), // Marker 4: West of center
                        LatLng(37.789, -122.425)  // Marker 5: North of center
                    )
                }

                // Define the camera position (e.g., San Francisco)
                val centerLocation = LatLng(37.7749, -122.4194)
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(centerLocation, 12f)
                }

                // Coroutine scope for asynchronous operations
                val coroutineScope = rememberCoroutineScope()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    // Address Input TextField
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Enter Address") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    // Button to Add Marker
                    Button(
                        onClick = {
                            if (address.isNotBlank()) {
                                coroutineScope.launch {
                                    addMarkerFromAddress(context, address, markers, cameraPositionState)
                                    address = "" // Clear the input after adding
                                }
                            } else {
                                Toast.makeText(context, "Please enter an address", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text("Add Marker")
                    }

                    // Spacer to separate the input section from the map
                    Spacer(modifier = Modifier.height(8.dp))

                    // Google Map
                    GoogleMap(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = true),
                        uiSettings = MapUiSettings(
                            myLocationButtonEnabled = true,
                            zoomControlsEnabled = false
                        )
                    ) {
                        // Add markers to the map
                        markers.forEach { location ->
                            Marker(
                                state = MarkerState(position = location),
                                title = "Forty rules of love",
                                snippet = "John Doe"
                            )
                        }
                    }
                }
            } else {
                // Inform the user that permissions are required
                Text(
                    text = "Location permissions are required to display the map.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

/**
 * Function to geocode an address and add a marker to the map.
 *
 * @param context The current context.
 * @param address The address string to geocode.
 * @param markers The mutable list of markers to update.
 * @param cameraPositionState The camera position state to update the camera view.
 */
suspend fun addMarkerFromAddress(
    context: Context,
    address: String,
    markers: MutableList<LatLng>,
    cameraPositionState: CameraPositionState
) {
    val geocoder = Geocoder(context, Locale.getDefault())
    try {
        // Perform the geocoding in the IO dispatcher
        val addresses: List<Address>? = withContext(Dispatchers.IO) {
            geocoder.getFromLocationName(address, 1)
        }

        if (addresses != null && addresses.isNotEmpty()) {
            val location = addresses[0]
            val latLng = LatLng(location.latitude, location.longitude)

            // Add marker to the list
            markers.add(latLng)

            // Move and zoom the camera to the new marker
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                durationMs = 1000
            )
        } else {
            Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
        }
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Geocoder failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
