package com.example.playit.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.playit.player.PlayerController
import com.example.playit.ui.components.MiniPlayer
import com.example.playit.viewmodel.MediaViewModel

@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: MediaViewModel = viewModel()
    var selectedTab by remember { mutableIntStateOf(0) }
    val playerState by PlayerController.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadMedia() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("PlayIt") }) },
        bottomBar = {
            if (playerState.hasMedia) {
                MiniPlayer(
                    state = playerState,
                    onClick = {
                        navController.navigate(if (playerState.isVideo) "video_player" else "audio_player")
                    }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Videos") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Audio") })
            }
            val isLoading by viewModel.isLoading.collectAsState()
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    0 -> {
                        val videos by viewModel.videos.collectAsState()
                        VideoListScreen(videos = videos) { index ->
                            PlayerController.playPlaylist(
                                videos.map { Triple(it.uri, it.title, "") },
                                index,
                                isVideo = true
                            )
                            navController.navigate("video_player")
                        }
                    }
                    1 -> {
                        val audio by viewModel.audio.collectAsState()
                        AudioListScreen(audio = audio) { index ->
                            PlayerController.playPlaylist(
                                audio.map { Triple(it.uri, it.title, it.artist) },
                                index,
                                isVideo = false
                            )
                            navController.navigate("audio_player")
                        }
                    }
                }
            }
        }
    }
}
