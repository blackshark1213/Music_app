package com.keshav.drone.mymusic.offline

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
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
import androidx.compose.material3.HorizontalDivider
import androidx.core.app.ActivityCompat

import android.net.Uri
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.res.painterResource
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
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.media3.common.util.UnstableApi

import coil.compose.rememberAsyncImagePainter


open class MainActivity : ComponentActivity() {

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            // Declare ViewModel scoped to the activity
            val musicViewModel: MusicViewModel = viewModel()

            // Pass it to NavHost
            MyAppNavigation(musicViewModel)
        }
        requestStoragePermission()

    }
    override fun onDestroy() {
        super.onDestroy()
//        unregisterReceiver(playbackReceiver)
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


//    override fun onBackPressed() {
//        super.onBackPressed()
//        // Prevent exiting immediately
//        AlertDialog.Builder(this)
//            .setTitle("Exit App?")
//            .setMessage("Are you sure you want to exit?")
//            .setPositiveButton("Yes") { _, _ -> finish() }
//            .setNegativeButton("No", null)
//            .show()
//    }

}

data class Song(
    val title: String,
    val artist: String,
    val path: String,
    val thumbnail: String? = null,
)

@Composable
fun MyAppNavigation(musicViewModel: MusicViewModel ) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val context = LocalContext.current
            // MusicUI call and share its content
            MusicAppUI(context ,
                navController ,
                musicViewModel ,
                exoPlayer = musicViewModel.exoPlayer)
        }

        composable("music/{path}") { backStackEntry ->
            val songPath = backStackEntry.arguments?.getString("songPath")
            //val thumbnail = backStackEntry.arguments?.getString("thumb")
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

/*
@OptIn(UnstableApi::class)
@Composable
fun MusicAppUI(context: Context, navController: NavController, MV: MusicViewModel ,  exoPlayer: ExoPlayer) {
    var searchText by remember { mutableStateOf("") }

    // Load songs once
    LaunchedEffect(Unit) {
        MV.loadSongs(context)
    }
    val context = LocalContext.current
    var audioSessionId by remember { mutableStateOf(0) }

    // Listen for Audio Session ID changes
    LaunchedEffect(exoPlayer) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(id: Int) {
                audioSessionId = id
            }
        })
    }

    // Filter songs
    val filteredSongs = MV.songs.filter {
        it.title.contains(searchText, ignoreCase = true) ||
                it.artist.contains(searchText, ignoreCase = true)
    }

    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val backgroundTint = if (isDarkTheme) Color.DarkGray else Color(0xFFF0F0F0)

    var showLikedSongs by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1DB954))
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                    .shadow(elevation = 4.dp)
            ) {
                TopBarWithImageAndSearch(
                    searchText = searchText,
                    onSearchTextChange = { searchText = it },
                    navController = navController,
                    value = 1,
                    MV = MV,
                )
            }
        },
        bottomBar = {
            MV.currentSong?.let {
                BottomPlayer(
                    song = it,
                    navController = navController,
                    VM = MV,
                    textcolor = textColor,
                    bg = backgroundTint,
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        showLikedSongs = dragAmount < -10
                    }
                }
        ) {
            if (showLikedSongs) {
//                LikedSongsUI(MV)
                EqualizerUI(audioSessionId = exoPlayer.audioSessionId)


            } else {
                val skipIndex = MV.skipIndex

                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(backgroundColor)
                ) {
                    itemsIndexed(filteredSongs) { index, song ->
                        val isSkipped = index in skipIndex

                       // Skip icon color as per UI
                        val iconColor : Color = if ((isSkipped)){
                            Color.Red
                        } else if (isDarkTheme){
                            Color.White
                        } else{
                            Color.Black
                        }

                        SongItem(
                            title = song.title,
                            artist = song.artist,
                            textColor = textColor,
                            thumbnail = song.thumbnail,
                            MV = MV,
                            onClick = {
                                MV.currentSongIndex = index
                                MV.playSong(song)
                            },
                            skipIcon = {
                                IconButton(onClick = {
                                    if (index in MV.skipIndex) {
                                        MV.skipIndex.remove(index)
                                        Toast.makeText(context, "Song set UnSkiped", Toast.LENGTH_SHORT).show()
                                        //Toast.makeText(context, "${MV.skipIndex}", Toast.LENGTH_LONG).show()
                                    } else {
                                        MV.skipIndex.add(index)
                                        Toast.makeText(context, "Song set Skiped", Toast.LENGTH_SHORT).show()
                                        //Toast.makeText(context, "${MV.skipIndex}", Toast.LENGTH_LONG).show()
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.MusicOff,
                                        contentDescription = "Skip",
                                        tint = iconColor
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
 */

@OptIn(UnstableApi::class)
@Composable
fun MusicAppUI(
    context: Context,
    navController: NavController,
    MV: MusicViewModel,
    exoPlayer: ExoPlayer
) {
    var searchText by remember { mutableStateOf("") }
    var audioSessionId by remember { mutableStateOf(0) }
    var showLikedSongs by remember { mutableStateOf(false) }

    // Load songs only once
    LaunchedEffect(Unit) {
        MV.loadSongs(context)
    }

    // Listen for audio session changes
    LaunchedEffect(exoPlayer) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(id: Int) {
                MV.setAudioSessionId(id)
            }
        })
    }


    // Theming
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val backgroundTint = if (isDarkTheme) Color.DarkGray else Color(0xFFF0F0F0)

    // Filtered songs
    val filteredSongs = MV.songs.filter {
        it.title.contains(searchText, ignoreCase = true) ||
                it.artist.contains(searchText, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1DB954))
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                    .shadow(4.dp)
            ) {
                TopBarWithImageAndSearch(
                    searchText = searchText,
                    onSearchTextChange = { searchText = it },
                    navController = navController,
                    value = 1,
                    MV = MV
                )
            }
        },
        bottomBar = {
            MV.currentSong?.let {
                BottomPlayer(
                    song = it,
                    navController = navController,
                    VM = MV,
                    textcolor = textColor,
                    bg = backgroundTint
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(padding)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        showLikedSongs = dragAmount < -10
                    }
                }
        ) {
            if (showLikedSongs) {
                    EqualizerUI(MV)
            } else {
                val skipIndex = MV.skipIndex

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(filteredSongs) { index, song ->
                        val isSkipped = index in skipIndex
                        val iconColor = when {
                            isSkipped -> Color.Red
                            isDarkTheme -> Color.White
                            else -> Color.Black
                        }

                        SongItem(
                            title = song.title,
                            artist = song.artist,
                            textColor = textColor,
                            thumbnail = song.thumbnail,
                            MV = MV,
                            onClick = {
                                MV.currentSongIndex = index
                                MV.playSong(song)

                            },
                            skipIcon = {
                                IconButton(onClick = {
                                    if (index in MV.skipIndex) {
                                        MV.skipIndex.remove(index)
                                        Toast.makeText(context, "Song set UnSkipped", Toast.LENGTH_SHORT).show()
                                    } else {
                                        MV.skipIndex.add(index)
                                        Toast.makeText(context, "Song set Skipped", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.MusicOff,
                                        contentDescription = "Skip",
                                        tint = iconColor
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun SongItem(
    title: String,
    artist: String,
    textColor: Color,
    thumbnail: String?,
    MV: MusicViewModel,
    onClick: () -> Unit,
    skipIcon: @Composable () -> Unit
) {
    val isPlaying = title == MV.currentSong?.title

    val iconTint = if (isPlaying) Color.DarkGray else Color.White
    val titleColor = if (isPlaying) colorResource(id = R.color.LightGreen) else textColor

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Thumbnail Image
                Image(
                    painter = rememberAsyncImagePainter(
                        model = thumbnail,
                       // placeholder = painterResource(id = R.drawable.music_default) // fallback image if loading fails
                    ),
                    contentDescription = "Album Art",
                    modifier = Modifier
                        .size(30.dp)
                        .padding(1.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF1DB954), Color.Gray)
                            )
                        ),
                    contentScale = ContentScale.Crop
                )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)  // so text doesn't overflow
            ) {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = titleColor)
                Text(text = artist, fontSize = 12.sp, color = Color.Gray)

            }

            // 🔥 Play Icon (right side)
            Box(
                modifier = Modifier
                    .size(20.dp),
                    //.background(boxColor, shape = MaterialTheme.shapes.small),

                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = iconTint
                )
            }
                        Box(
                modifier = Modifier
                    .size(30.dp),
            ){
                   // Skip Icon
                skipIcon()
            }

        }
    }

    HorizontalDivider()
}

