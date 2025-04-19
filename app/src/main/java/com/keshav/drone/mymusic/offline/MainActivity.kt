package com.keshav.drone.mymusic.offline

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.core.app.ActivityCompat

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.res.painterResource
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.compose.NavHost
import kotlinx.coroutines.delay
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

//import androidx.compose.foundation.slider.Slider
//
//import com.google.android.exoplayer2.*
//import com.google.android.exoplayer2.media3.MediaItem
//import com.google.android.exoplayer2.ExoPlayer
//import com.google.android.exoplayer2.MediaItem

import androidx.compose.material.icons.filled.Pause

import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
//            MusicAppUI(this)
            MyAppNavigation()
        }
        requestStoragePermission()

    }
    private fun requestStoragePermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        ActivityCompat.requestPermissions(this, permissions, 101)
    }

    override fun onPause() {
        super.onPause()
    }
}

data class Song(
    val title: String,
    val artist: String,
    val path: String
)


@Composable
fun MyAppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val context = LocalContext.current
            MusicAppUI(context , navController)
        }

        composable("music/{title}/{path}/{isPlaying}") { backStackEntry ->
            val songTitle = backStackEntry.arguments?.getString("songTitle")
            val songPath = backStackEntry.arguments?.getString("songPath")
            val isPlaying = backStackEntry.arguments?.getString("isPlaying")?.toBoolean() ?: false
            val context = LocalContext.current

            val exoPlayer = remember(context) {
                ExoPlayer.Builder(context).build()
            }

            PlayerUI(
                navController = navController,
                songTitle = songTitle ?: "",
                songPath = songPath ?: "",
                exoPlayer = exoPlayer,
                isPlayingM = isPlaying,
            )
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicAppUI(context: Context , navController: NavController) {


    val songs = remember { mutableStateListOf<Song>() }
    var currentSong by remember { mutableStateOf<Song?>(null) }
    var searchText by remember { mutableStateOf("") }
    var currentSongIndex by remember { mutableIntStateOf(0) }

    // Get ViewModel
   val MV: MusicViewModel = viewModel()

    lateinit var exoPlayer: ExoPlayer

    // playing song at any index
    fun playSongAt(index: Int) {
        if (index in MV.songs.indices) {
            val song = MV.songs[index]
            currentSong = song
            currentSongIndex = index

            exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(song.path)))
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    // it will call automatically after the song complete
    fun playNext() {
        val nextIndex = currentSongIndex + 1
        if (nextIndex < MV.songs.size) {
            playSongAt(nextIndex)
        } else {
            // Optional: Loop back to first song
            // playSongAt(0)
            currentSong = null
        }
    }

    exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        MV.playNext()
                    }
                }
            })
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // Load songs
//    LaunchedEffect(Unit) {
//        songs.clear()
//        songs.addAll(getAllAudioFiles(context))
//    }

    // Load songs once
    LaunchedEffect(Unit) {
        MV.loadSongs(context)
    }

    // Play selected song
    fun playSong(song: Song) {
        currentSong = song
        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(song.path)))
        exoPlayer.prepare()
        exoPlayer.play()

    }

    // Filter songs based on search text
    val filteredSongs = MV.songs.filter {
        it.title.contains(searchText, ignoreCase = true) ||
                it.artist.contains(searchText, ignoreCase = true)
    }


    // Stop playback
    fun stopSong() {
        exoPlayer.stop()
        currentSong = null
    }

    Scaffold(
//        Modifier.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
                topBar = {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                title = {

                        TopBarWithImageAndSearch(
                            searchText = searchText,
                            onSearchTextChange = { searchText = it },
                            navController
                        )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1DB954),
                    titleContentColor = Color.White
                )
            )

        },
//        bottomBar = {
//            currentSong?.let {
//                BottomPlayer(
//                    song = currentSong!!,
//                    isPlaying = exoPlayer.isPlaying,
//                    onStop = { stopSong() },
//                    onToggle = {
//                        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
//                    },
//                    navController = navController,
//                    exoPlayer = exoPlayer,
//                )
//            }
//        }
//    ) { padding ->
//        LazyColumn(modifier = Modifier.padding(padding)) {
//            items(filteredSongs) { song ->
//                SongItem(song.title, song.artist) {
//                    playSong(song)
//                }
//            }
//        }
//    }

        bottomBar = {
            MV.currentSong?.let {
                BottomPlayer(
                    song = MV.currentSong!!,
                    isPlaying = MV.exoPlayer.isPlaying,
                    onStop = { MV.stopPlayer() },
                    onToggle = {
                        if (MV.exoPlayer.isPlaying) MV.exoPlayer.pause() else MV.exoPlayer.play()
                    },
                    navController = navController,
                    exoPlayer = MV.exoPlayer,
                )
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(filteredSongs) { song ->
                SongItem(song.title, song.artist) {
//                    musicViewModel.playSongAt(songs.indexOf(song))
                    MV.playSong(song)

                }
            }
        }
    }
}



