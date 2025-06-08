package com.keshav.drone.mymusic.offline

import android.app.Application
import android.content.Context
import android.media.audiofx.Equalizer
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

@OptIn(UnstableApi::class)
class MusicViewModel (application: Application) : AndroidViewModel(application) {

    // ExoPlayer
    var exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build()

    // Song state
    var currentSong by mutableStateOf<Song?>(null)
    var currentSongIndex by mutableStateOf(0)
    val songs = mutableStateListOf<Song>()
    var current_thumbnail: String? = null
    var skipIndex = mutableStateListOf<Int>()


    var bassLevel by   mutableStateOf(0)
    var trebleLevel by  mutableStateOf(0)
    var midLevel by mutableStateOf(0)
    var lowmidLevel by mutableStateOf(0)
    var highmidLevel by  mutableStateOf(0)

    var isch by mutableStateOf(false)

    // Equalizer
    private var _equalizer: Equalizer? = null
    private val _audioSessionId = mutableStateOf(0)
    val audioSessionId: State<Int> get() = _audioSessionId
    private val _isEqualizerEnabled = mutableStateOf(false)
    val isEqualizerEnabled: State<Boolean> get() = _isEqualizerEnabled

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) playNext()
            }

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                setAudioSessionId(audioSessionId)
            }

            override fun onPlayerError(error: PlaybackException) {
                // Handle player error
            }
        })
    }

    fun setAudioSessionId(id: Int) {
        _audioSessionId.value = id
        setupEqualizer(id)
    }

    private fun setupEqualizer(sessionId: Int) {
        _equalizer?.release()
        _equalizer = Equalizer(0, sessionId).apply {
            enabled = true
        }
        _isEqualizerEnabled.value = true
    }

    fun toggleEqualizer() {
        _equalizer?.let {
            val newState = !it.enabled
            it.enabled = newState
            _isEqualizerEnabled.value = newState
        }
    }

    fun setBandLevel(band: Short, level: Short) {
        _equalizer?.setBandLevel(band, level)
    }

    fun playSongAt(index: Int) {
        if (index in songs.indices) {
            val song = songs[index]
            currentSong = song
            currentSongIndex = index
            exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(song.path)))
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    fun playSong(song: Song) {
        currentSong = song
        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(song.path)))
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun playNext() {
        var nextIndex = currentSongIndex + 1
        while (nextIndex in skipIndex && nextIndex < songs.size) nextIndex++
        if (nextIndex == songs.size) return

        current_thumbnail = songs[nextIndex].thumbnail
        if (nextIndex < songs.size) playSongAt(nextIndex)
        else {
            currentSong = null
            exoPlayer.seekTo(0)
            exoPlayer.pause()
        }
    }

    fun playPrevious() {
        var nextIndex = currentSongIndex - 1
        while (nextIndex in skipIndex && nextIndex >= 0) nextIndex--
        if (nextIndex < 0) return

        current_thumbnail = songs[nextIndex].thumbnail
        playSongAt(nextIndex)
    }

    fun stopPlayer() {
        exoPlayer.stop()
        currentSong = null
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
        _equalizer?.release()
    }

    fun loadSongs(context: Context) {
        songs.clear()
        songs.addAll(getAllAudioFiles(context))
    }
}
