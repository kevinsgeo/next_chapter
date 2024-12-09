package com.cs407.next_chapter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomNavigationItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun NavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavigationItem(
            route = "map",
            title = "Map",
            selectedIcon = ImageVector.vectorResource(R.drawable.baseline_map_24),
            unselectedIcon = ImageVector.vectorResource(R.drawable.outline_map_24)
        ),
        BottomNavigationItem(
            route = "chat",
            title = "Chat",
            selectedIcon = ImageVector.vectorResource(R.drawable.baseline_chat_24),
            unselectedIcon = ImageVector.vectorResource(R.drawable.outline_chat_24)
        ),
        BottomNavigationItem(
            route = "home",
            title = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        BottomNavigationItem(
            route = "scan_isbn", // Removed timestamp
            title = "Scan",
            selectedIcon = ImageVector.vectorResource(R.drawable.baseline_photo_camera_24),
            unselectedIcon = ImageVector.vectorResource(R.drawable.outline_photo_camera_24)
        ),
        BottomNavigationItem(
            route = "profile",
            title = "Profile",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person
        )
    )

    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry.value?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute?.startsWith(item.route) == true,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                label = {
                    Text(text = item.title)
                },
                alwaysShowLabel = false,
                icon = {
                    BadgedBox(
                        badge = {}
                    ) {
                        Icon(
                            imageVector = if (currentRoute?.startsWith(item.route) == true) {
                                item.selectedIcon
                            } else item.unselectedIcon,
                            contentDescription = item.title
                        )
                    }
                }
            )
        }
    }
}