package com.keshav.drone.mymusic.offline

data class YouTubeSearchResponse(val items: List<YouTubeVideoItem>)

data class YouTubeVideoItem(
    val id: VideoId,
    val snippet: Snippet
)

data class VideoId(val videoId: String)

data class Snippet(
    val title: String,
    val channelTitle: String,
    val thumbnails: Thumbnails
)

data class Thumbnails(val medium: Thumbnail)
data class Thumbnail(val url: String)

