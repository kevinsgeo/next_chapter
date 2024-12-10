//import android.util.Log
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavHostController
//import coil.compose.rememberAsyncImagePainter
//import com.cs407.next_chapter.R
//import com.google.firebase.auth.FirebaseAuth
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import org.json.JSONObject
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun BookInfoScreen(navController: NavHostController,
//                   scannedISBN: String?, onAdd: () -> Unit = {}
//) {
//    val bookDetails = remember { mutableStateOf("Loading...") }
//    val bookImageUrl = remember { mutableStateOf<String?>(null) }
//    val bookDescription = remember { mutableStateOf("Loading description...") }
//    val context = LocalContext.current
//
//    LaunchedEffect(scannedISBN) {
//        if (scannedISBN != null) {
//            val apiKey = getGoogleApiKey(context)
//            if (apiKey != null) {
//                val (info, imageUrl, description) = fetchBookDetails(scannedISBN, apiKey)
//                bookDetails.value = info ?: "No details found for ISBN: $scannedISBN"
//                bookImageUrl.value = imageUrl
//                bookDescription.value = description ?: "No description available."
//            } else {
//                bookDetails.value = "API Key not found"
//                bookDescription.value = "Unable to fetch description without API key."
//            }
//        } else {
//            bookDetails.value = "No ISBN provided"
//            bookDescription.value = "No description available."
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Book Information") },
//                navigationIcon = {
//                    IconButton(onClick = { navController.navigate("home") }) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowBack,
//                            contentDescription = "Back"
//                        )
//                    }
//                }
//            )
//        },
//        content = { innerPadding ->
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(innerPadding),
//                contentAlignment = Alignment.TopCenter
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    modifier = Modifier
//                        .padding(16.dp)
//                        .verticalScroll(rememberScrollState()) // Make content scrollable
//                ) {
//                    // Display the scanned ISBN
//                    Text(
//                        text = "ISBN: ${scannedISBN ?: "Unknown"}",
//                        color = Color.Black,
//                        style = MaterialTheme.typography.titleLarge,
//                        modifier = Modifier.padding(bottom = 16.dp)
//                    )
//
//                    // Display the book's image if available
//                    bookImageUrl.value?.let { imageUrl ->
//                        Image(
//                            painter = rememberAsyncImagePainter(
//                                model = imageUrl,
//                                error = painterResource(id = R.drawable.placeholder_image) // Placeholder image resource in case of error
//                            ),
//                            contentDescription = "Book Cover",
//                            modifier = Modifier
//                                .fillMaxWidth(0.5f)
//                                .aspectRatio(1f)
//                                .padding(bottom = 16.dp)
//                        )
//                    } ?: Text( // Display a message if no image is available
//                        text = "No book cover available",
//                        color = Color.Gray,
//                        style = MaterialTheme.typography.bodyLarge,
//                        modifier = Modifier.padding(bottom = 16.dp)
//                    )
//
//                    // Display the book's details
//                    Text(
//                        text = bookDetails.value,
//                        color = Color.Black,
//                        style = MaterialTheme.typography.bodyLarge,
//                        modifier = Modifier.padding(bottom = 16.dp)
//                    )
//
//                    // Display the book's description
//                    Text(
//                        text = bookDescription.value,
//                        color = Color.DarkGray,
//                        style = MaterialTheme.typography.bodyMedium,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(bottom = 16.dp)
//                    )
//
//                    // Add Book Button
//                    Button(
//                        onClick = {
//                            val uid = FirebaseAuth.getInstance().currentUser?.uid
//                            if (uid != null) {
//                                addBookDetails(
//                                    bookDetails = bookDetails.value,
//                                    bookImageUrl = bookImageUrl.value,
//                                    uid = uid,
//                                    scannedISBN = scannedISBN,
//                                    navController = navController
//                                )
//                            } else {
//                                Log.e("BookInfoScreen", "User is not authenticated")
//                            }
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 8.dp)
//                    ) {
//                        Text("Add Book")
//                    }
//
//                    // Retake Button
//                    Button(
//                        onClick = { navController.navigate("scan_isbn") },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 8.dp)
//                    ) {
//                        Text("Retake")
//                    }
//                }
//            }
//        }
//    )
//}
//
//// Function to add book details to the Firebase Realtime Database
//fun addBookDetails(
//    bookDetails: String,
//    bookImageUrl: String?,
//    uid: String,
//    scannedISBN: String?,
//    navController: NavHostController
//) {
//    if (bookDetails.isNotEmpty() && scannedISBN != null) {
//        // Extract title and authors from bookDetails
//        val title = bookDetails.substringAfter("Title: ").substringBefore("\nAuthors:")
//        val authors = bookDetails.substringAfter("Authors: ").split(", ")
//
//        // Reference the Firebase Realtime Database
//        val databaseRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("Books")
//
//        // Check if the book with the same ISBN already exists
//        databaseRef.child(scannedISBN).get().addOnSuccessListener { snapshot ->
//            if (snapshot.exists()) {
//                // If the book exists, update the uid array
//                val existingUids = snapshot.child("uid").value as? MutableList<String> ?: mutableListOf()
//                if (!existingUids.contains(uid)) {
//                    existingUids.add(uid)
//                    databaseRef.child(scannedISBN).child("uid").setValue(existingUids)
//                        .addOnSuccessListener {
//                            Log.d("BookInfoScreen", "User added to existing book's uid array successfully!")
//                            navController.navigate("home")
//                        }
//                        .addOnFailureListener { error ->
//                            Log.e("BookInfoScreen", "Failed to update uid array: ${error.message}")
//                        }
//                } else {
//                    Log.d("BookInfoScreen", "User already exists in the uid array for this book.")
//                    navController.navigate("home")
//                }
//            } else {
//                // If the book doesn't exist, create a new entry
//                val bookData = mapOf(
//                    "title" to title,
//                    "authors" to authors,
//                    "bookImageUrl" to (bookImageUrl ?: "No image available"),
//                    "uid" to listOf(uid), // Initialize uid as a list
//                    "isbn" to scannedISBN
//                )
//
//                databaseRef.child(scannedISBN).setValue(bookData)
//                    .addOnSuccessListener {
//                        Log.d("BookInfoScreen", "New book added successfully!")
//                        navController.navigate("home")
//                    }
//                    .addOnFailureListener { error ->
//                        Log.e("BookInfoScreen", "Failed to add new book: ${error.message}")
//                    }
//            }
//        }.addOnFailureListener { error ->
//            Log.e("BookInfoScreen", "Failed to check if book exists: ${error.message}")
//        }
//    } else {
//        Log.e("BookInfoScreen", "Book details are empty or ISBN is null, cannot add to database")
//    }
//}
//
//suspend fun fetchBookDetails(isbn: String, apiKey: String): Triple<String?, String?, String?> {
//    return withContext(Dispatchers.IO) {
//        try {
//            val client = OkHttpClient()
//            val url = "https://www.googleapis.com/books/v1/volumes?q=isbn:$isbn&key=$apiKey"
//            val request = Request.Builder().url(url).build()
//
//            val response = client.newCall(request).execute()
//            if (response.isSuccessful) {
//                val jsonResponse = JSONObject(response.body?.string() ?: "")
//                val items = jsonResponse.optJSONArray("items") ?: return@withContext Triple(null, null, null)
//                val volumeInfo = items.getJSONObject(0).getJSONObject("volumeInfo")
//
//                val title = volumeInfo.optString("title", "Unknown Title")
//                val authorsArray = volumeInfo.optJSONArray("authors")
//                val authors = if (authorsArray != null) {
//                    // Convert JSONArray to a comma-separated string
//                    (0 until authorsArray.length()).map { authorsArray.getString(it) }.joinToString(", ")
//                } else {
//                    "Unknown Author"
//                }
//
//                val imageLinks = volumeInfo.optJSONObject("imageLinks")
//                var thumbnailUrl = imageLinks?.optString("thumbnail")
//
//                // Fix the thumbnail URL (convert http to https if needed)
//                if (thumbnailUrl != null && thumbnailUrl.startsWith("http://")) {
//                    thumbnailUrl = thumbnailUrl.replace("http://", "https://")
//                }
//
//                // Extract the description
//                val description = volumeInfo.optString("description", "No description available.")
//
//                Triple("Title: $title\nAuthors: $authors", thumbnailUrl, description)
//            } else {
//                Triple(null, null, null)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Triple(null, null, null)
//        }
//    }
//}
//
//fun getGoogleApiKey(context: android.content.Context): String? {
//    return try {
//        val applicationInfo = context.packageManager.getApplicationInfo(
//            context.packageName,
//            android.content.pm.PackageManager.GET_META_DATA
//        )
//        val bundle = applicationInfo.metaData
//        bundle?.getString("com.google.android.geo.API_KEY")
//    } catch (e: Exception) {
//        e.printStackTrace()
//        null
//    }
//}

