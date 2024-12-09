package com.cs407.next_chapter

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.cs407.next_chapter.ui.theme.Next_chapterTheme // Adjust the import based on your theme
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController

@Composable
fun UploadBookScreen(navController: NavHostController) {
    MaterialTheme {
        Scaffold(
            bottomBar = { NavigationBar(navController = navController) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Background Image
                Image(
                    painter = painterResource(id = R.drawable.nextchapterlogo),
                    contentDescription = "Background Image",
                    modifier = Modifier.offset(100.dp, 300.dp),
                    contentScale = ContentScale.Crop
                )

                // Content Layer
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp)) // Space from the top

                    // Button to scan the book
                    Button(
                        onClick = { navController.navigate("upload_photo") },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp), // Optional: Fixed height for consistency
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(text = "Scan the Book", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(16.dp)) // Spacer between buttons

                    // Button to scan the ISBN barcode
                    Button(
                        onClick = { navController.navigate("scan_isbn") },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp), // Optional: Fixed height for consistency
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(text = "Scan ISBN Barcode", color = Color.White)
                    }

                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUploadBookScreen() {
    Next_chapterTheme { // Ensure you wrap with your app's theme
        UploadBookScreen(navController = rememberNavController())
    }
}
