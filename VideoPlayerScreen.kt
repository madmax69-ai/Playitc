package com.example.playit.ui.screens

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.example.playit.player.PlayerController
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity
    val view = LocalView.current
    val playerState by PlayerController.uiState.collectAsState()

    var controlsVisible by remember { mutableStateOf(true) }
    var lastInteraction by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var isDragging by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var seekFeedback by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose {
            view.keepScreenOn = false
            PlayerController.pause()
        }
    }

    LaunchedEffect(controlsVisible, lastInteraction) {
        if (controlsVisible) {
            delay(3500)
            if (System.currentTimeMillis() - lastInteraction >= 3400) {
                controlsVisible = false
            }
        }
    }

    LaunchedEffect(seekFeedback) {
        if (seekFeedback != null) {
            delay(600)
            seekFeedback = null
        }
    }

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        AndroidView(
            factory = {
                PlayerView(it).apply {
                    useController = false
                    setShutterBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = { it.player = PlayerController.getExoPlayer() },
            modifier = Modifier.fillMaxSize()
        )

        // Tap-to-toggle-controls + double-tap-to-seek layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            controlsVisible = !controlsVisible
                            lastInteraction = System.currentTimeMillis()
                        },
                        onDoubleTap = { offset: Offset ->
                            lastInteraction = System.currentTimeMillis()
                            if (offset.x < this@pointerInput.size.width / 2) {
                                PlayerController.seekBack()
                                seekFeedback = "-10s"
                            } else {
                                PlayerController.seekForward()
                                seekFeedback = "+10s"
                            }
                        }
                    )
                }
        )

        // Left half = brightness swipe, right half = volume swipe
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { change, dragAmount ->
                            change.consume()
                            activity?.window?.let { window ->
                                val attrs = window.attributes
                                val current = if (attrs.screenBrightness in 0f..1f) attrs.screenBrightness else 0.5f
                                val newValue = (current - dragAmount / 1000f).coerceIn(0.02f, 1f)
                                attrs.screenBrightness = newValue
                                window.attributes = attrs
                                seekFeedback = "Brightness ${(newValue * 100).toInt()}%"
                            }
                        }
                    }
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { change, dragAmount ->
                            change.consume()
                            val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                            val delta = (-dragAmount / 30f).toInt()
                            val newVolume = (current + delta).coerceIn(0, maxVolume)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
                            seekFeedback = "Volume ${(newVolume * 100 / maxVolume)}%"
                        }
                    }
            )
        }

        seekFeedback?.let {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(it, color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    playerState.title.ifBlank { "Video" },
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                )
                Box {
                    IconButton(onClick = { showSpeedMenu = true }) {
                        Icon(Icons.Filled.Speed, contentDescription = "Speed", tint = Color.White)
                    }
                    DropdownMenu(expanded = showSpeedMenu, onDismissRequest = { showSpeedMenu = false }) {
                        listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f).forEach { speed ->
                            DropdownMenuItem(
                                text = { Text("${speed}x") },
                                onClick = {
                                    PlayerController.setPlaybackSpeed(speed)
                                    showSpeedMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        formatDuration(if (isDragging) sliderPosition.toLong() else playerState.currentPositionMs),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Slider(
                        value = if (isDragging) sliderPosition else playerState.currentPositionMs.toFloat(),
                        onValueChange = {
                            isDragging = true
                            sliderPosition = it
                            lastInteraction = System.currentTimeMillis()
                        },
                        onValueChangeFinished = {
                            PlayerController.seekTo(sliderPosition.toLong())
                            isDragging = false
                        },
                        valueRange = 0f..(playerState.durationMs.coerceAtLeast(1L)).toFloat(),
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )
                    Text(formatDuration(playerState.durationMs), color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { PlayerController.skipPrevious() }) {
                        Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    IconButton(onClick = { PlayerController.seekBack() }) {
                        Icon(Icons.Filled.Replay10, contentDescription = "Back 10", tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                    IconButton(
                        onClick = { PlayerController.togglePlayPause() },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            if (playerState.isPlaying) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    IconButton(onClick = { PlayerController.seekForward() }) {
                        Icon(Icons.Filled.Forward10, contentDescription = "Forward 10", tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                    IconButton(onClick = { PlayerController.skipNext() }) {
                        Icon(Icons.Filled.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}
