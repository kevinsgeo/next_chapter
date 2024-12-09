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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit, // Navigate back to the UserProfileScreen
    onLogoutPressed: () -> Unit = { LogOutPressed() },
    onDeleteAccountPressed: () -> Unit = { DeleteAccountPressed() }
) {
    var sliderValue by remember { mutableStateOf(10f) } // Initial value for the slider

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
            // Slider Title
            Text(
                text = "Location",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Slider
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = 10f..50f, // Range from 10 to 50
                steps = 8, // Increment in steps of 5 miles (10 to 50 = 8 steps)
                modifier = Modifier.fillMaxWidth()
            )

            // Display the current slider value
            Text(text = "Distance: ${sliderValue.toInt()} miles")

            Spacer(modifier = Modifier.height(32.dp))

            // Logout Button
            Button(
                onClick = { onLogoutPressed() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Log Out")
            }

            // Delete Account Button
            Button(
                onClick = { onDeleteAccountPressed() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Delete Account")
            }
        }
    }
}

// Dummy function for "Log Out"
fun LogOutPressed() {
    Log.d("SettingsScreen", "Log Out button pressed")
}

// Dummy function for "Delete Account"
fun DeleteAccountPressed() {
    Log.d("SettingsScreen", "Delete Account button pressed")
}

// Preview Function
@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen(
            onBackPressed = { Log.d("SettingsScreen", "Back button pressed in preview") },
            onLogoutPressed = { Log.d("SettingsScreen", "Log Out pressed in preview") },
            onDeleteAccountPressed = { Log.d("SettingsScreen", "Delete Account pressed in preview") }
        )
    }
}
