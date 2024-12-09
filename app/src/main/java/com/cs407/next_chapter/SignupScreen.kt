import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.navigation.NavController

@Composable
fun SignupScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    navController: NavController,
    firebaseAuth: FirebaseAuth
) {
    var mobileNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var homeAddress by remember { mutableStateOf("")  }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { onBackClick() }) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_revert),
                    contentDescription = "Back"
                )
            }
        }

        Text(
            text = "NextChapter",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 30.dp)
        )



        Text(
            text = "Sign up to see book swaps near you",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 26.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Full Name",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )

        TextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Email Address",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Username",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Mobile Number",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )

        TextField(
            value = mobileNumber,
            onValueChange = { mobileNumber = it },
            label = { Text("Mobile Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Home Address",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )


        TextField(
            value = homeAddress,
            onValueChange = { homeAddress = it },
            label = { Text("Home Address (Street, City, State, Country)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank() && fullName.isNotBlank()) {
                    if (password.length < 6) {
                        Toast.makeText(context, "Password should be at least 6 characters long", Toast.LENGTH_SHORT).show()
                        return@Button // Exit early if password is invalid
                    }
                    // Firebase Authentication
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Fetch user ID
                                val uid = firebaseAuth.currentUser?.uid
                                if (uid != null) {
                                    // Prepare data for Realtime Database
                                    val userData = mapOf(
                                        "uid" to uid,
                                        "email" to email,
                                        "name" to fullName,
                                        "username" to username,
                                        "password" to password,
                                        "mobileNumber" to mobileNumber,
                                        "homeAddress" to homeAddress
                                    )

                                    // Save data to Realtime Database
                                    val databaseRef = com.google.firebase.database.FirebaseDatabase.getInstance()
                                        .getReference("Users")

                                    databaseRef.child(uid).setValue(userData)
                                        .addOnSuccessListener {
                                            // Success - Navigate to next screen
                                            Log.d("Signup", "User data saved successfully!")
                                            navController.navigate("scan_isbn")
                                        }
                                        .addOnFailureListener { error ->
                                            // Failure - Log error
                                            Log.e("Signup", "Failed to save user data: ${error.message}")
                                        }
                                } else {
                                    Log.e("Signup", "Failed to fetch user ID:")
                                }
                            } else {
                                // Check the exception to determine if it's an invalid email error
                                val exception = task.exception
                                when (exception) {
                                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                                        if (exception.errorCode == "ERROR_INVALID_EMAIL") {
                                            Toast.makeText(context, "Invalid email format", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    is com.google.firebase.auth.FirebaseAuthUserCollisionException -> {
                                        Toast.makeText(context, "Email is already in use", Toast.LENGTH_SHORT).show()
                                    }
                                    else -> {
                                        // General error handling
                                        Toast.makeText(
                                            context,
                                            "Failed to create user: ${exception?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                Log.e("Signup", "Failed to create user: ${exception?.message}")
                            }
                        }
                } else {
                    Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Sign Up")
        }

    }
}

