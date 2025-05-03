package com.keshav.drone.mymusic.offline

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer


class MusicViewModel(application: Application) : AndroidViewModel(application) {

    // Initialize ExoPlayer
    var exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build()

    var currentSong by mutableStateOf<Song?>(null)
    var currentSongIndex by mutableStateOf(0)
    val songs = mutableStateListOf<Song>()
    var current_thumbnail: String = ""


    init {
        // Adding listener to ExoPlayer
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_ENDED) {
                    playNext()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                // Handle error
            }
        })
    }

    // Play a song at the given index
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

    // Play selected song
    fun playSong(song: Song) {
        currentSong = song
        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(song.path)))
        exoPlayer.prepare()
        exoPlayer.play()

    }

    // Play the next song
    fun playNext() {

        val nextIndex = currentSongIndex + 1
        current_thumbnail = songs[nextIndex].thumbnail
        if (nextIndex < songs.size) {
            playSongAt(nextIndex)
        } else {
            currentSong = null
            exoPlayer.seekTo(0) // Optionally loop to start
            exoPlayer.pause()
        }
    }

    // Play the next song
    fun playPrevious() {
        val nextIndex = currentSongIndex - 1
        current_thumbnail = songs[nextIndex].thumbnail
        if (nextIndex < songs.size) {
            playSongAt(nextIndex)
        } else {
            currentSong = null
            exoPlayer.seekTo(0) // Optionally loop to start
            exoPlayer.pause()
        }
    }

    // Stop the player
    fun stopPlayer() {
        exoPlayer.stop()
        currentSong = null
    }

    // Release the ExoPlayer when ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }

    // Load songs from the context
    fun loadSongs(context: Context) {
        songs.clear()
        songs.addAll(getAllAudioFiles(context))
    }

    fun loadSongsOnline(context: Context) {
        songs.clear()
        songs.addAll(getAllAudioFiles(context))
    }
}
