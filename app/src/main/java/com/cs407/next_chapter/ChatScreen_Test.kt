
package com.cs407.next_chapter

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.User
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory

@Composable
fun ChatScreen_Test(navController: NavHostController) {
    val context = LocalContext.current

    // Setup Stream Chat plugins
    val offlinePluginFactory = remember {
        StreamOfflinePluginFactory(appContext = context)
    }
    val statePluginFactory = remember {
        StreamStatePluginFactory(config = StatePluginConfig(), appContext = context)
    }

    // Initialize the Stream Chat client
    val client = remember {
        ChatClient.Builder(context.getString(R.string.api_key), context)
            .withPlugins(offlinePluginFactory, statePluginFactory)
            .logLevel(ChatLogLevel.ALL) // Adjust for production
            .build()
    }

    // Authenticate and connect the user
    LaunchedEffect(client) {
        val user = User(
            id = "nextchapter407",
            extraData = mutableMapOf(
                "name" to "Tutorial Droid",
                "image" to "https://bit.ly/2TIt8NR"
            )
        )
        val token = client.devToken(user.id)
        Log.d("ChatScreen_Test", "Token: $token")
        client.connectUser(user = user, token = token).enqueue { result ->
            result.onSuccess {
                Log.d("ChatScreen_Test", "User connected successfully.")
            }.onError { error ->
                Log.e("ChatScreen_Test", "Error connecting user: ${error.message}")
            }
        }
    }

    // Observe the initialization state
    val clientInitializationState by client.clientState.initializationState.collectAsState()

    Scaffold(
        bottomBar = { NavigationBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ChatTheme {
                when (clientInitializationState) {
                    InitializationState.COMPLETE -> {
                        ChannelsScreen(
                            title = stringResource(id = R.string.app_name),
                            isShowingHeader = true,
                            onChannelClick = { channel ->
                                context.startActivity(ChannelActivity.getIntent(context, channel.cid))
                            },
                            onBackPressed = { navController.popBackStack() }
                        )
                    }
                    InitializationState.INITIALIZING -> {
                        Text(text = "Initializing...")
                    }
                    InitializationState.NOT_INITIALIZED -> {
                        Text(text = "Not initialized...")
                    }
                }
            }
        }
    }
}

/*
package com.cs407.next_chapter

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.User
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory
@Composable
fun ChatScreen_Test(navController: NavHostController) {
    val context = LocalContext.current

    // Firebase authentication user
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val chatClient = remember {
        ChatClient.Builder("STREAM_API_KEY", context)
            .logLevel(ChatLogLevel.ALL)
            .apply {
                val offlinePlugin = StreamOfflinePluginFactory(context)
                val statePlugin = StreamStatePluginFactory(StatePluginConfig())
                addPlugins(listOf(offlinePlugin, statePlugin))
            }
            .build()
    }

    var isStreamUserInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            val userId = it.uid
            val userName = it.displayName ?: "User$userId"
            val userToken = generateStreamToken(userId) // Replace with your token generation logic

            // Initialize Stream user
            chatClient.connectUser(
                User(id = userId, name = userName),
                userToken
            ).enqueue { result ->
                if (result.isSuccess) {
                    isStreamUserInitialized = true
                } else {
                    Log.e("ChatScreen_Test", "Error connecting Stream user: ${result.error().message}")
                }
            }
        }
    }

    Scaffold(
        bottomBar = { NavigationBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isStreamUserInitialized) {
                ChatTheme {
                    ChannelsScreen(
                        onItemClick = { channel ->
                            navController.navigate("chat/${channel.cid}")
                        }
                    )
                }
            } else {
                Text("Loading chats...", modifier = Modifier.fillMaxSize())
            }
        }
    }
}
 */

