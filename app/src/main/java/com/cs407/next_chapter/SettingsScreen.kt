package com.cs407.next_chapter

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import io.getstream.chat.android.client.ChatClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    navController: NavHostController
) {
    var sliderValue by remember { mutableStateOf(10f) }
    var showDialog by remember { mutableStateOf(false) } // State for showing the dialog
    val context = LocalContext.current

    // State to hold the user's swaps count
    var swapsCount by remember { mutableStateOf(0) }

    // Fetch user's swaps count
    LaunchedEffect(Unit) {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid
        if (userUid != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userUid)
            userRef.child("swaps").get().addOnSuccessListener { snapshot ->
                swapsCount = (snapshot.value as? Long)?.toInt() ?: 0
            }.addOnFailureListener { error ->
                Log.e("SettingsScreen", "Failed to fetch swaps count: ${error.message}")
            }
        } else {
            Log.e("SettingsScreen", "No user is currently logged in")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { onBackPressed() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Log Out Button
            Button(
                onClick = { LogOutPressed(context, navController) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Log Out")
            }

            // Delete Account Button
            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Delete Account")
            }

            // Swaps Count
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Image
                Image(
                    painter = painterResource(id = R.drawable.green_footprint),
                    contentDescription = "Green Footprint Background",
                    modifier = Modifier.fillMaxSize(),
                    alpha = 0.4f
                )

                // Text Content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Swaps Completed",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = FontFamily.Cursive,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "$swapsCount",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = FontFamily.Cursive,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Show confirmation dialog
            if (showDialog) {
                DeleteAccountConfirmationDialog(
                    context = context,
                    navController = navController,
                    onDismiss = { showDialog = false }
                )
            }
        }
    }
}

@Composable
fun DeleteAccountConfirmationDialog(
    context: Context,
    navController: NavHostController,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Delete Account") },
        text = {
            Text(
                text = "Are you sure you want to delete your account? This will delete all your uploaded books and your wishlist."
            )
        },
        confirmButton = {
            Button(onClick = {
                DeleteAccountPressed(context, navController)
                onDismiss()
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

fun DeleteAccountPressed(context: Context, navController: NavHostController) {
    disconnectChatUser()
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser != null) {
        val uid = currentUser.uid

        val databaseRef = FirebaseDatabase.getInstance().getReference("Users")
        val booksRef = FirebaseDatabase.getInstance().getReference("Books")

        databaseRef.child(uid).removeValue().addOnSuccessListener {
            booksRef.get().addOnSuccessListener { snapshot ->
                snapshot.children.forEach { book ->
                    val uidList = book.child("uid").value as? MutableList<String>
                    if (uidList != null && uidList.contains(uid)) {
                        uidList.remove(uid)
                        if (uidList.isEmpty()) {
                            booksRef.child(book.key!!).removeValue()
                        } else {
                            booksRef.child(book.key!!).child("uid").setValue(uidList)
                        }
                    }
                }

                currentUser.delete().addOnSuccessListener {
                    Toast.makeText(context, "Account successfully deleted!", Toast.LENGTH_SHORT).show()
                    navController.navigate("SigninScreen") { popUpTo(0) }
                }.addOnFailureListener { error ->
                    Log.e("SettingsScreen", "Failed to delete user account: ${error.message}")
                }
            }.addOnFailureListener { error ->
                Log.e("SettingsScreen", "Failed to retrieve books for user UID cleanup: ${error.message}")
            }
        }.addOnFailureListener { error ->
            Log.e("SettingsScreen", "Failed to remove user data: ${error.message}")
        }
    } else {
        Log.e("SettingsScreen", "No user is currently logged in")
    }
}

fun LogOutPressed(context: Context, navController: NavHostController) {
    FirebaseAuth.getInstance().signOut()
    Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show()
    disconnectChatUser()
    navController.navigate("SigninScreen") { popUpTo(0) }
}

fun disconnectChatUser() {
    val client = ChatClient.instance()

    client.disconnect(flushPersistence = true).enqueue { result ->
        result.onSuccess {
            Log.d("ChatDisconnection", "User disconnected successfully.")
        }.onError { error ->
            Log.e("ChatDisconnection", "Error disconnecting user: ${error.message}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    val navController = rememberNavController()

    MaterialTheme {
        SettingsScreen(
            onBackPressed = { Log.d("SettingsScreen", "Back button pressed in preview") },
            navController = navController
        )
    }
}


