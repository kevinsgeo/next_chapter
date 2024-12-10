import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

data class BookMarkerData(
    val bookTitle: String?,
    val userName: String?,
    val location: LatLng,
    val bookImageUrl: String?,
    val isbn: String?
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    navController: NavHostController,
    firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    database: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
    }

    val bookMarkers = remember { mutableStateListOf<BookMarkerData>() }
    val context = LocalContext.current

    val centerLocation = LatLng(37.7749, -122.4194)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(centerLocation, 12f)
    }

    val coroutineScope = rememberCoroutineScope()

    // State to manage the currently selected marker (for dialog)
    var selectedMarker by rememberSaveable { mutableStateOf<BookMarkerData?>(null) }

    // Load all books from the database once
    LaunchedEffect(Unit) {
        val booksRef = database.getReference("Books")
        booksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot.children
                if (!children.any()) {
                    Toast.makeText(context, "No books found in the database", Toast.LENGTH_SHORT).show()
                    return
                }

                bookMarkers.clear()

                coroutineScope.launch {
                    for (bookSnapshot in children) {
                        val bookData = bookSnapshot.value as? Map<*, *>
                        if (bookData != null) {
                            val uidValue = bookData["uid"]
                            val uids = when (uidValue) {
                                is List<*> -> uidValue.filterIsInstance<String>()
                                is String -> listOf(uidValue)
                                else -> emptyList()
                            }

                            val title = bookData["title"] as? String
                            val imageUrl = bookData["bookImageUrl"] as? String
                            val isbn = bookData["isbn"] as? String

                            // For each UID (each user who owns the book), create a marker
                            for (uid in uids) {
                                val userRef = database.getReference("Users").child(uid)
                                val userSnapshot = withContext(Dispatchers.IO) {
                                    userRef.get().await()
                                }

                                val userMap = userSnapshot.value as? Map<*, *>
                                if (userMap != null) {
                                    val userName = userMap["name"] as? String
                                    val homeAddress = userMap["homeAddress"] as? String
                                    if (!homeAddress.isNullOrBlank() && !imageUrl.isNullOrBlank()) {
                                        val latLng = geocodeAddress(context, homeAddress)
                                        if (latLng != null) {
                                            bookMarkers.add(
                                                BookMarkerData(
                                                    bookTitle = title,
                                                    userName = userName,
                                                    location = latLng,
                                                    bookImageUrl = imageUrl,
                                                    isbn = isbn
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (bookMarkers.isNotEmpty()) {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(bookMarkers.first().location, 12f),
                            durationMs = 1000
                        )
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load books: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Scaffold(
        bottomBar = {
            com.cs407.next_chapter.NavigationBar(navController = navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (permissionsState.allPermissionsGranted) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top
                ) {
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
                        bookMarkers.forEach { markerData ->
                            Marker(
                                state = MarkerState(position = markerData.location),
                                title = markerData.bookTitle ?: "No Title",
                                snippet = markerData.userName ?: "No user name",
                                onClick = {
                                    selectedMarker = markerData
                                    false // return false to show info window
                                }
                            )
                        }
                    }
                }

                // Show a dialog when a marker is selected
                selectedMarker?.let { marker ->
                    AlertDialog(
                        onDismissRequest = { selectedMarker = null },
                        confirmButton = {
                            TextButton(onClick = { selectedMarker = null }) {
                                Text("Close")
                            }
                        },
                        dismissButton = {
                            // Add a button to navigate to the BookInfoOnClick page
                            marker.isbn?.let { isbn ->
                                TextButton(onClick = {
                                    // Navigate to BookInfoOnClick page with the ISBN
                                    navController.navigate("book_info/$isbn")
                                    selectedMarker = null
                                }) {
                                    Text("View More Info")
                                }
                            }
                        },
                        title = {
                            Text(marker.bookTitle ?: "No Title")
                        },
                        text = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Display the book image
                                marker.bookImageUrl?.let { imageUrl ->
                                    Image(
                                        painter = rememberAsyncImagePainter(imageUrl),
                                        contentDescription = "Book Cover",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .padding(bottom = 8.dp)
                                    )
                                }

                                // Display the user's name
                                Text(
                                    text = marker.userName ?: "No user name",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    )
                }

            } else {
                Text(
                    text = "Location permissions are required to display the map.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

suspend fun geocodeAddress(
    context: Context,
    address: String
): LatLng? {
    val geocoder = Geocoder(context, Locale.getDefault())
    return withContext(Dispatchers.IO) {
        try {
            val addresses: List<Address>? = geocoder.getFromLocationName(address, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val location = addresses[0]
                LatLng(location.latitude, location.longitude)
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}

/**
 * Extension function to await a Firebase Database Reference get call.
 */
suspend fun DatabaseReference.get(): DataSnapshot {
    return suspendCancellableCoroutine { continuation ->
        this@get.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                continuation.resume(snapshot, null)
            }
            override fun onCancelled(error: DatabaseError) {
                continuation.cancel(error.toException())
            }
        })
    }
}
