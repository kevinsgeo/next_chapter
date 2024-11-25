package com.cs407.next_chapter

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(navController: NavHostController,
    onSettingsClick: () -> Unit = {
        Log.d("ProfileTopAppBar", "Settings clicked")
    }
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {NavigationBar(navController = navController)} ,
            topBar = {
                TopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = {}, // No title
                    actions = {
                        IconButton(onClick = { onSettingsClick() }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onPrimary // Icon color
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(10.dp),
                contentAlignment = Alignment.TopCenter // Align everything to the top-center
            )  {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally // Center items horizontally
                ) {
                    // Profile Icon with Border
                    Box(
                        contentAlignment = Alignment.Center, // Center the icon inside the circle
                        modifier = Modifier
                            .size(90.dp) // Circle size (includes border)
                            .border(
                                border = BorderStroke(2.dp, Color.Gray), // Use a visible border color
                                shape = CircleShape
                            )
                            .clip(CircleShape) // Ensures the circle shape
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile Icon",
                            tint = Color.Black,
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Username",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }



    }
}

//@Preview(showBackground = true)
//@Composable
//fun ProfileTopAppBarPreview() {
//    UserProfileScreen(navController =)
//}
