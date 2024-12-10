package com.cs407.next_chapter

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavHostController? = null, // Optional for preview
    onSettingsClick: () -> Unit = {
        navController?.navigate("settings")
    }
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val userUid = FirebaseAuth.getInstance().currentUser?.uid

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                navController?.let { NavigationBar(navController = it) }
            },
            topBar = {
                TopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Profile")
                        }
                    },
                    actions = {
                        IconButton(onClick = { onSettingsClick() }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val tabs = listOf("Wishlist", "My Books")
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(text = title) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTabIndex) {
                    0 -> userUid?.let {
                        WishlistContent(
                            navController = navController!!,
                            userUid = it
                        )
                    }

                    1 -> MyBooksContent()
                }
            }
        }
    }
}

@Composable
fun WishlistContent(navController: NavHostController, userUid: String) {
    val wishlistBooks = remember { mutableStateListOf<Book>() }
    val isLoading = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val fetchedBooks = fetchBooksFromWishlist(userUid)
            wishlistBooks.clear()
            wishlistBooks.addAll(fetchedBooks)
        } catch (e: Exception) {
            Log.e("WishlistContent", "Error fetching wishlist: ${e.message}")
        } finally {
            isLoading.value = false
        }
    }

    if (isLoading.value) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (wishlistBooks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Your wishlist is empty!")
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(wishlistBooks) { book ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clickable {
                            navController.navigate("book_info/${book.isbn}")
                        },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = book.thumbnailUrl),
                            contentDescription = "Book Cover",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = book.authors,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Genre: ${book.genre}",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun MyBooksContent() {
    val userUid = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current
    val apiKey = getGoogleApiKey(context)
    val booksDetails = remember { mutableStateListOf<Book>() } // State list for recomposition
    val isLoading = remember { mutableStateOf(true) }

    // Fetch ISBNs and corresponding book details
    LaunchedEffect(userUid, apiKey) {
        if (userUid == null) {
            Log.e("MyBooksContent", "User is not authenticated.")
            isLoading.value = false
            return@LaunchedEffect
        }

        if (apiKey == null) {
            Log.e("MyBooksContent", "Google API Key not found.")
            isLoading.value = false
            return@LaunchedEffect
        }

        try {
            // Step 1: Fetch user's `my_books` ISBNs from Firebase
            Log.d("MyBooksContent", "Fetching ISBNs from Firebase.")
            val userRef = com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("Users").child(userUid).child("my_books")
            val snapshot = userRef.get().await()
            val isbnList = snapshot.value as? List<*> ?: emptyList<String>()

            if (isbnList.isEmpty()) {
                Log.d("MyBooksContent", "No ISBNs found for the user.")
                isLoading.value = false
                return@LaunchedEffect
            }

            Log.d("MyBooksContent", "Fetched ISBNs: $isbnList")

            // Step 2: Fetch book details from Google Books API
            booksDetails.clear()
            for (isbn in isbnList.filterIsInstance<String>()) {
                Log.d("MyBooksContent", "Calling Google Books API for ISBN: $isbn")
                val booksFromApi = fetchBooksByISBN(isbn)
                if (booksFromApi.isNotEmpty()) {
                    val firstBook = booksFromApi.first()
                    if (booksDetails.none { it.isbn == firstBook.isbn }) {
                        booksDetails.add(firstBook)
                        Log.d("MyBooksContent", "Added book: ${firstBook.title}")
                    }
                } else {
                    Log.d("MyBooksContent", "No results found for ISBN: $isbn")
                }
            }
        } catch (e: Exception) {
            Log.e("MyBooksContent", "Error fetching data: ${e.message}")
        } finally {
            isLoading.value = false
        }
    }

    // UI logic
    if (isLoading.value) {
        // Show a loading indicator while fetching data
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        if (booksDetails.isEmpty()) {
            Log.d("MyBooksContent", "Books list is empty.")
            // Show a message if no books are found
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No books available", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            // Display books in a grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // Two columns
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(booksDetails) { book ->
                    RecommendationCard(book = book, onClick = {
                        Log.d("MyBooksContent", "Clicked on ${book.title}")
                    })
                }
            }
        }
    }
}

