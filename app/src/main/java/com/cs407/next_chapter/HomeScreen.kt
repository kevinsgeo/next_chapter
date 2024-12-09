package com.cs407.next_chapter

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter

// **Data Class for Book**
data class Book(
    val isbn: String,
    val title: String,
    val authors: String,
    val thumbnailUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    // State for the search query
    val (searchQuery, setSearchQuery) = remember { mutableStateOf(TextFieldValue("")) }

    // State for sort selection
    val (selectedSort, setSelectedSort) = remember { mutableStateOf("Recommended") }

    // Expanded list of 14 hardcoded books (ensure unique ISBNs where necessary)
    val books = listOf(
        Book(
            isbn = "9780241972939", // Updated ISBN for the first book
            title = "The Forty Rules of Love",
            authors = "Elif Shafak",
            thumbnailUrl = "https://m.media-amazon.com/images/I/914DQ26V6RL._AC_UF894,1000_QL80_.jpg"
        ),
        Book(
            isbn = "9781101189948",
            title = "Another Book",
            authors = "Another Author",
            thumbnailUrl = "https://example.com/thumbnail2.jpg"
        ),
        // Add more books with unique ISBNs as needed
        Book(
            isbn = "9780000000001",
            title = "Sample Book 2",
            authors = "Author Two",
            thumbnailUrl = "https://example.com/thumbnail2.jpg"
        ),
        Book(
            isbn = "9780000000003",
            title = "Sample Book 3",
            authors = "Author Three",
            thumbnailUrl = "https://example.com/thumbnail3.jpg"
        ),
        Book(
            isbn = "9780000000003",
            title = "Sample Book 3",
            authors = "Author Three",
            thumbnailUrl = "https://example.com/thumbnail3.jpg"
        ),
        Book(
            isbn = "9780000000003",
            title = "Sample Book 3",
            authors = "Author Three",
            thumbnailUrl = "https://example.com/thumbnail3.jpg"
        ),
        Book(
            isbn = "9780000000003",
            title = "Sample Book 3",
            authors = "Author Three",
            thumbnailUrl = "https://example.com/thumbnail3.jpg"
        ),
        Book(
            isbn = "9780000000003",
            title = "Sample Book 3",
            authors = "Author Three",
            thumbnailUrl = "https://example.com/thumbnail3.jpg"
        ),
        Book(
            isbn = "9780000000003",
            title = "Sample Book 3",
            authors = "Author Three",
            thumbnailUrl = "https://example.com/thumbnail3.jpg"
        ),
        Book(
            isbn = "9780000000003",
            title = "Sample Book 3",
            authors = "Author Three",
            thumbnailUrl = "https://example.com/thumbnail3.jpg"
        ),
        Book(
            isbn = "9780000000003",
            title = "Sample Book 3",
            authors = "Author Three",
            thumbnailUrl = "https://example.com/thumbnail3.jpg"
        ),
        Book(
            isbn = "9780000000003",
            title = "Sample Book 3",
            authors = "Author Three",
            thumbnailUrl = "https://example.com/thumbnail3.jpg"
        ),
        Book(
            isbn = "9780000000003",
            title = "Sample Book 3",
            authors = "Author Three",
            thumbnailUrl = "https://example.com/thumbnail3.jpg"
        ),
        Book(
            isbn = "9780000000003",
            title = "Sample Book 3",
            authors = "Author Three",
            thumbnailUrl = "https://example.com/thumbnail3.jpg"
        )
        // ... (Add the remaining books similarly)
    )

    // Scaffold to structure the screen with a bottom navigation bar
    Scaffold(
        bottomBar = {
            NavigationBar(navController = navController)
        }
    ) { innerPadding ->
        // Main content with padding from Scaffold
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // Two columns
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = innerPadding, // Apply padding from Scaffold
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header: Search Bar and Filter Button spanning both columns
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = setSearchQuery,
                        label = { Text("Search") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search Icon"
                            )
                        },
                        modifier = Modifier
                            .weight(1f) // Take up remaining space
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp)) // Space between search bar and filter button

                    // Filter Button
                    IconButton(
                        onClick = { /* TODO: Add filter functionality */ },
                        modifier = Modifier
                            .size(56.dp) // Same height as search bar
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

            // Spacer after header
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Sort Options spanning both columns
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sort by:",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // "Nearest You" Button
                    Button(
                        onClick = { setSelectedSort("Nearest You") }, // Update selection
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(40.dp),
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

                    // "Recommended" Button
                    Button(
                        onClick = { setSelectedSort("Recommended") }, // Update selection
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(40.dp),
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

            // Spacer before recommendations
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Recommendations Section Title spanning both columns
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

            // Spacer after title
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Recommendation Cards
            items(books) { book ->
                RecommendationCard(book = book, onClick = {
                    navController.navigate("book_info/${book.isbn}")
                })
            }

            // Spacer at the bottom
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(16.dp))
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
            .clickable { onClick() }, // Make the card clickable
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Book Cover Image
            Image(
                painter = rememberAsyncImagePainter(model = book.thumbnailUrl),
                contentDescription = "Cover of ${book.title}",
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Book Title
            Text(
                text = book.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 4.dp),
                maxLines = 2,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Book Authors
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


    @Composable
    fun NavigationBar(navController: NavController) {
        val items = listOf(
            BottomNavigationItem(
                route = "map",
                title = "Map",
                selectedIcon = ImageVector.vectorResource(R.drawable.baseline_map_24),
                unselectedIcon = ImageVector.vectorResource(R.drawable.outline_map_24)
            ),
            BottomNavigationItem(
                route = "chat",
                title = "Chat",
                selectedIcon = ImageVector.vectorResource(R.drawable.baseline_chat_24),
                unselectedIcon = ImageVector.vectorResource(R.drawable.outline_chat_24)
            ),
            BottomNavigationItem(
                route = "home",
                title = "Home",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home
            ),
            BottomNavigationItem(
                route = "scan_isbn",
                title = "Scan",
                selectedIcon = ImageVector.vectorResource(R.drawable.baseline_photo_camera_24),
                unselectedIcon = ImageVector.vectorResource(R.drawable.outline_photo_camera_24)
            ),
            BottomNavigationItem(
                route = "profile",
                title = "Profile",
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person
            )
        )

        val currentBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = currentBackStackEntry.value?.destination?.route

        NavigationBar {
            items.forEach { item ->
                NavigationBarItem(
                    selected = currentRoute?.startsWith(item.route) == true,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    label = {
                        Text(text = item.title)
                    },
                    alwaysShowLabel = false,
                    icon = {
                        BadgedBox(
                            badge = {}
                        ) {
                            Icon(
                                imageVector = if (currentRoute?.startsWith(item.route) == true) {
                                    item.selectedIcon
                                } else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        }
                    }
                )
            }
        }
    }

        // **Data Class for Bottom Navigation Items**
        data class BottomNavigationItem(
            val route: String,
            val title: String,
            val selectedIcon: ImageVector,
            val unselectedIcon: ImageVector
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewHomeScreen() {
        // Provide a dummy NavController for preview
        val navController = rememberNavController()
        HomeScreen(navController = navController)
    }

