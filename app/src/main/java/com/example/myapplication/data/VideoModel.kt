package com.example.myapplication.data

import android.net.Uri

data class VideoModel(
    val name: String,
    val size: Long, // in bytes
    val duration: Long, // in milliseconds
    val dateModified: Long, // in seconds
    val uri: Uri
)