suspend fun fetchBooksByISBN(isbn: String): List<Book> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val url = "https://www.googleapis.com/books/v1/volumes?q=isbn:$isbn"
        val request = Request.Builder().url(url).build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            Log.e("fetchBooksByISBN", "API call failed for ISBN: $isbn, Response: ${response.body?.string()}")
            return@withContext emptyList<Book>()
        }

        val jsonResponse = JSONObject(response.body?.string() ?: "")
        Log.d("fetchBooksByISBN", "Raw API Response for ISBN $isbn: $jsonResponse")

        val items = jsonResponse.optJSONArray("items") ?: return@withContext emptyList<Book>()

        val apiBooks = mutableListOf<Book>()
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            val volumeInfo = item.optJSONObject("volumeInfo") ?: continue
            val title = volumeInfo.optString("title", "Unknown Title")
            val authorsArray = volumeInfo.optJSONArray("authors")
            val authors = if (authorsArray != null) {
                (0 until authorsArray.length()).map { authorsArray.getString(it) }.joinToString(", ")
            } else {
                "Unknown Author"
            }

            val imageLinks = volumeInfo.optJSONObject("imageLinks")
            var thumbnailUrl = imageLinks?.optString("thumbnail")
            if (thumbnailUrl != null && thumbnailUrl.startsWith("http://")) {
                thumbnailUrl = thumbnailUrl.replace("http://", "https://")
            }

            apiBooks.add(
                Book(
                    isbn = isbn,
                    title = title,
                    authors = authors,
                    thumbnailUrl = thumbnailUrl ?: "",
                    genre = "Unknown Genre"
                )
            )
        }

        Log.d("fetchBooksByISBN", "Parsed Books for ISBN $isbn: $apiBooks")
        return@withContext apiBooks
    }
}

suspend fun fetchBooksFromWishlist(userUid: String): List<Book> = withContext(Dispatchers.IO) {
    val db = FirebaseDatabase.getInstance().reference.child("Wishlist")
    val snapshot = db.get().await()
    val wishlistBooks = mutableListOf<Book>()

    for (child in snapshot.children) {
        val isbn = child.key ?: continue
        val uids = child.child("uid").value as? List<*>
        if (uids != null && userUid in uids) {
            val bookDetails = fetchBookDetailsFromGoogleApi(isbn)
            bookDetails?.let { (title, authors, thumbnail, genre) ->
                wishlistBooks.add(
                    Book(
                        isbn = isbn,
                        title = title,
                        authors = authors,
                        thumbnailUrl = thumbnail ?: "", // Handle null thumbnail gracefully
                        genre = genre
                    )
                )
            }
        }
    }

    wishlistBooks
}

suspend fun fetchBookDetailsFromGoogleApi(isbn: String): Quadruple<String, String, String?, String>? = withContext(Dispatchers.IO) {
    val apiUrl = "https://www.googleapis.com/books/v1/volumes?q=isbn:$isbn"
    val client = OkHttpClient()
    val request = Request.Builder().url(apiUrl).build()

    try {
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return@withContext null

        val jsonResponse = JSONObject(response.body?.string() ?: "")
        val itemsArray = jsonResponse.optJSONArray("items") ?: return@withContext null
        val volumeInfo = itemsArray.getJSONObject(0).getJSONObject("volumeInfo")

        val title = volumeInfo.optString("title", "Unknown Title")
        val authorsArray = volumeInfo.optJSONArray("authors")
        val authors = if (authorsArray != null) {
            (0 until authorsArray.length()).joinToString(", ") { authorsArray.getString(it) }
        } else {
            "Unknown Author"
        }
        val thumbnail = volumeInfo.optJSONObject("imageLinks")?.optString("thumbnail")?.let { url ->
            if (url.startsWith("http://")) url.replace("http://", "https://") else url
        }
        val genresArray = volumeInfo.optJSONArray("categories")
        val genre = genresArray?.getString(0) ?: "Unknown Genre"

        Quadruple(title, authors, thumbnail, genre)
    } catch (e: Exception) {
        Log.e("fetchBookDetails", "Error fetching book details: ${e.message}")
        null
    }
}

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)


@Preview(showBackground = true)
@Composable
fun UserProfileScreenPreview() {
    UserProfileScreen()
}

@Preview(showBackground = true)
@Composable
fun MyBooksContentPreview() {
    MyBooksContent()
}