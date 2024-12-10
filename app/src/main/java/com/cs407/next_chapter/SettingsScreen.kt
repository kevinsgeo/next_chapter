package com.cs407.next_chapter

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
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
            // Slider
            Text(text = "Location", style = MaterialTheme.typography.bodyLarge)
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = 10f..50f,
                steps = 8,
                modifier = Modifier.fillMaxWidth()
            )
            Text(text = "Distance: ${sliderValue.toInt()} miles")
            Spacer(modifier = Modifier.height(32.dp))

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
        val uid = currentUser.uid // Get the current user's UID

        // Reference to the Users database
        val databaseRef = com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference("Users")

        // Reference to the Books database
        val booksRef = com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference("Books")

        // Delete user data from Firebase Realtime Database
        databaseRef.child(uid).removeValue().addOnSuccessListener {
            Log.d("SettingsScreen", "User data removed from Firebase Realtime Database")

            // Remove user's UID from books they've uploaded
            booksRef.get().addOnSuccessListener { snapshot ->
                snapshot.children.forEach { book ->
                    val uidList = book.child("uid").value as? MutableList<String>
                    if (uidList != null && uidList.contains(uid)) {
                        uidList.remove(uid)
                        if (uidList.isEmpty()) {
                            booksRef.child(book.key!!).removeValue().addOnSuccessListener {
                                Log.d("SettingsScreen", "Book entry removed as no users remain.")
                            }.addOnFailureListener { error ->
                                Log.e("SettingsScreen", "Failed to remove book entry: ${error.message}")
                            }
                        } else {
                            booksRef.child(book.key!!).child("uid").setValue(uidList)
                                .addOnSuccessListener {
                                    Log.d("SettingsScreen", "UID removed from book entry successfully.")
                                }.addOnFailureListener { error ->
                                    Log.e("SettingsScreen", "Failed to update book UID list: ${error.message}")
                                }
                        }
                    }
                }

                // Delete the user's account from Firebase Authentication
                currentUser.delete().addOnSuccessListener {
                    Log.d("SettingsScreen", "User account deleted from Firebase Authentication")
                    Toast.makeText(context, "Account successfully deleted!", Toast.LENGTH_SHORT).show()
                    navController.navigate("SigninScreen") { popUpTo(0) } // Navigate to SigninScreen
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
    val navController = rememberNavController() // Create a mock NavController for the preview

    MaterialTheme {
        SettingsScreen(
            onBackPressed = { Log.d("SettingsScreen", "Back button pressed in preview") },
            navController = navController // Pass the mock NavController
        )
    }
}
