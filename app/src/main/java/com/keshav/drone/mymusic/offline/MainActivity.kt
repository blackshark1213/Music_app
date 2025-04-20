package com.keshav.drone.mymusic.offline

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.media.Image
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
import androidx.compose.animation.core.FastOutSlowInEasing
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

import androidx.compose.material.icons.filled.Pause

import androidx.compose.material.icons.filled.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
//import coil.compose.rememberAsyncImagePainter


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
//            MusicAppUI(this)

            // Declare ViewModel scoped to the activity
            val musicViewModel: MusicViewModel = viewModel()

            // Pass it to NavHost
            MyAppNavigation(musicViewModel)
            //MyAppNavigation()
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
fun MyAppNavigation(musicViewModel: MusicViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val context = LocalContext.current
            // MusicUI call and share its content
            MusicAppUI(context , navController , musicViewModel)
        }

        composable("music/{path}") { backStackEntry ->
            val songPath = backStackEntry.arguments?.getString("songPath")
//            val isPlaying = backStackEntry.arguments?.getString("isPlaying")?.toBoolean() ?: false
            val context = LocalContext.current

            val exoPlayer = remember(context) {
                ExoPlayer.Builder(context).build()
            }

            PlayerUI(
                navController = navController,
                songPath = songPath ?: "",
                exoPlayer = exoPlayer,
                VM = musicViewModel,
            )
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicAppUI(context: Context , navController: NavController, MV: MusicViewModel) {

    // Get ViewModel
//    val MV: MusicViewModel = viewModel()
    var searchText by remember { mutableStateOf("") }

    lateinit var exoPlayer: ExoPlayer


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

    // Load songs once
    LaunchedEffect(Unit) {
        MV.loadSongs(context)
    }

    // Filter songs based on search text
    val filteredSongs = MV.songs.filter {
        it.title.contains(searchText, ignoreCase = true) ||
                it.artist.contains(searchText, ignoreCase = true)
    }


    Scaffold(
//        Modifier.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1DB954))
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()) // for status bar space
                    .shadow(elevation = 4.dp)
            )
            {
                TopBarWithImageAndSearch(
                    searchText = searchText,
                    onSearchTextChange = { searchText = it },
                    navController = navController,
                    value = 1
                )
            }
        },

//                topBar = {
//            TopAppBar(
//                modifier = Modifier
//                    .fillMaxWidth(),
//                //.height(250.dp),
//                title = {
//
//                    TopBarWithImageAndSearch(
//                        searchText = searchText,
//                        onSearchTextChange = { searchText = it },
//                        navController,
//                        value = 1 ,
//                    )
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = Color(0xFF1DB954),
//                    titleContentColor = Color.White
//                )
//            )
//
//        },

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

                )
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(filteredSongs) { song ->
                SongItem(song.title, song.artist) {
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

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(text = artist, fontSize = 12.sp, color = Color.Gray)
            }
        }

    }
    HorizontalDivider()
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
fun BottomPlayer(
    song: Song,
    onStop: () -> Unit,
    isPlaying: Boolean,
    onToggle: () -> Unit,
    navController: NavController,

) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F0F0)) // light gray background
            .clickable {
                val encodedPath = Uri.encode(song.path)
                navController.navigate("music/$encodedPath")
            }
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

                )
        }
        Button(
            onClick = onStop,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Gray,
                contentColor = Color.White
            )
        )
        {

            Text("Stop")
        }
    }
}

@Composable
fun TopBarWithImageAndSearch(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    navController: NavController,
    value: Int = 0
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1DB954))
            .padding(horizontal = 16.dp, vertical = 12.dp),
            //.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()), // Consider this for status bar
        verticalArrangement = Arrangement.spacedBy(12.dp) // Add spacing between elements
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

        if (value == 1) {
            //Search Field
                TextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    placeholder = { Text("Search songs...", color = Color.LightGray  , fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp)
                        ),
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
    }


@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun PlayerUI(
    navController: NavController,
    songPath: String,
    exoPlayer: ExoPlayer,
    VM: MusicViewModel
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
//            val context = LocalContext.current
//            val thumbnailUri = getAlbumArtUri(context, Uri.parse(VM.exoPlayer.Song.path))
//
//
//            if (thumbnailUri != null) {
//                Image(
//                    painter = rememberAsyncImagePainter(model = thumbnailUri ?: R.drawable.placeholder),
//                    contentDescription = "Song Thumbnail",
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .clip(RoundedCornerShape(16.dp))
////                        .shadow(8.dp)
//                )
//            }

            if (isPlaying) RotatingDiskImage()
            else RotatingDiskImage(200000)


        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(16.dp))
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
                    Text(
                        text = (it.title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

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

            Spacer(modifier = Modifier.height(32.dp))

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
        )
    }



}


@SuppressLint("DefaultLocale")
fun formatTime(timeInSeconds: Float): String {
    val totalSeconds = timeInSeconds.toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

// Future use  FUN

fun getAlbumArtUri(context: Context, audioUri: Uri): Uri? {
    val projection = arrayOf(MediaStore.Audio.Media.ALBUM_ID)
    val cursor = context.contentResolver.query(audioUri, projection, null, null, null)

    cursor?.use {
        if (it.moveToFirst()) {
            val albumId = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
            return Uri.parse("content://media/external/audio/albumart/$albumId")
        }
    }
    return null
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
        style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = 18.sp, // slightly bigger for better readability
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            shadow = Shadow(
                color = Color.Black,
                offset = Offset(2f, 2f),
                blurRadius = 4f
            )
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun RotatingDiskImage(value : Int = 8000) {
    // Animate rotation angle from 0 to 360 and loop infinitely
    val infiniteTransition = rememberInfiniteTransition()
    var time_to_one_round = value
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = time_to_one_round, easing = LinearEasing), // 5 seconds full rotation
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .size(500.dp) // Box size
            .background(Color.Transparent), // Optional: Transparent background for image
        contentAlignment = Alignment.Center // Center the image
    ) {
        Image(
            painter = painterResource(id = R.drawable.disk),
            contentDescription = "Disk Image",
            modifier = Modifier
                .size(400.dp)
                .background(Color.Transparent)
                .graphicsLayer {
                    rotationZ = rotationAngle
                    shadowElevation = 16f // adds a soft shadow
                    shape = CircleShape      // optional, makes shadow round
                    clip = true              // if you want to clip the image to the shape
                },
        )
    }
}
