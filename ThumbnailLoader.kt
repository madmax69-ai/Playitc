package com.example.playit.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun loadVideoThumbnail(context: Context, item: VideoItem): Bitmap? = withContext(Dispatchers.IO) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.loadThumbnail(item.uri, Size(400, 250), null)
        } else {
            @Suppress("DEPRECATION")
            ThumbnailUtils.createVideoThumbnail(item.path, MediaStore.Video.Thumbnails.MINI_KIND)
        }
    } catch (e: Exception) {
        null
    }
}

suspend fun loadAudioArt(context: Context, item: AudioItem): Bitmap? = withContext(Dispatchers.IO) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && item.albumArtUri != null) {
            context.contentResolver.loadThumbnail(item.albumArtUri, Size(300, 300), null)
        } else {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(item.path)
            val art = retriever.embeddedPicture
            retriever.release()
            art?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        }
    } catch (e: Exception) {
        null
    }
}
