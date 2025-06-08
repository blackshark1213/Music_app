package com.keshav.drone.mymusic.offline

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun PlayerUI(
    navController: NavController,
    songPath: String,
    exoPlayer: ExoPlayer,
    VM: MusicViewModel
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableFloatStateOf(0f) }
    var songDuration by remember { mutableFloatStateOf(1f) }

    isPlaying = VM.exoPlayer.isPlaying

    // Animate title offset
    val animatedOffset = remember { Animatable(-300f) }

    // Track song duration and playback
    LaunchedEffect(songPath) {
        VM.exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY && VM.exoPlayer.duration > 0) {
                    songDuration = VM.exoPlayer.duration.toFloat() / 1000f
                }
            }
        })

        VM.exoPlayer.playWhenReady = true

        while (true) {
            if (VM.exoPlayer.isPlaying && VM.exoPlayer.duration > 0) {
                currentPosition = VM.exoPlayer.currentPosition.toFloat() / 1000f
                songDuration = VM.exoPlayer.duration.toFloat() / 1000f
            }
            delay(500)
        }
    }

    LaunchedEffect(Unit) {
        animatedOffset.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
//        .pointerInput(Unit) {
//        detectHorizontalDragGestures { _, dragAmount ->
//            if (dragAmount < -100) {
//                VM.playNext()
//            } else if (dragAmount > 100) {
//                VM.playPrevious()
//            }
//        }
//    },
        contentAlignment = Alignment.BottomEnd
    ) {
        // Background with blur
        Song_thumbnail(VM, 300)

        // Rotating disk and thumbnail
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isPlaying) RotatingDiskImage() else RotatingDiskImage(90000)

            Box(
                modifier = Modifier
                    .size(300.dp)
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Song_thumbnail(VM, 0)
            }
        }

        // Controls Column
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Song Title
            VM.currentSong?.let {
                Box(
                    modifier = Modifier
                        .offset(y = animatedOffset.value.dp)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .offset(y = animatedOffset.value.dp)
                            .fillMaxWidth()
//                            .background(
//                                brush = Brush.verticalGradient(
//                                    colors = listOf(Color(0xFF1DB954), Color.Gray)
//                                ))
                            .padding(12.dp),
                        contentAlignment = Alignment.CenterStart
                    ){
                    }
                    Column (
                       modifier = Modifier
                           .fillMaxWidth()
                           .padding(5.dp)
                    ){
                        MarqueeText(it.title, Color.White , 20 )
                        MarqueeText(it.artist, Color.Green , 15 )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Like button
            var color by remember { mutableStateOf(Color.White) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "♡",
                    color = color,
                    fontSize = 30.sp,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            color = if (color == Color.White) Color.Green else Color.White
                        }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Slider
            SongSlider(currentPosition,songDuration , onSeek = { VM.exoPlayer.seekTo(it)} )

            // Time indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatTime(currentPosition), color = Color.White)
                Text(text = formatTime(songDuration), color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Playback controls
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { VM.playPrevious() }) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", tint = Color.White)
                }

                IconButton(onClick = {
                    if (isPlaying) VM.exoPlayer.pause() else VM.exoPlayer.play()
                    isPlaying = !isPlaying
                }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }

                IconButton(onClick = { VM.playNext() }) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Next", tint = Color.White)
                }
            }
        }
    }
    // Top Bar
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1DB954))
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            .shadow(elevation = 4.dp)
    ) {
        TopBarWithImageAndSearch(
            searchText = "",
            onSearchTextChange = { },
            navController = navController,
            MV = VM
        )
    }
}

//Slider
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongSlider(currentPosition: Float, songDuration: Float, onSeek: (Long) -> Unit) {
    val safeDuration = songDuration.takeIf { it > 0f && it.isFinite() } ?: 1f

    Slider(
        value = currentPosition.coerceIn(0f, safeDuration),
        onValueChange = { onSeek((it * 1000).toLong()) },
        valueRange = 0f..safeDuration,
        colors = SliderDefaults.colors(
            thumbColor = Color.Transparent,
            activeTrackColor = Color.Green,
            inactiveTrackColor = Color.Gray
        ),
        thumb = {
            // Custom slim vertical line as thumb
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(50.dp)
                    .background(Color.Green, RoundedCornerShape(1.dp))
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    )
}

// find the song time
@SuppressLint("DefaultLocale")
fun formatTime(timeInSeconds: Float): String {
    val totalSeconds = timeInSeconds.toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}


// used to load song image
@Composable
fun Song_thumbnail(VM: MusicViewModel,blur:Int) {
    val currentThumbnail = VM.current_thumbnail

    if (currentThumbnail == null) {
        Image(
            painter = painterResource(R.drawable.music_default),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .shadow(8.dp)
                .blur(blur.dp),
        )
    }
        else
        {
            Image(
                painter = rememberAsyncImagePainter(
                    model = VM.current_thumbnail,
                ),
                contentDescription = "Album Art",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .shadow(8.dp)
                    .blur(blur.dp),
                contentScale = ContentScale.Crop
            )
        }
}
// Liked UI

@Composable
fun LikedSongsUI(viewModel: MusicViewModel) {
    val isDark = isSystemInDarkTheme()
    val background = if (isDark) Color.Black else Color.White
    val textColor = if (isDark) Color.White else Color.Black
    val likedSongs: List<Song> = viewModel.songs

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 130.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(likedSongs) { song ->
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable {
                        viewModel.playSong(song)
                    }
            ) {
                Column(
                    modifier = Modifier
                        .background(Color(0xFF1DB954).copy(alpha = 0.1f))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = song.thumbnail,
                        contentDescription = "Thumbnail",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = textColor
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}


