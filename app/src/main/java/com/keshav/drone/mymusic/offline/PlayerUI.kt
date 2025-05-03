package com.keshav.drone.mymusic.offline

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.*
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage


@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun PlayerUI(
    navController: NavController,
    songPath: String,
    exoPlayer: ExoPlayer,
    VM: MusicViewModel,
    // thumbnail : String?,
) {
//    val isPlaying by rememberUpdatedState(newValue = VM.exoPlayer.isPlaying)
    var isPlaying by remember { mutableStateOf(false) }
    isPlaying = VM.exoPlayer.isPlaying

    var currentPosition by remember { mutableFloatStateOf(0f) }
    //currentPosition = VM.exoPlayer.currentPosition.toFloat()

    var songDuration by remember { mutableFloatStateOf(1f) }

    // Prepare and observe the player

    LaunchedEffect(songPath) {
        VM.exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY && VM.exoPlayer.duration > 0) {
                    songDuration = VM.exoPlayer.duration.toFloat() / 1000f
                }
            }
        })

        VM.exoPlayer.playWhenReady = true // optional autoplay

        while (true) {
            if (VM.exoPlayer.isPlaying && VM.exoPlayer.duration > 0) {
                currentPosition = VM.exoPlayer.currentPosition.toFloat() / 1000f
                songDuration = VM.exoPlayer.duration.toFloat() / 1000f
            }
            delay(500) // smooth updates
        }
    }
    val animatedOffset = remember { Animatable(-300f) }

    LaunchedEffect(Unit) {
        animatedOffset.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    DisposableEffect(Unit) {
        onDispose {
//            VM.exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorResource(id = R.color.PlayerBack),
                        Color.Blue.copy(alpha = 0.3f)
                    )
                )
            ),
        contentAlignment = Alignment.BottomEnd
    ){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center // This centers the content (Image) inside the Box
        ) {

            Image(
                painter = rememberAsyncImagePainter(
                    model = VM.current_thumbnail,
                    placeholder = painterResource(id = R.drawable.disk)
                ),
                contentDescription = "Album Art",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .shadow(8.dp)
                    .blur(12.dp)
                    .background(Color.DarkGray),
                contentScale = ContentScale.Crop
            )

            if (isPlaying) RotatingDiskImage()
            else RotatingDiskImage(90000)
            Box (
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ){
                Image(
                    painter = rememberAsyncImagePainter(
                        model = VM.current_thumbnail,
                        placeholder = painterResource(id = R.drawable.disk)
                    ),
                    contentDescription = "Album Art",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .shadow(8.dp)
                        .border(5.dp, Color.White)
                        .background(Color.DarkGray),
                    contentScale = ContentScale.Crop
                )
            }


        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(18.dp))
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
//                    Text(
//                        text = (it.title),
//                        style = MaterialTheme.typography.headlineMedium.copy(
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.Bold,
//                            fontStyle = FontStyle.Italic,
//                            shadow = Shadow(
//                                color = Color.Black,
//                                offset = Offset(2f, 2f),
//                                blurRadius = 4f
//                            )
//                        ),
//                        color = Color.White,
//                        textAlign = TextAlign.Center
//                    )
                    MarqueeText(it.title, Color.White )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ){
                var color by remember { mutableStateOf(Color.White) }
                Text(
                    text = "♡",
                    color = color,
                    fontSize = 30.sp,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            if (color == Color.White) color = Color.Green
                            else color = Color.White
                        }
                    ,

                    )
            }
            Spacer(modifier = Modifier.height(16.dp))

            val safeDuration = songDuration.takeIf { it > 0f && it.isFinite() } ?: 1f

            // Slider for song position
            Slider(
                value = currentPosition.coerceIn(0f, safeDuration),
                onValueChange = { VM.exoPlayer.seekTo((it * 1000).toLong()) }, // convert back to ms
                valueRange = 0f..safeDuration,
                colors = SliderDefaults.colors(
                    thumbColor = colorResource(id = R.color.LightGreen),
                    activeTrackColor = Color.Green,
                    inactiveTrackColor = Color.Gray // Optional
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.DarkGray,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clip(RoundedCornerShape(24.dp))

            )

            // Time indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatTime(currentPosition))
                Text(text = formatTime(songDuration))
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Control buttons
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { VM.playPrevious() }) {
                    Icon(imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous")
                }

                IconButton(onClick = {
                    if (isPlaying) VM.exoPlayer.pause()
                    else VM.exoPlayer.play()
                    isPlaying = !isPlaying
                }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(48.dp)
                    )
                }

                IconButton(onClick = { VM.playNext() }) {
                    Icon(imageVector = Icons.Filled.SkipNext, contentDescription = "Next")
                }
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1DB954))
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()) // for status bar space
            .shadow(elevation = 4.dp)
    )
    {
        TopBarWithImageAndSearch(
            searchText = "",
            onSearchTextChange = {  },
            navController = navController,
            MV = VM,
        )
    }

}

// Liked UI

@Composable
fun LikedSongsUI(viewModel: MusicViewModel) {
    val isDark = isSystemInDarkTheme()
    val background = if (isDark) Color.Black else Color.White
    val textColor = if (isDark) Color.White else Color.Black
    var likedSongs: List<Song> = viewModel.songs

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


