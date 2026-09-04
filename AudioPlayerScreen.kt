package com.example.playit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.playit.player.PlayerController

@Composable
fun AudioPlayerScreen(navController: NavController) {
    val playerState by PlayerController.uiState.collectAsState()
    var isDragging by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.background)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Close")
                }
                Spacer(Modifier.weight(1f))
                Text("Now Playing", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(48.dp))
            }

            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                playerState.title.ifBlank { "Unknown Title" },
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Text(
                playerState.subtitle.ifBlank { "Unknown Artist" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Slider(
                value = if (isDragging) sliderPosition else playerState.currentPositionMs.toFloat(),
                onValueChange = { isDragging = true; sliderPosition = it },
                onValueChangeFinished = {
                    PlayerController.seekTo(sliderPosition.toLong())
                    isDragging = false
                },
                valueRange = 0f..(playerState.durationMs.coerceAtLeast(1L)).toFloat()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    formatDuration(if (isDragging) sliderPosition.toLong() else playerState.currentPositionMs),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(formatDuration(playerState.durationMs), style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { PlayerController.skipPrevious() }, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(40.dp))
                }
                Spacer(Modifier.width(16.dp))
                FilledIconButton(
                    onClick = { PlayerController.togglePlayPause() },
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                IconButton(onClick = { PlayerController.skipNext() }, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Next", modifier = Modifier.size(40.dp))
                }
            }
        }
    }
}
