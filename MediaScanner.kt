package com.example.playit.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("DEPRECATION")
object MediaScanner {

    suspend fun queryVideos(context: Context): List<VideoItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<VideoItem>()
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED
        )
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"
        context.contentResolver.query(collection, projection, null, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                list.add(
                    VideoItem(
                        id = id,
                        title = cursor.getString(nameCol) ?: "Unknown",
                        uri = uri,
                        duration = cursor.getLong(durCol),
                        size = cursor.getLong(sizeCol),
                        path = cursor.getString(dataCol) ?: "",
                        dateAdded = cursor.getLong(dateCol)
                    )
                )
            }
        }
        list
    }

    suspend fun queryAudio(context: Context): List<AudioItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<AudioItem>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        context.contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumArtBase = Uri.parse("content://media/external/audio/albumart")
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                val albumId = cursor.getLong(albumIdCol)
                val artUri = ContentUris.withAppendedId(albumArtBase, albumId)
                list.add(
                    AudioItem(
                        id = id,
                        title = cursor.getString(titleCol) ?: "Unknown",
                        artist = cursor.getString(artistCol) ?: "Unknown Artist",
                        album = cursor.getString(albumCol) ?: "",
                        uri = uri,
                        albumArtUri = artUri,
                        duration = cursor.getLong(durCol),
                        path = cursor.getString(dataCol) ?: ""
                    )
                )
            }
        }
        list
    }
}
