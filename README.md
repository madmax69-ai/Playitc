# PlayIt Clone — Native Android Video & Audio Player

A modern, Material 3, Jetpack Compose video/audio player for Android, built on
**Media3 ExoPlayer**. It scans your device for local videos and music (like Playit),
and gives you a gesture-driven player: tap to show/hide controls, double-tap left/right
to seek ±10s, swipe up/down on the left half for brightness and the right half for
volume, plus a background audio service with a system notification (play/pause/skip
work from the lock screen and notification shade).

## Features
- Home screen with **Videos** / **Audio** tabs (MediaStore-backed library, no internet needed)
- Grid thumbnails for video, album art for audio (auto-generated, cached in memory)
- Full-screen video player: custom Compose overlay controls, auto-hide, playback
  speed menu (0.5x–2x), seek bar with scrubbing, skip next/prev across the playlist
- Gesture controls: double-tap seek, swipe-to-adjust brightness/volume, tap to
  toggle the control overlay
- Full-screen "Now Playing" audio player with seek bar and transport controls
- **Background playback**: a `MediaSessionService` keeps audio (and video's audio
  track) playing after you leave the player, with a real notification and lock-screen
  controls, powered by Media3's `MediaSession`/`MediaController`
- Mini-player bar on the library screen while something is playing
- Runtime permission handling for Android 13+ (`READ_MEDIA_VIDEO` / `READ_MEDIA_AUDIO`)
  and legacy `READ_EXTERNAL_STORAGE` on older versions
- Dark, cinematic Material 3 theme

## Project structure
```
app/src/main/java/com/example/playit/
├── MainActivity.kt              # permission gate + NavHost
├── data/
│   ├── MediaModels.kt           # VideoItem / AudioItem
│   ├── MediaScanner.kt          # MediaStore queries
│   └── ThumbnailLoader.kt       # video thumbnail / album art loading
├── player/
│   ├── PlaybackService.kt       # MediaSessionService hosting the ExoPlayer
│   └── PlayerController.kt      # singleton bridge: Compose UI <-> MediaController
├── viewmodel/
│   └── MediaViewModel.kt        # loads video/audio lists
└── ui/
    ├── theme/                   # Color.kt, Type.kt, Theme.kt
    ├── components/MiniPlayer.kt
    └── screens/
        ├── HomeScreen.kt
        ├── VideoListScreen.kt
        ├── AudioListScreen.kt
        ├── VideoPlayerScreen.kt # gesture-driven full-screen video player
        └── AudioPlayerScreen.kt # full-screen now-playing audio UI
```

## How to build and run
1. **Open in Android Studio** (Koala/2024.1 or newer recommended) → *Open* →
   select the `PlayItClone` folder.
2. Let Gradle sync. If Android Studio prompts that the Gradle wrapper jar is
   missing, click **"OK" / "Generate wrapper"** — it will fetch it automatically
   (this project ships `gradle-wrapper.properties` pointing at Gradle 8.7, but not
   the binary wrapper jar itself, since that's a binary file). Alternatively, run
   `gradle wrapper` once from a terminal if you have Gradle installed locally.
3. Plug in a device (or start an emulator) running **Android 7.0 (API 24) or higher**,
   with some local video/audio files on it (or use the emulator's sample media —
   push files with `adb push myvideo.mp4 /sdcard/Movies/`).
4. Click **Run ▶**. On first launch, grant the media permission when prompted.
5. Browse the **Videos** / **Audio** tabs, tap anything to start playback.

## Notes / things you can extend
- Thumbnails are generated on the fly via `ContentResolver.loadThumbnail` (API 29+)
  or `ThumbnailUtils` / `MediaMetadataRetriever` as a fallback — for very large
  libraries you'd want to cache these (e.g. with Coil or a disk cache).
- The player currently streams from local `content://` URIs only; swap the
  `MediaItem.Builder().setUri(...)` calls in `PlayerController` for network URLs to
  add streaming support.
- No persistence for "recently played" / favorites yet — would be a good next step
  with a small Room database.
- App icon uses the system default since no image assets were generated; drop your
  own `mipmap-*/ic_launcher.png` files in `res/` and reference them from the
  manifest's `android:icon` attribute if you want custom branding.
