package com.cs407.next_chapter

import android.app.Application
import android.util.Log
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory

class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Stream Chat plugins
        val offlinePluginFactory = StreamOfflinePluginFactory(appContext = this)
        val statePluginFactory = StreamStatePluginFactory(config = StatePluginConfig(), appContext = this)

        // Initialize ChatClient
        ChatClient.Builder(getString(R.string.api_key), this)
            .withPlugins(offlinePluginFactory, statePluginFactory)
            .logLevel(ChatLogLevel.ALL)
            .build()

        Log.d("MyApplication", "ChatClient initialized successfully")
    }
}
