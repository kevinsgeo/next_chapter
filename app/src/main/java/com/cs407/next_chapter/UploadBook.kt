package com.cs407.next_chapter

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun UploadBookScreen(navController: NavHostController) {
    MaterialTheme {
        Scaffold(
            bottomBar = { NavigationBar(navController = navController) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // Apply padding to respect the NavigationBar
                    .padding(horizontal = 16.dp, vertical = 32.dp), // Additional content padding
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp)) // Space from the top

                // Button to scan the book
                Button(
                    onClick = { /* TODO: Handle scan book */ },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp) // Optional: Fixed height for consistency
                ) {
                    Text(text = "Scan the Book", color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp)) // Spacer between buttons

                // Button to scan the ISBN barcode
                Button(
                    onClick = { /* TODO: Handle scan ISBN barcode */ },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp) // Optional: Fixed height for consistency
                ) {
                    Text(text = "Scan ISBN Barcode", color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUploadBookScreen() {
    //UploadBookScreen()
}
