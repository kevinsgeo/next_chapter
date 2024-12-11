package com.cs407.next_chapter

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

// Define the enum outside the composable function
enum class FilterMode {
    NONE,
    AUTHOR,
    GENRE
}

data class Book(
    val isbn: String,
    val title: String,
    val authors: String,
    val thumbnailUrl: String,
    val genre: String
)

@Composable
fun HomeScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    val scope = rememberCoroutineScope()

    val allBooks = remember { mutableStateListOf<Book>() }
    val displayedBooks = remember { mutableStateListOf<Book>() }
    val apiResults = remember { mutableStateListOf<Book>() }
    var hasMore by rememberSaveable { mutableStateOf(true) }
    var lastKey by rememberSaveable { mutableStateOf<String?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var selectedAuthor by remember { mutableStateOf<String?>(null) }
    var selectedGenre by remember { mutableStateOf<String?>(null) }
    var filterMode by remember { mutableStateOf(FilterMode.NONE) }

    suspend fun applyFilter() {
        val query = searchQuery.text.trim().lowercase()
        displayedBooks.clear()
        apiResults.clear()

        if (query.isEmpty()) {
            displayedBooks.addAll(allBooks)
        } else {
            when (filterMode) {
                FilterMode.AUTHOR -> {
                    val filteredLocal = allBooks.filter { it.authors.lowercase().contains(query) }
                    if (filteredLocal.isEmpty()) {
                        val fetchedFromApi = fetchBooksFromGoogleApi(query, FilterMode.AUTHOR)
                        apiResults.addAll(fetchedFromApi)
                    } else {
                        displayedBooks.addAll(filteredLocal)
                    }
                }
                FilterMode.GENRE -> {
                    val filteredLocal = allBooks.filter { it.genre.lowercase().contains(query) }
                    if (filteredLocal.isEmpty()) {
                        val fetchedFromApi = fetchBooksFromGoogleApi(query, FilterMode.GENRE)
                        apiResults.addAll(fetchedFromApi)
                    } else {
                        displayedBooks.addAll(filteredLocal)
                    }
                }
                FilterMode.NONE -> {
                    val filteredLocal = allBooks.filter { it.title.lowercase().contains(query) }
                    if (filteredLocal.isEmpty()) {
                        val fetchedFromApi = fetchBooksFromGoogleApi(query, FilterMode.NONE)
                        apiResults.addAll(fetchedFromApi)
                    } else {
                        displayedBooks.addAll(filteredLocal)
                    }
                }
            }
        }

        if (selectedAuthor != null) {
            displayedBooks.retainAll { it.authors.contains(selectedAuthor!!) }
            apiResults.retainAll { it.authors.contains(selectedAuthor!!) }
        }
        if (selectedGenre != null) {
            displayedBooks.retainAll { it.genre.equals(selectedGenre!!, ignoreCase = true) }
            apiResults.retainAll { it.genre.equals(selectedGenre!!, ignoreCase = true) }
        }
    }

    LaunchedEffect(Unit) {
        val (loadedBooks, lastFetchedKey, moreAvailable) = loadBooksFromDatabase(limit = 10, startAfter = null)
        allBooks.clear()
        allBooks.addAll(loadedBooks)
        lastKey = lastFetchedKey
        hasMore = moreAvailable
        applyFilter()
    }

    LaunchedEffect(searchQuery, filterMode) {
        scope.launch {
            applyFilter()
        }
    }

    val authorsSet = remember { mutableSetOf<String>() }
    val genresSet = remember { mutableSetOf<String>() }

    LaunchedEffect(displayedBooks, apiResults) {
        authorsSet.clear()
        genresSet.clear()
        (displayedBooks + apiResults).forEach { book ->
            book.authors.split(", ").forEach { authorsSet.add(it) }
            if (book.genre.isNotBlank()) {
                genresSet.add(book.genre)
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(navController = navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Search Bar & Filter Button
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search Icon"
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(65.dp),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box {
                            IconButton(
                                onClick = { showFilterMenu = !showFilterMenu },
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filter",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            DropdownMenu(
                                expanded = showFilterMenu,
                                onDismissRequest = { showFilterMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Filter by Author") },
                                    onClick = {
                                        filterMode = FilterMode.AUTHOR
                                        showFilterMenu = false
                                        scope.launch { applyFilter() }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Filter by Genre") },
                                    onClick = {
                                        filterMode = FilterMode.GENRE
                                        showFilterMenu = false
                                        scope.launch { applyFilter() }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Clear Filters") },
                                    onClick = {
                                        filterMode = FilterMode.NONE
                                        selectedAuthor = null
                                        selectedGenre = null
                                        showFilterMenu = false
                                        scope.launch { applyFilter() }
                                    }
                                )
                            }
                        }
                    }
                }

                // Spacer
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Available for swap section
                if (displayedBooks.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "Available for swap",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp),
                            textAlign = TextAlign.Start
                        )
                    }

                    items(displayedBooks) { book ->
                        RecommendationCard(book = book, onClick = {
                            navController.navigate("book_info/${book.isbn}")
                        })
                    }
                }

                // Not available for swap section
                if (apiResults.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Not available for swap",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp),
                            textAlign = TextAlign.Start
                        )
                    }

                    items(apiResults) { book ->
                        RecommendationCard(book = book, onClick = {
                            navController.navigate("book_info/${book.isbn}")
                        })
                    }
                }

                // Load More button
                if (hasMore && searchQuery.text.isBlank()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    val (loadedBooks, lastFetchedKey, moreAvailable) = loadBooksFromDatabase(
                                        limit = 10,
                                        startAfter = lastKey
                                    )
                                    allBooks.addAll(loadedBooks)
                                    lastKey = lastFetchedKey
                                    hasMore = moreAvailable
                                    applyFilter()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text("Load More")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

        }
    }
}

