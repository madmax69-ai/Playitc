package com.example.playit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.playit.data.AudioItem
import com.example.playit.data.MediaScanner
import com.example.playit.data.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MediaViewModel(app: Application) : AndroidViewModel(app) {

    private val _videos = MutableStateFlow<List<VideoItem>>(emptyList())
    val videos: StateFlow<List<VideoItem>> = _videos

    private val _audio = MutableStateFlow<List<AudioItem>>(emptyList())
    val audio: StateFlow<List<AudioItem>> = _audio

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var loaded = false

    fun loadMedia() {
        if (loaded) return
        loaded = true
        viewModelScope.launch {
            _isLoading.value = true
            _videos.value = MediaScanner.queryVideos(getApplication())
            _audio.value = MediaScanner.queryAudio(getApplication())
            _isLoading.value = false
        }
    }
}
