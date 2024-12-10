package com.cs407.next_chapter

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.cs407.next_chapter.ui.theme.Next_chapterTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.User

@Composable
fun LoginScreen(onLogin: () -> Unit = {}, onSignUp: () -> Unit = {}, firebaseAuth: FirebaseAuth, ) {
    var emailAddress by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(170.dp))

        @Composable
        fun FadeText() {
            // Animatable for controlling alpha
            val alpha = remember { Animatable(0f) }

            // Launch the animation for a single fade-in
            LaunchedEffect(Unit) {
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
                )
            }

            // Display the text with the animated alpha
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NextChapter",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Cursive, // Built-in cursive font
                        color = MaterialTheme.colorScheme.primary.copy(alpha = alpha.value)
                    ),
                    fontSize = 36.sp
                )
            }
        }


        FadeText()



        Text(
            text = "Email Address",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )

        TextField(
            value = emailAddress,
            onValueChange = { emailAddress = it },
            label = { Text("Email Address") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = "Password",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {

                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(44.dp))

        Button(
            onClick = {
                if (emailAddress.isNotBlank() && password.isNotBlank()) {
                    firebaseAuth.signInWithEmailAndPassword(emailAddress, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Login successful
                                val firebaseUser = firebaseAuth.currentUser
                                if (firebaseUser != null) {
                                    connectChatUser(firebaseUser)
                                }
                                Log.d("Login", "Login successful")
                                Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT)
                                    .show()
                                onLogin()
                            } else {
                                // Login failed
                                Log.e("Login", "Login failed: ${task.exception?.message}")
                                Toast.makeText(
                                    context,
                                    "Login Failed: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("Login")
        }



        Spacer(modifier = Modifier.height(45.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center // Centers content horizontally
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally // Aligns children horizontally to the center
                ) {
                    Text(
                        text = "Don't have an account?",
                        style = MaterialTheme.typography.bodySmall
                    )

                    TextButton(onClick = onSignUp) {
                        Text(
                            text = "Sign up",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                    }
                }
            }

        }
    }
}
fun connectChatUser(firebaseUser: FirebaseUser) {
    val client = ChatClient.instance()
    val user = User(
        id = firebaseUser.uid,
        extraData = mutableMapOf(
            "name" to (firebaseUser.displayName ?: "Guest"),
            "image" to (firebaseUser.photoUrl?.toString() ?: "https://bit.ly/2TIt8NR")
        )
    )
    val token = client.devToken(user.id) // Replace with a real token in production
    client.connectUser(user = user, token = token).enqueue { result ->
        result.onSuccess {
            Log.d("ChatInitialization", "User connected successfully.")
        }.onError { error ->
            Log.e("ChatInitialization", "Error connecting user: ${error.message}")
        }
    }
}

