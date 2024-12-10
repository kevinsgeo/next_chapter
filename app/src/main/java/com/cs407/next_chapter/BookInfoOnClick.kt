package com.cs407.next_chapter

import android.util.Log
import android.widget.Toast
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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import io.getstream.chat.android.client.ChatClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookInfoOnClick(navController: NavHostController, scannedISBN: String?) {
    val bookDetails = remember { mutableStateOf("Loading...") }
    val bookImageUrl = remember { mutableStateOf<String?>(null) }
    val bookDescription = remember { mutableStateOf("Loading description...") }
    val context = LocalContext.current
    val chatClient = ChatClient.instance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = currentUserId?.let { FirebaseDatabase.getInstance().getReference("Users").child(it).child("username") }
    val snapshot = db?.get()
    val currentUserName = fetchCurrentUserName()



    // State to hold the list of owners' names and ids
    val owners = remember { mutableStateListOf<Pair<String, String>>() }

    LaunchedEffect(scannedISBN) {
        if (scannedISBN != null) {
            // Call fetchBookDetails from wherever it's defined (e.g., BookInfoScreen.kt)
            val apiKey = getGoogleApiKey(context) // Assume defined in BookInfoScreen.kt or another utility file
            if (apiKey != null) {
                val (info, imageUrl, description) = fetchBookDetails(scannedISBN, apiKey) // Assume defined elsewhere
                bookDetails.value = info ?: "No details found for ISBN: $scannedISBN"
                bookImageUrl.value = imageUrl
                bookDescription.value = description ?: "No description available."
            } else {
                bookDetails.value = "API Key not found"
                bookDescription.value = "Unable to fetch description without API key."
            }

            // After fetching book info, load owners from Firebase
            loadBookOwners(scannedISBN, owners)

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
                    IconButton(onClick = { navController.popBackStack() }) {
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
                        text = "ISBN: ${scannedISBN ?: "Unknown"}",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Display the book's image if available
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

                    // Display owners if any
                    if (owners.isNotEmpty()) {
                        Text(
                            text = "Users who have this book:",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        owners.forEach { (ownerName, ownerId) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Text(
                                    text = ownerName,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.Black,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Button(onClick = {
                                    if (currentUser != null) {
                                        val user = ChatClient.instance().clientState.user.value
                                        if (user == null) {
                                            Log.e("BookInfoOnClick", "User is not connected. Attempting to connect...")

                                            val chatUser = io.getstream.chat.android.models.User(
                                                id = currentUser.uid,
                                                extraData = mapOf(
                                                    "name" to (currentUser.displayName ?: "Guest"),
                                                    "image" to (currentUser.photoUrl?.toString() ?: "https://bit.ly/2TIt8NR")
                                                )
                                            )
                                            val token = ChatClient.instance().devToken(currentUser.uid)

                                            ChatClient.instance().connectUser(chatUser, token).enqueue { result ->
                                                if (result.isSuccess) {
                                                    Log.d("BookInfoOnClick", "User connected successfully.")
                                                    if (currentUserName != null) {
                                                        createAndNavigateToChannel(currentUser.uid, ownerId, ownerName, currentUserName, context)
                                                    }
                                                } else {
                                                    Log.e("BookInfoOnClick", "Error connecting user: ${result.errorOrNull()?.message}")
                                                }
                                            }
                                        } else {
                                            if (currentUserName != null) {
                                                createAndNavigateToChannel(currentUser.uid, ownerId, ownerName, currentUserName, context)
                                            }
                                        }
                                    } else {
                                        Log.e("BookInfoOnClick", "Error: Current user is null")
                                    }
                                }) {
                                    Text("Message")
                                }
                            }
                        }

                    } else {
                        // Show a message if no owners found yet (and no error)
                        if (scannedISBN != null && owners.isEmpty()) {
                            Text(
                                text = "No known owners",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                                    if (uid != null && scannedISBN != null) {
                                        addToWishlist(isbn = scannedISBN, uid = uid, navController = navController)
                                        Toast.makeText(context, "Added to wishlist!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Log.e("BookInfoScreen", "User is not authenticated or ISBN is null")
                                    }

                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text("Add to Wishlist")
                            }


                        }
                    }
                }
            }
        }
    )
}

// Removed fetchBookDetails and getGoogleApiKey from here since they are defined elsewhere.
// Make sure to import them from the file where they are defined, e.g.:
// import com.cs407.next_chapter.fetchBookDetails
// import com.cs407.next_chapter.getGoogleApiKey

fun createAndNavigateToChannel(
    currentUserId: String,
    ownerId: String,
    ownerName: String,
    currentUserName: String,
    context: android.content.Context
) {
    val memberIds = listOf(currentUserId, ownerId)
    val extraData = mapOf(
        "name" to "$ownerName and $currentUserName",
        "members" to memberIds
    )

    ChatClient.instance().createChannel(
        channelType = "messaging",
        channelId = "${currentUserId}_$ownerId",
        memberIds = memberIds,
        extraData = extraData
    ).enqueue { result ->
        if (result.isSuccess) {
            val channel = result.getOrNull()
            if (channel != null) {
                Log.d("BookInfoOnClick", "Channel created successfully: ${channel.cid}")
                context.startActivity(
                    ChannelActivity.getIntent(context, channel.cid)
                )
            }
        } else {
            Log.e("BookInfoOnClick", "Error creating channel: ${result.errorOrNull()?.message}")
        }
    }
}

suspend fun loadBookOwners(isbn: String, owners: MutableList<Pair<String, String>>) {
    withContext(Dispatchers.IO) {
        try {
            val db = FirebaseDatabase.getInstance().reference
            val bookSnapshot = db.child("Books").child(isbn).get().await()

            val uids = bookSnapshot.child("uid").getValue<List<String>>() ?: return@withContext
            for (uid in uids) {
                val userSnapshot = db.child("Users").child(uid).get().await()
                val userName = userSnapshot.child("name").getValue<String>() ?: "Unknown User"
                owners.add(userName to uid)
            }
        } catch (e: IOException) {
            Log.e("loadBookOwners", "Error loading owners: ${e.message}", e)
        }
    }
}

fun addToWishlist(isbn: String, uid: String, navController: NavHostController) {
    val databaseRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("Wishlist")

    databaseRef.child(isbn).get().addOnSuccessListener { snapshot ->
        if (snapshot.exists()) {
            val existingUids = snapshot.child("uid").value as? MutableList<String> ?: mutableListOf()
            if (!existingUids.contains(uid)) {
                existingUids.add(uid)
                databaseRef.child(isbn).child("uid").setValue(existingUids)
                    .addOnSuccessListener {
                        Log.d("addToWishlist", "User added to the wishlist successfully!")
                        navController.navigate("home")
                    }
                    .addOnFailureListener { error ->
                        Log.e("addToWishlist", "Failed to update uid array: ${error.message}")
                    }
            } else {
                Log.d("addToWishlist", "User already exists in the wishlist for this book.")
                navController.navigate("home")
            }
        } else {
            val wishlistData = mapOf(
                "isbn" to isbn,
                "uid" to listOf(uid)
            )

            databaseRef.child(isbn).setValue(wishlistData)
                .addOnSuccessListener {
                    Log.d("addToWishlist", "New book added to wishlist successfully!")
                    navController.navigate("home")
                }
                .addOnFailureListener { error ->
                    Log.e("addToWishlist", "Failed to add new wishlist entry: ${error.message}")
                }
        }
    }.addOnFailureListener { error ->
        Log.e("addToWishlist", "Failed to check if wishlist entry exists: ${error.message}")
    }
}
fun fetchCurrentUserName(): String? {
    return runBlocking {
        try {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserId != null) {
                val db = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId).child("username")
                val snapshot = db.get().await()
                snapshot.value?.toString() ?: "Unknown User"
            } else {
                Log.e("Firebase", "User is not authenticated.")
                null
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Error fetching user name: ${e.message}", e)
            null
        }
    }
}
