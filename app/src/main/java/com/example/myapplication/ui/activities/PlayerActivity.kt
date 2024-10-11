package com.example.myapplication.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityPlayerBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private var videoUri: String? = null
    private lateinit var playerView: PlayerView
    private var exoPlayer: SimpleExoPlayer? = null
    private val TAG = "PlayerActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        videoUri = intent.getStringExtra("videoPath")

        playerView = binding.playerView
        initializePlayer()
    }

    private fun initializePlayer() {
        if (videoUri != null) {
            exoPlayer = SimpleExoPlayer.Builder(this).build()
            playerView.player = exoPlayer
            val mediaItem = MediaItem.fromUri(videoUri!!)
            exoPlayer?.setMediaItem(mediaItem)
            exoPlayer?.prepare()
            exoPlayer?.playWhenReady = true
        }
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
    }

    private fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }
}