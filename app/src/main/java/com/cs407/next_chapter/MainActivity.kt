package com.cs407.next_chapter


import MapScreen
import SignupScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cs407.next_chapter.ui.theme.Next_chapterTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private val onboardingUtils by lazy { OnboardingUtils(this) }
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        installSplashScreen()
        firebaseAuth = FirebaseAuth.getInstance()

        setContent {
            Next_chapterTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation(onboardingUtils, firebaseAuth)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(onboardingUtils: OnboardingUtils, firebaseAuth: FirebaseAuth) {
    val navController = rememberNavController()
    LaunchedEffect(Unit) {
        if (!onboardingUtils.isOnboardingCompleted()) {
            navController.navigate("OnboardingScreen") {
                onboardingUtils.setOnboardingCompleted()
                popUpTo(0) { inclusive = true }
            }
        } else {
            navController.navigate("SigninScreen") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavigationGraph(navController = navController, firebaseAuth = firebaseAuth)
}
@Composable
fun NavigationGraph(navController: NavHostController, firebaseAuth: FirebaseAuth) {
    NavHost(navController, startDestination = "OnboardingScreen") {
        composable("OnboardingScreen") {
            OnboardingScreen(onFinished = {
                // Mark onboarding as completed
                // Navigate to SigninScreen
                navController.navigate("SigninScreen") {
                    popUpTo("OnboardingScreen") { inclusive = true }
                }
            })
        }
        composable("SigninScreen") {
            LoginScreen(
                onSignUp = { navController.navigate("SignupScreen") },
                onLogin = { navController.navigate("home") },
                firebaseAuth = firebaseAuth
            )
        }
        composable("SignupScreen") {
            SignupScreen(
                modifier = Modifier.padding(16.dp),
                onBackClick = { navController.navigateUp() },
                firebaseAuth = firebaseAuth,
                navController = navController,
            )
        }
        composable("scan_isbn") { // Unified route for the camera button
            ScanISBNScreen(navController)
        }
        composable("home") {
            HomeScreen(navController)
        }
        composable("map") {
            MapScreen(navController)
        }
        composable("chat") {
            ChatScreen_Test(navController)
        }
        composable("profile") {
            UserProfileScreen(navController)
        }
        composable(
            "bookInfo/{scannedISBN}",
            arguments = listOf(navArgument("scannedISBN") { type = NavType.StringType })
        ) { backStackEntry ->
            val scannedISBN = backStackEntry.arguments?.getString("scannedISBN")
            BookInfoScreen(navController, scannedISBN)
        }
        composable(
            route = "book_info/{isbn}",
            arguments = listOf(navArgument("isbn") { type = NavType.StringType })
        ) { backStackEntry ->
            val isbn = backStackEntry.arguments?.getString("isbn")
            BookInfoOnClick(navController = navController, scannedISBN = isbn)
        }

        composable("settings") {
            SettingsScreen(onBackPressed = { navController.navigate("profile") },
                navController = navController)
        }

        composable(
            route = "WishlistInfoScreen/{scannedISBN}",
            arguments = listOf(navArgument("scannedISBN") { type = NavType.StringType })
        ) { backStackEntry ->
            val scannedISBN = backStackEntry.arguments?.getString("scannedISBN")
            WishlistScreenInfoScreen(navController = navController, scannedISBN = scannedISBN)
        }

    }
}