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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreenInfoScreen(
    navController: NavHostController,
    scannedISBN: String?
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
                    IconButton(onClick = { navController.navigate("profile") }) {
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
                    bookImageUrl.value?.let { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = imageUrl,
                                error = painterResource(id = R.drawable.placeholder_image)
                            ),
                            contentDescription = "Book Cover",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
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
                }
            }
        }
    )
}

