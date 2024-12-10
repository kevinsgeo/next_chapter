
package com.cs407.next_chapter

import android.annotation.SuppressLint
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
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channels.ChannelViewModelFactory
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.User
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory

@Composable
/*
fun ChatScreen_Test(navController: NavHostController) {
    val client = ChatClient.instance()
    val clientInitializationState by client.clientState.initializationState.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val context = LocalContext.current

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
                                val otherUserName = channel.members
                                    .firstOrNull { it.user.id != currentUserId }
                                    ?.user
                                    ?.name
                                    ?: "Chat"

                                context.startActivity(
                                    ChannelActivity.getIntent(context, channel.cid).apply { putExtra("channelName", otherUserName) }
                                )
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

 */
fun ChatScreen_Test(navController: NavHostController) {
    val client = ChatClient.instance()
    val clientInitializationState by client.clientState.initializationState.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val context = LocalContext.current

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
                                val otherUser = channel.members
                                    .firstOrNull { it.user.id != currentUserId }

                                val otherUserName = otherUser?.user?.name ?: "Unknown User"
                                context.startActivity(
                                    ChannelActivity.getIntent(context, channel.cid).apply {
                                        putExtra("channelName", otherUserName)
                                    }
                                )
                            },
                            viewModelFactory = ChannelViewModelFactory(),
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


