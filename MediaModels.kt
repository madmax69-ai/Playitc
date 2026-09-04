package com.example.playit.data

import android.net.Uri

data class VideoItem(
    val id: Long,
    val title: String,
    val uri: Uri,
    val duration: Long,
    val size: Long,
    val path: String,
    val dateAdded: Long
)

data class AudioItem(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val uri: Uri,
    val albumArtUri: Uri?,
    val duration: Long,
    val path: String
)