package com.cs407.next_chapter

import android.util.Log
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.GenericTypeIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookInfoScreen(
    navController: NavHostController,
    scannedISBN: String?,
    onAdd: () -> Unit = {}
) {
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
                    IconButton(onClick = { navController.navigate("home") }) {
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
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "ISBN: ${scannedISBN ?: "Unknown"}",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    bookImageUrl.value?.let { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = imageUrl,
                                error = painterResource(id = R.drawable.placeholder_image)
                            ),
                            contentDescription = "Book Cover",
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .aspectRatio(1f)
                                .padding(bottom = 16.dp)
                        )
                    } ?: Text(
                        text = "No book cover available",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = bookDetails.value,
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = bookDescription.value,
                        color = Color.DarkGray,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                            if (uid != null) {
                                addBookDetails(
                                    bookDetails = bookDetails.value,
                                    bookImageUrl = bookImageUrl.value,
                                    uid = uid,
                                    scannedISBN = scannedISBN,
                                    navController = navController
                                )
                            } else {
                                Log.e("BookInfoScreen", "User is not authenticated")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Add Book")
                    }

                    Button(
                        onClick = { navController.navigate("scan_isbn") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Retake")
                    }
                }
            }
        }
    )
}

fun addBookDetails(
    bookDetails: String,
    bookImageUrl: String?,
    uid: String,
    scannedISBN: String?,
    navController: NavHostController
) {
    if (bookDetails.isNotEmpty() && scannedISBN != null) {
        // Extract title and authors from bookDetails
        val title = bookDetails.substringAfter("Title: ").substringBefore("\nAuthors:")
        val authors = bookDetails.substringAfter("Authors: ").split(", ")

        // Reference the Firebase Realtime Database
        val databaseRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("Books")
        val userRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("Users")

        // Check if the book with the same ISBN already exists
        databaseRef.child(scannedISBN).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // If the book exists, update the uid array
                val existingUids = snapshot.child("uid").value as? MutableList<String> ?: mutableListOf()
                if (!existingUids.contains(uid)) {
                    existingUids.add(uid)
                    databaseRef.child(scannedISBN).child("uid").setValue(existingUids)
                        .addOnSuccessListener {
                            Log.d("BookInfoScreen", "User added to existing book's uid array successfully!")
                        }
                        .addOnFailureListener { error ->
                            Log.e("BookInfoScreen", "Failed to update uid array: ${error.message}")
                        }
                } else {
                    Log.d("BookInfoScreen", "User already exists in the uid array for this book.")
                }
            } else {
                // If the book doesn't exist, create a new entry
                val bookData = mapOf(
                    "title" to title,
                    "authors" to authors,
                    "bookImageUrl" to (bookImageUrl ?: "No image available"),
                    "uid" to listOf(uid), // Initialize uid as a list
                    "isbn" to scannedISBN
                )

                databaseRef.child(scannedISBN).setValue(bookData)
                    .addOnSuccessListener {
                        Log.d("BookInfoScreen", "New book added successfully!")
                    }
                    .addOnFailureListener { error ->
                        Log.e("BookInfoScreen", "Failed to add new book: ${error.message}")
                    }
            }
        }.addOnFailureListener { error ->
            Log.e("BookInfoScreen", "Failed to check if book exists: ${error.message}")
        }

        // Add the book's ISBN to the user's "my_books" field
        userRef.child(uid).child("my_books").get().addOnSuccessListener { userSnapshot ->
            val myBooks = userSnapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {}) ?: mutableListOf()

            if (!myBooks.contains(scannedISBN)) {
                myBooks.add(scannedISBN)
                userRef.child(uid).child("my_books").setValue(myBooks)
                    .addOnSuccessListener {
                        Log.d("BookInfoScreen", "ISBN added to user's my_books successfully!")
                        navController.navigate("home") // Navigate to home after successful addition
                    }
                    .addOnFailureListener { error ->
                        Log.e("BookInfoScreen", "Failed to add ISBN to user's my_books: ${error.message}")
                    }
            } else {
                Log.d("BookInfoScreen", "ISBN already exists in user's my_books.")
                navController.navigate("home") // Navigate to home if ISBN already exists
            }
        }.addOnFailureListener { error ->
            Log.e("BookInfoScreen", "Failed to fetch user's my_books: ${error.message}")
        }
    } else {
        Log.e("BookInfoScreen", "Book details are empty or ISBN is null, cannot add to database")
    }
}


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
                val authors = if (authorsArray != null && authorsArray.length() > 0) {
                    (0 until authorsArray.length()).map { authorsArray.getString(it) }.joinToString(", ")
                } else {
                    "Unknown Author"
                }

                val categoriesArray = volumeInfo.optJSONArray("categories")
                val genre = if (categoriesArray != null && categoriesArray.length() > 0) {
                    categoriesArray.join(", ").replace("\"", "")
                } else {
                    "Unknown Genre"
                }

                val imageLinks = volumeInfo.optJSONObject("imageLinks")
                var thumbnailUrl = imageLinks?.optString("thumbnail")
                if (thumbnailUrl != null && thumbnailUrl.startsWith("http://")) {
                    thumbnailUrl = thumbnailUrl.replace("http://", "https://")
                }

                val description = volumeInfo.optString("description", "No description available.")

                val info = "Title: $title\nAuthors: $authors\nGenre: $genre"
                Triple(info, thumbnailUrl, description)
            } else {
                Triple(null, null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Triple(null, null, null)
        }
    }
}

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