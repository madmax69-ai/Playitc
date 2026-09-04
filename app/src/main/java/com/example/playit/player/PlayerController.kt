package com.example.playit.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPercentage: Int = 0,
    val title: String = "",
    val subtitle: String = "",
    val isVideo: Boolean = true,
    val playbackSpeed: Float = 1f,
    val hasMedia: Boolean = false
)

/**
 * Singleton bridge between the Compose UI and the ExoPlayer instance that lives
 * inside [PlaybackService]. Connects via a MediaController so playback survives
 * navigation and continues in the background with a system notification.
 */
object PlayerController {

    private var controller: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val handler = Handler(Looper.getMainLooper())

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState

    private val positionUpdater = object : Runnable {
        override fun run() {
            controller?.let { c ->
                _uiState.value = _uiState.value.copy(
                    currentPositionMs = c.currentPosition.coerceAtLeast(0),
                    durationMs = c.duration.coerceAtLeast(0),
                    bufferedPercentage = c.bufferedPercentage,
                    isPlaying = c.isPlaying
                )
            }
            handler.postDelayed(this, 500)
        }
    }

    fun connect(context: Context, onReady: () -> Unit = {}) {
        if (controller != null) {
            onReady()
            return
        }
        val appContext = context.applicationContext
        val sessionToken = SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(appContext, sessionToken).buildAsync()
        controllerFuture?.addListener({
            controller = controllerFuture?.get()
            controller?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
                }
                override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                    _uiState.value = _uiState.value.copy(
                        title = mediaMetadata.title?.toString() ?: _uiState.value.title,
                        subtitle = mediaMetadata.artist?.toString() ?: _uiState.value.subtitle
                    )
                }
            })
            handler.post(positionUpdater)
            onReady()
        }, MoreExecutors.directExecutor())
    }

    /** Builds a playlist from the given items and starts playback at [startIndex]. */
    fun playPlaylist(items: List<Triple<Uri, String, String>>, startIndex: Int, isVideo: Boolean) {
        val mediaItems = items.map { (uri, title, subtitle) ->
            MediaItem.Builder()
                .setUri(uri)
                .setMediaMetadata(
                    MediaMetadata.Builder().setTitle(title).setArtist(subtitle).build()
                )
                .build()
        }
        controller?.setMediaItems(mediaItems, startIndex, 0L)
        controller?.prepare()
        controller?.play()
        val current = items.getOrNull(startIndex)
        _uiState.value = _uiState.value.copy(
            isVideo = isVideo,
            hasMedia = true,
            title = current?.second ?: _uiState.value.title,
            subtitle = current?.third ?: _uiState.value.subtitle
        )
    }

    fun togglePlayPause() {
        controller?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun pause() {
        controller?.pause()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
    }

    fun seekForward(ms: Long = 10_000) {
        controller?.let { it.seekTo((it.currentPosition + ms).coerceAtMost(it.duration.coerceAtLeast(0))) }
    }

    fun seekBack(ms: Long = 10_000) {
        controller?.let { it.seekTo((it.currentPosition - ms).coerceAtLeast(0)) }
    }

    fun skipNext() {
        controller?.seekToNextMediaItem()
    }

    fun skipPrevious() {
        controller?.seekToPreviousMediaItem()
    }

    fun setPlaybackSpeed(speed: Float) {
        controller?.setPlaybackSpeed(speed)
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun getExoPlayer(): Player? = controller

    fun stop() {
        controller?.stop()
        _uiState.value = _uiState.value.copy(hasMedia = false, isPlaying = false)
    }
}