@Composable
fun SongItem(title: String, artist: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(Color(0xFF1DB954), shape = MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(text = artist, fontSize = 12.sp, color = Color.Gray)
            }
        }

    }
    HorizontalDivider()
}


@Composable
fun BottomPlayer(
    song: Song,
    onStop: () -> Unit,
    isPlaying: Boolean,
    onToggle: () -> Unit,
    navController: NavController,
    exoPlayer: ExoPlayer
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F0F0)) // light gray background
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically

    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(Color(0xFF1DB954), shape = MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White
            )
        }

        Column(modifier = Modifier.weight(1f)) {

            Text(
                text = song.title,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(16.dp)
                    .clickable {
                        val encodedTitle = Uri.encode(song.title)
                        val encodedPath = Uri.encode(song.path)
                        val isPlayingString = isPlaying.toString()

                        navController.navigate("music/$encodedTitle/$encodedPath/$isPlayingString")

                    }
            )
        }
        Button(
            onClick = onStop,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = Color.White
            )
        )
        {

            Text("Stop")
        }
    }
}



fun getAllAudioFiles(context: Context): List<Song> {
    val songList = mutableListOf<Song>()
    val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    val projection = arrayOf(
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DATA
    )

    val cursor = context.contentResolver.query(
        uri, projection,
        "${MediaStore.Audio.Media.IS_MUSIC} != 0",
        null,
        MediaStore.Audio.Media.TITLE + " ASC"
    )

    cursor?.use {
        val titleIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val dataIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

        while (it.moveToNext()) {
            val title = it.getString(titleIndex)
            val artist = it.getString(artistIndex)
            val data = it.getString(dataIndex)

            songList.add(Song(title, artist, data))
        }
    }

    return songList
}

@Composable
fun TopBarWithImageAndSearch(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    navController: NavController
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1DB954))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.ic_launcher),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        navController.navigate("home") // Navigate to home
                    }
            )


            Spacer(modifier = Modifier.width(8.dp))

            // Animated Typing Title
            TypingText(fullText = "My Music")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Field
        TextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            placeholder = { Text("Search songs...", color = Color.LightGray) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.15f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.10f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
        )
    }
}


@Composable
fun PlayerUI(
    navController: NavController,
    songTitle: String,
    songPath: String,
    exoPlayer: ExoPlayer,
    isPlayingM: Boolean,
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableFloatStateOf(0f) }
    var songDuration by remember { mutableFloatStateOf(1f) }

    // Prepare and observe the player
    LaunchedEffect(songPath) {
        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(songPath)))
        exoPlayer.prepare()
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    songDuration = exoPlayer.duration.coerceAtLeast(1).toFloat()
                }
            }
        })

        while (true) {
            currentPosition = exoPlayer.currentPosition / 1000f
            delay(1000)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Song Title
            Text(
                text = songTitle,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Slider for song position
            Slider(
                value = currentPosition.coerceIn(0f, songDuration),
                onValueChange = { exoPlayer.seekTo((it * 1000).toLong()) },
                valueRange = 0f..songDuration,
                modifier = Modifier.fillMaxWidth()
            )

            // Time indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatTime(currentPosition.toLong()))
                Text(text = formatTime(songDuration.toLong()))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Control buttons
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { exoPlayer.seekTo(0) }) {
                    Icon(imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous")
                }

                IconButton(onClick = {
                    if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                    isPlaying = !isPlaying
                }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(48.dp)
                    )
                }

                IconButton(onClick = { exoPlayer.seekToNext() }) {
                    Icon(imageVector = Icons.Filled.SkipNext, contentDescription = "Next")
                }
            }
        }
    }
}


@SuppressLint("DefaultLocale")
fun formatTime(timeInSeconds: Long): String {
    val minutes = timeInSeconds / 60
    val seconds = timeInSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

// Animation
@Composable
fun TypingText(
    fullText: String,
    DEV : String  = "K e s h a v",
    typingSpeed: Long = 100L,     // milliseconds per character
    restartDelay: Long = 5000L    // delay before restarting the typing animation
) {
    var displayedText by remember { mutableStateOf("") }
    var i by remember { mutableStateOf("1") }

    LaunchedEffect(Unit) {
        while (true) {
            if(i.isNotEmpty()){
                i = ""
                displayedText = ""
                fullText.forEachIndexed { index, _ ->
                    displayedText = fullText.substring(0, index + 1)
                    delay(typingSpeed)
                }
                delay(restartDelay)
            }else{
                i = "1"
                displayedText = ""
                DEV.forEachIndexed { index, _ ->
                    displayedText = DEV.substring(0, index + 1)
                    delay(typingSpeed)
                }
                delay(restartDelay)
            }
        }
    }

    Text(
        text = displayedText,
        color = Color.White,
        style = MaterialTheme.typography.titleLarge
    )
}