fun getAllAudioFiles(context: Context): List<Song> {
    val songList = mutableListOf<Song>()
    val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.DATA
    )

    val cursor = context.contentResolver.query(
        uri,
        projection,
        "${MediaStore.Audio.Media.IS_MUSIC} != 0",
        null,
        "${MediaStore.Audio.Media.TITLE} ASC"
    )

    cursor?.use {
        val idIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val albumIdIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
        val dataIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

        while (it.moveToNext()) {
            val id = it.getLong(idIndex)
            val title = it.getString(titleIndex)
            val artist = it.getString(artistIndex)
            val albumId = it.getLong(albumIdIndex)
            val data = it.getString(dataIndex)

            // Try to build album art URI
            val albumArtUri = ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"),
                albumId
            )

            // Check if thumbnail file exists
            val thumbnail = try {
                context.contentResolver.openInputStream(albumArtUri)?.close()
                albumArtUri.toString()
            } catch (e: Exception) {
                null
            }

            songList.add(Song(title, artist, data, thumbnail = thumbnail))
        }
    }

    return songList
}



@Composable
fun BottomPlayer(
    song: Song,
    navController: NavController,
    VM: MusicViewModel,
    textcolor: Color,
    bg : Color,
) {
    val exoPlayer = VM.exoPlayer
    var isPlaying by remember { mutableStateOf(VM.exoPlayer.isPlaying) }

//     Keep isPlaying in sync with ExoPlayer state
    LaunchedEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playWhenReady: Boolean) {
                isPlaying = exoPlayer.isPlaying
            }
        }
        exoPlayer.addListener(listener)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable {
                val encodedPath = Uri.encode(song.path)
                VM.current_thumbnail =  song.thumbnail

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

            //TypingText(song.title)
            MarqueeText(
                text = song.title,
                textcolor = textcolor,
            )
        }

        IconButton(onClick = { VM.playPrevious() }) {
            Icon(
                imageVector = Icons.Filled.SkipPrevious,
                contentDescription = "Previous",
                tint = textcolor
            )
        }

        IconButton(onClick = {
            if (isPlaying) exoPlayer.pause()
            else exoPlayer.play()
        }) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = "Play/Pause",
                modifier = Modifier.size(48.dp),
                tint = textcolor
            )
        }

        IconButton(onClick = { VM.playNext() }) {
            Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = "Next",
                tint = textcolor
            )
        }

        IconButton(onClick = { VM.stopPlayer() }) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close",
                tint = textcolor
            )
        }

    }
}