@Composable
fun RecommendationCard(book: Book, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = book.thumbnailUrl),
                contentDescription = "Cover of ${book.title}",
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = book.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 4.dp),
                maxLines = 2,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = book.authors,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 4.dp),
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

suspend fun loadBooksFromDatabase(
    limit: Int,
    startAfter: String?
): Triple<List<Book>, String?, Boolean> {
    val db = FirebaseDatabase.getInstance().reference.child("Books")

    val query: Query = if (startAfter != null) {
        db.orderByKey()
            .startAfter(startAfter)
            .limitToFirst(limit)
    } else {
        db.orderByKey()
            .limitToFirst(limit)
    }

    val snapshot = query.get().await()

    val loadedBooks = mutableListOf<Book>()
    var finalKey: String? = null
    for (child in snapshot.children) {
        val isbn = child.key
        val title = child.child("title").value as? String ?: "Unknown Title"
        val authorsList = child.child("authors").value as? List<*>
        val authors = authorsList?.joinToString(", ") ?: "Unknown Author"
        val bookImageUrl = child.child("bookImageUrl").value as? String ?: ""
        val genre = child.child("genre").value as? String ?: "Unknown Genre"

        if (isbn != null) {
            loadedBooks.add(
                Book(
                    isbn = isbn,
                    title = title,
                    authors = authors,
                    thumbnailUrl = bookImageUrl,
                    genre = genre
                )
            )
            finalKey = isbn
        }
    }

    val hasMoreData = loadedBooks.size == limit
    return Triple(loadedBooks, finalKey, hasMoreData)
}

suspend fun fetchBooksFromGoogleApi(query: String, filterMode: FilterMode): List<Book> {
    return withContext(kotlinx.coroutines.Dispatchers.IO) {
        val client = OkHttpClient()
        val formattedQuery = when (filterMode) {
            FilterMode.AUTHOR -> "inauthor:$query"
            FilterMode.GENRE -> "subject:$query"
            FilterMode.NONE -> query
        }
        val url = "https://www.googleapis.com/books/v1/volumes?q=$formattedQuery"
        val request = Request.Builder().url(url).build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return@withContext emptyList()

        val jsonResponse = JSONObject(response.body?.string() ?: "")
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

            val industryIdentifiers = volumeInfo.optJSONArray("industryIdentifiers")
            var isbnValue = "no_isbn_${title.hashCode()}"
            if (industryIdentifiers != null) {
                var foundIsbn13: String? = null
                var foundIsbn10: String? = null
                for (j in 0 until industryIdentifiers.length()) {
                    val identifierObj = industryIdentifiers.getJSONObject(j)
                    val type = identifierObj.optString("type", "")
                    val identifier = identifierObj.optString("identifier", "")
                    if (type.equals("ISBN_13", ignoreCase = true)) {
                        foundIsbn13 = identifier
                    } else if (type.equals("ISBN_10", ignoreCase = true)) {
                        foundIsbn10 = identifier
                    }
                }
                isbnValue = foundIsbn13 ?: foundIsbn10 ?: isbnValue
            }

            apiBooks.add(
                Book(
                    isbn = isbnValue,
                    title = title,
                    authors = authors,
                    thumbnailUrl = thumbnailUrl ?: "",
                    genre = genre
                )
            )
        }
        return@withContext apiBooks
    }
}
