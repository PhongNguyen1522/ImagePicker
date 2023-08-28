package com.phongnn.imagepicker.data.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val path: Int // Path or URL to the audio file
) : java.io.Serializable
