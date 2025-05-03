package com.keshav.drone.mymusic.offline

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.HorizontalDivider
import androidx.core.app.ActivityCompat

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


class MainActivityOnline : ComponentActivity  (){

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
//            MusicAppUI(this)

            // Declare ViewModel scoped to the activity
            val musicViewModel: MusicViewModel = viewModel()

            // Pass it to NavHost
            MyAppNavigationOnline(musicViewModel)
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


}
data class SongO(
    val title: String,
    val artist: String,
    val videoId: String,
    val thumbnailUrl: String
)

const val YOUTUBE_API_KEY = "AIzaSyCCCfA_kHy8E4Oot3e1hd6tyatNgLEh6TQ"



@Composable
fun MyAppNavigationOnline(musicViewModel: MusicViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val context = LocalContext.current
            val text = "Go offline"
            // MusicUIOnline call and share its content
            MusicAppUIOnline(context , navController , musicViewModel)
        }

        composable("music/{path}") { backStackEntry ->
            val songPath = backStackEntry.arguments?.getString("songPath")
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

        composable("player/{videoId}") { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
            YouTubeVideoPlayer(videoId = videoId , navController)
        }


    }
}


@Composable
fun MusicAppUIOnline(context: Context , navController: NavController, MV: MusicViewModel ) {

    DisposableEffect(Unit) {
        onDispose {
            //exoPlayer.release()
        }
    }

    var searchText by remember { mutableStateOf("") }

    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val Background = if (isDarkTheme) Color.DarkGray else Color(0xFFF0F0F0)



    var filteredSongs by remember { mutableStateOf<List<SongO>>(emptyList()) }
    LaunchedEffect(searchText) {
        filteredSongs = searchYouTubeSongs(searchText)
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
                    value = 1,
                    MV = MV,
                )
            }
        },

        bottomBar = {
            MV.currentSong?.let {
                BottomPlayer(
                    song = MV.currentSong!!,
                    navController = navController,
                    VM = MV,
                    textcolor = textColor,
                    bg = Background,
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .background(backgroundColor)
        ) {
            items(filteredSongs) { song ->
                SongItemO(
                    title = song.title,
                    artist = song.artist,
                    textColor = textColor,
                    MV = MV,
                    onClick = {
                        navController.navigate("player/${song.videoId}")
                    }
                )
                HorizontalDivider(color = textColor.copy(alpha = 0.2f))
            }
        }
    }
}



@Composable
fun SongItemO(
    title: String,
    artist: String,
    textColor: Color, // You were passing textColor, so I've included it
    MV: MusicViewModel, // Assuming MV is your ViewModel instance
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(16.dp)
    ) {

        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = textColor) // Applied textColor
            Text(artist, style = MaterialTheme.typography.bodySmall, color = textColor) // Applied textColor
        }
    }
    HorizontalDivider(color = textColor.copy(alpha = 0.2f))
}



interface YouTubeApiService {
    @GET("search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("key") apiKey: String = YOUTUBE_API_KEY,
        @Query("maxResults") maxResults: Int = 25,
        @Query("type") type: String = "video"
    ): YouTubeSearchResponse
}

val retrofit = Retrofit.Builder()
    .baseUrl("https://www.googleapis.com/youtube/v3/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val service = retrofit.create(YouTubeApiService::class.java)

suspend fun searchYouTubeSongs(query: String): List<SongO> {
    return try {
        val response = service.searchVideos(query = query)
        response.items.map {
            SongO(
                title = it.snippet.title,
                artist = it.snippet.channelTitle,
                videoId = it.id.videoId,
                thumbnailUrl = it.snippet.thumbnails.medium.url
            )
        }
    } catch (e: Exception) {
        println("Error searching YouTube: ${e.localizedMessage}")
        emptyList() // Return an empty list in case of an error
    }
}

@Composable
fun YouTubeVideoPlayer(videoId: String , navController: NavController) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .background(Color.Red)
            .size(400.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = {
                val youTubePlayerView = YouTubePlayerView(it)
                val lifecycle = (context as ComponentActivity).lifecycle
                lifecycle.addObserver(youTubePlayerView)

                youTubePlayerView.addYouTubePlayerListener(object :
                    AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        youTubePlayer.loadVideo(videoId, 0f)
                    }
                })

                youTubePlayerView
            },
            modifier = Modifier
                //.fillMaxWidth()

                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
        )

        Box(
            modifier = Modifier
                .background(Color.Red)
                .size(400.dp),
            contentAlignment = Alignment.Center
        ) {
            navController.navigate("home") // Navigate to home
        }

    }


}



