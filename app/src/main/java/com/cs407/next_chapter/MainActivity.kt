package com.cs407.next_chapter

import SignupScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.cs407.next_chapter.OnboardingScreen
import com.cs407.next_chapter.OnboardingUtils
import androidx.compose.ui.Modifier
import com.cs407.next_chapter.ui.theme.Next_chapterTheme
import kotlinx.coroutines.launch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {

    private val onboardingUtils by lazy { OnboardingUtils(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        setContent {
            Next_chapterTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation(onboardingUtils)
                }
            }
        }
    }
}
@Composable
fun AppNavigation(onboardingUtils: OnboardingUtils) {
    //val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    var currentScreen by remember {
        mutableStateOf(
            if (onboardingUtils.isOnboardingCompleted()) "SigninScreen" else "OnboardingScreen"
        )
    }


    when (currentScreen) {
        "OnboardingScreen" -> {
            OnboardingScreen(onFinished = {
                onboardingUtils.setOnboardingCompleted()
                currentScreen = "SigninScreen"
            })
        }
        "SigninScreen" -> {
            LoginScreen(
                onSignUp = { currentScreen = "SignupScreen" },
                onLogin = { currentScreen = "HomeScreen" }
            )
        }
        "SignupScreen" -> {
            SignupScreen(
                modifier = Modifier.padding(16.dp),
                onBackClick = { currentScreen = "SigninScreen" },
                onSignupFinish = { currentScreen = "UploadBook" }
            )
        }
        "UploadBook" -> {
            NavigationGraph(navController = navController, startDestination = "upload")
//            UploadBookScreen(navController)
        }
        "HomeScreen" -> {
            NavigationGraph(navController = navController, startDestination = "home")
//            HomeScreen(navController)
        }
        else -> {
            NavigationGraph(navController = navController, startDestination = "home")
        }
    }
}
@Composable
fun NavigationGraph(navController: NavHostController,startDestination: String) {
    NavHost(navController, startDestination = startDestination) {
        composable("map") {
            MapScreen_Test(navController)
        }
        composable("chat") {
            ChatScreen_Test(navController)
        }
        composable("home") {
            HomeScreen(navController)
        }
        composable("upload") {
            UploadBookScreen(navController)
        }
        composable("profile") {
            UserProfileScreen(navController)
        }
    }
}