@Composable
fun TopBarWithImageAndSearch(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    navController: NavController,
    value: Int = 0,
    MV: MusicViewModel,
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {

                val context = LocalContext.current
                val isMainActivity = context is MainActivity
                var currentActivity : String = ""
                var text = "-"

                if (isMainActivity) {
                    text = "Go online"

                    currentActivity = "MainActivityOnline"
                } else {
                    text = "Go offline"

                        currentActivity = "MainActivity"
                }

                Button(onClick = {
                    val targetActivity = when (currentActivity) {
                        "MainActivity" -> MainActivity::class.java
                        "MainActivityOnline" -> MainActivityOnline::class.java
                        else -> MainActivity::class.java
                    }
                        MV.exoPlayer.stop()
                        //MV.exoPlayer.release()

                    val intent = Intent(context, targetActivity)
                    context.startActivity(intent)
                },
                    modifier = Modifier
                        .padding(8.dp)
                    ,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF45D278),
                        contentColor = Color.White
                    )

                ) {
                    Text(text)
                }
            }
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


// Animation

@Composable
fun MarqueeText(text: String , textcolor: Color , fontS : Int = 16) {
    val scrollState = rememberScrollState()

    // Scroll animation loop
    LaunchedEffect(Unit) {
        while (true) {
            scrollState.animateScrollTo(scrollState.maxValue, animationSpec = tween(5000))
            delay(3000)
            scrollState.scrollTo(0)
            delay(5000)
        }
    }

    Row(
        modifier = Modifier
            .horizontalScroll(scrollState, enabled = false)
            .clipToBounds()
    ) {
        AnimatedVisibility(
            visible = true,  // Set this to your condition for visibility
            enter = fadeIn(tween(durationMillis = 1000)),  // Fade-in effect
            exit = fadeOut(tween(durationMillis = 1000))   // Fade-out effect
        ) {
            Text(
                text = text,
                color = textcolor,
                fontSize = fontS.sp,
                maxLines = 1,

                modifier = Modifier.padding(2.dp),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.W200,
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
            )
        }
    }
}

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
fun RotatingDiskImage(value : Int = 20000 ) {
    // Animate rotation angle from 0 to 360 and loop infinitely
    val infiniteTransition = rememberInfiniteTransition()
    var time_to_one_round = value
       val rotationAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = time_to_one_round, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

    Box(
        modifier = Modifier
            .size(500.dp) // Box size
            .background(Color.Transparent),
        contentAlignment = Alignment.Center // Center the image
    ) {
        Image(
            painter = painterResource(id = R.drawable.disk),
            contentDescription = "Disk Image",
            modifier = Modifier
                .size(350.dp)
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