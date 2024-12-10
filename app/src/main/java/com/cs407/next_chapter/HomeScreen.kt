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
    // State declarations using delegated properties
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var selectedSort by remember { mutableStateOf("Recommended") }

    val scope = rememberCoroutineScope()

    // Holds all books loaded so far from local DB
    val allBooks = remember { mutableStateListOf<Book>() }

    // Holds the currently displayed subset of books from local DB (after searching)
    val displayedBooks = remember { mutableStateListOf<Book>() }

    // Holds books from Google API if local search yields no result
    val apiResults = remember { mutableStateListOf<Book>() }

    // Track if more books are available to load (local DB)
    var hasMore by rememberSaveable { mutableStateOf(true) }

    // Track the last key fetched (for pagination)
    var lastKey by rememberSaveable { mutableStateOf<String?>(null) }

    // Filtering states
    var showFilterMenu by remember { mutableStateOf(false) }
    var selectedAuthor by remember { mutableStateOf<String?>(null) }
    var selectedGenre by remember { mutableStateOf<String?>(null) }

    // Add filter mode state
    var filterMode by remember { mutableStateOf(FilterMode.NONE) }

    // Function to apply filters
    suspend fun applyFilter() {
        val query = searchQuery.text.trim().lowercase()
        displayedBooks.clear()
        apiResults.clear()

        if (query.isEmpty()) {
            // Show all local books if no query
            displayedBooks.addAll(allBooks)
        } else {
            when (filterMode) {
                FilterMode.AUTHOR -> {
                    // Filter allBooks by authors
                    val filteredLocal = allBooks.filter {
                        it.authors.lowercase().contains(query)
                    }
                    if (filteredLocal.isEmpty()) {
                        // If no local matches, fetch from API
                        val fetchedFromApi = fetchBooksFromGoogleApi(query, FilterMode.AUTHOR)
                        apiResults.addAll(fetchedFromApi)
                    } else {
                        displayedBooks.addAll(filteredLocal)
                    }
                }
                FilterMode.GENRE -> {
                    // Filter allBooks by genre
                    val filteredLocal = allBooks.filter {
                        it.genre.lowercase().contains(query)
                    }
                    if (filteredLocal.isEmpty()) {
                        // If no local matches, fetch from API
                        val fetchedFromApi = fetchBooksFromGoogleApi(query, FilterMode.GENRE)
                        apiResults.addAll(fetchedFromApi)
                    } else {
                        displayedBooks.addAll(filteredLocal)
                    }
                }
                FilterMode.NONE -> {
                    // Default filter by title
                    val filteredLocal = allBooks.filter {
                        it.title.lowercase().contains(query)
                    }
                    if (filteredLocal.isEmpty()) {
                        // If no local matches, fetch from API
                        val fetchedFromApi = fetchBooksFromGoogleApi(query, FilterMode.NONE)
                        apiResults.addAll(fetchedFromApi)
                    } else {
                        displayedBooks.addAll(filteredLocal)
                    }
                }
            }
        }

        // Apply specific value filters if selected
        if (selectedAuthor != null) {
            displayedBooks.retainAll { it.authors.contains(selectedAuthor!!) }
            apiResults.retainAll { it.authors.contains(selectedAuthor!!) }
        }
        if (selectedGenre != null) {
            displayedBooks.retainAll { it.genre.equals(selectedGenre!!, ignoreCase = true) }
            apiResults.retainAll { it.genre.equals(selectedGenre!!, ignoreCase = true) }
        }
    }

    // Initially load the first 10 books locally
    LaunchedEffect(Unit) {
        val (loadedBooks, lastFetchedKey, moreAvailable) = loadBooksFromDatabase(limit = 10, startAfter = null)
        allBooks.clear()
        allBooks.addAll(loadedBooks)
        lastKey = lastFetchedKey
        hasMore = moreAvailable

        // Initially show all books (no search)
        applyFilter()
    }

    // Every time the search query or filter mode changes, re-apply the filter
    LaunchedEffect(searchQuery, filterMode) {
        scope.launch {
            applyFilter()
        }
    }

    // Collect authors and genres for filtering menu
    val authorsSet = remember {
        mutableSetOf<String>()
    }
    val genresSet = remember {
        mutableSetOf<String>()
    }

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
                        }
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Sort Buttons
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sort by:",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = { selectedSort = "Nearest You" },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedSort == "Nearest You") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(
                                text = "Nearest You",
                                color = if (selectedSort == "Nearest You") Color.White else Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { selectedSort = "Recommended" },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedSort == "Recommended") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(
                                text = "Recommended",
                                color = if (selectedSort == "Recommended") Color.White else Color.Black
                            )
                        }
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                val showAvailableSection = displayedBooks.isNotEmpty()
                val showApiSection = apiResults.isNotEmpty()

                if (showAvailableSection || showApiSection) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "Recommended for You",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp),
                            textAlign = TextAlign.Start
                        )
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Show "Available for swap" if we have local results
                    if (showAvailableSection) {
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

                    // If we have API results and the query is not empty, show "Not available for swap"
                    if (showApiSection) {
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

                    // Show "Load More" only if we are not currently searching and have more local books
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

                } else {
                    if (searchQuery.text.isNotBlank()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No results found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    } else {
                        // If query is blank and we have no books at all, means DB empty
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No books available",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            // DropdownMenu placed outside the LazyVerticalGrid
            if (showFilterMenu) {
                DropdownMenu(
                    expanded = showFilterMenu,
                    onDismissRequest = { showFilterMenu = false },
                    modifier = Modifier
                        .zIndex(10f)
                        .background(Color.White)
                ) {
                    // Filter Mode Selection
                    DropdownMenuItem(
                        text = { Text("Filter by Author") },
                        onClick = {
                            filterMode = FilterMode.AUTHOR
                            selectedGenre = null // Clear genre filter if any
                            showFilterMenu = false
                            scope.launch { applyFilter() }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Filter by Genre") },
                        onClick = {
                            filterMode = FilterMode.GENRE
                            selectedAuthor = null // Clear author filter if any
                            showFilterMenu = false
                            scope.launch { applyFilter() }
                        }
                    )

                    // Divider between filter modes and specific selections
                    Divider()

                    // Show specific filter options based on the selected filter mode
                    when (filterMode) {
                        FilterMode.AUTHOR -> {
                            if (authorsSet.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Clear Author Filter") },
                                    onClick = {
                                        selectedAuthor = null
                                        filterMode = FilterMode.NONE
                                        showFilterMenu = false
                                        scope.launch { applyFilter() }
                                    }
                                )
                                authorsSet.forEach { author ->
                                    DropdownMenuItem(
                                        text = { Text(author) },
                                        onClick = {
                                            selectedAuthor = author
                                            showFilterMenu = false
                                            scope.launch { applyFilter() }
                                        }
                                    )
                                }
                            } else {
                                DropdownMenuItem(
                                    text = { Text("No Authors Available") },
                                    onClick = { /* Optionally handle this case */ }
                                )
                            }
                        }
                        FilterMode.GENRE -> {
                            if (genresSet.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Clear Genre Filter") },
                                    onClick = {
                                        selectedGenre = null
                                        filterMode = FilterMode.NONE
                                        showFilterMenu = false
                                        scope.launch { applyFilter() }
                                    }
                                )
                                genresSet.forEach { genre ->
                                    DropdownMenuItem(
                                        text = { Text(genre) },
                                        onClick = {
                                            selectedGenre = genre
                                            showFilterMenu = false
                                            scope.launch { applyFilter() }
                                        }
                                    )
                                }
                            } else {
                                DropdownMenuItem(
                                    text = { Text("No Genres Available") },
                                    onClick = { /* Optionally handle this case */ }
                                )
                            }
                        }
                        else -> {
                            // No specific filter mode selected
                        }
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
