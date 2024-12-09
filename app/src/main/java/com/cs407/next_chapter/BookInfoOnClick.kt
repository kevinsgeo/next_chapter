package com.cs407.next_chapter

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.cs407.next_chapter.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookInfoOnClick(navController: NavHostController, scannedISBN: String?) {
    val bookDetails = remember { mutableStateOf("Loading...") }
    val bookImageUrl = remember { mutableStateOf<String?>(null) }
    val bookDescription = remember { mutableStateOf("Loading description...") }
    val context = LocalContext.current

    LaunchedEffect(scannedISBN) {
        if (scannedISBN != null) {
            val apiKey = getGoogleApiKey(context)
            if (apiKey != null) {
                val (info, imageUrl, description) = fetchBookDetails(scannedISBN, apiKey)
                bookDetails.value = info ?: "No details found for ISBN: $scannedISBN"
                bookImageUrl.value = imageUrl
                bookDescription.value = description ?: "No description available."
            } else {
                bookDetails.value = "API Key not found"
                bookDescription.value = "Unable to fetch description without API key."
            }
        } else {
            bookDetails.value = "No ISBN provided"
            bookDescription.value = "No description available."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Information") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Navigate back to HomeScreen
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()) // Make content scrollable
                ) {
                    // Display the scanned ISBN
                    Text(
                        text = "Scanned ISBN: ${scannedISBN ?: "Unknown"}",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Display the book's image if available
                    bookImageUrl.value?.let { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = imageUrl,
                                error = painterResource(id = R.drawable.placeholder_image) // Placeholder image resource in case of error
                            ),
                            contentDescription = "Book Cover",
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .aspectRatio(1f)
                                .padding(bottom = 16.dp)
                        )
                    } ?: Text( // Display a message if no image is available
                        text = "No book cover available",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Display the book's details
                    Text(
                        text = bookDetails.value,
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Display the book's description
                    Text(
                        text = bookDescription.value,
                        color = Color.DarkGray,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }
            }
        }
    )
}

/**
 * Fetches book details from the Google Books API.
 *
 * @param isbn The ISBN of the book to fetch.
 * @param apiKey Your Google Books API key.
 * @return Triple containing book information, image URL, and description.
 */
suspend fun fetchBookDetails(isbn: String, apiKey: String): Triple<String?, String?, String?> {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val url = "https://www.googleapis.com/books/v1/volumes?q=isbn:$isbn&key=$apiKey"
            val request = Request.Builder().url(url).build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonResponse = JSONObject(response.body?.string() ?: "")
                val items = jsonResponse.optJSONArray("items") ?: return@withContext Triple(null, null, null)
                val volumeInfo = items.getJSONObject(0).getJSONObject("volumeInfo")

                val title = volumeInfo.optString("title", "Unknown Title")
                val authorsArray = volumeInfo.optJSONArray("authors")
                val authors = if (authorsArray != null) {
                    // Convert JSONArray to a comma-separated string
                    (0 until authorsArray.length()).map { authorsArray.getString(it) }.joinToString(", ")
                } else {
                    "Unknown Author"
                }

                val imageLinks = volumeInfo.optJSONObject("imageLinks")
                var thumbnailUrl = imageLinks?.optString("thumbnail")

                // Fix the thumbnail URL (convert http to https if needed)
                if (thumbnailUrl != null && thumbnailUrl.startsWith("http://")) {
                    thumbnailUrl = thumbnailUrl.replace("http://", "https://")
                }

                // Extract the description
                val description = volumeInfo.optString("description", "No description available.")

                Triple("Title: $title\nAuthors: $authors", thumbnailUrl, description)
            } else {
                Triple(null, null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Triple(null, null, null)
        }
    }
}

/**
 * Retrieves the Google API Key from the app's meta-data.
 *
 * @param context The current context.
 * @return The Google API Key as a String, or null if not found.
 */
fun getGoogleApiKey(context: android.content.Context): String? {
    return try {
        val applicationInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            android.content.pm.PackageManager.GET_META_DATA
        )
        val bundle = applicationInfo.metaData
        bundle?.getString("com.google.android.geo.API_KEY")
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
