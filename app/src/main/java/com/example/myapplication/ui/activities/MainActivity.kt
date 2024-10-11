package com.example.myapplication.ui.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.ui.adapters.VideoAdapter
import com.example.myapplication.data.repository.VideoRepository
import com.example.myapplication.ui.viewmodel.VideoViewModel
import com.example.myapplication.ui.viewmodel.VideoViewModelFactory
import com.example.myapplication.data.models.VideoModel
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.utils.InterstitialAdHelper
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_PERMISSION = 101
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var binding: ActivityMainBinding
    val videoList = mutableListOf<VideoModel>()
    private lateinit var interstitialAdHelper: InterstitialAdHelper

    private val TAG = "MainActivity"

    private lateinit var gridLayoutManager: GridLayoutManager

    private lateinit var videoViewModel: VideoViewModel
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(this@MainActivity) {}
        }


        val repository = VideoRepository(contentResolver)
        val viewModelFactory = VideoViewModelFactory(repository)
        videoViewModel = ViewModelProvider(this, viewModelFactory)
            .get(VideoViewModel::class.java)


        interstitialAdHelper = InterstitialAdHelper(this)
        interstitialAdHelper.loadAd()

        videoAdapter = VideoAdapter(
            videoList = videoList,
            onDelete = { video ->
//                deleteVideo(video)
            },
            onShare = { video ->
//                shareVideo(video)
            },
            onOpen = { video ->
                interstitialAdHelper.showAd(this) {
                    openVideoInPlayer(video)
                }
            }
        )

        gridLayoutManager = GridLayoutManager(this, 3)

        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if ((position + 1) % 7 == 0) {
                    3 // Full width for AdView
                } else {
                    1
                }
            }
        }
        binding.rvVideos.layoutManager = gridLayoutManager
        binding.rvVideos.adapter = videoAdapter

//        showCustomPermissionDialog()

        binding.swiperefresh.setOnRefreshListener {
//            loadVideosFromGallery()
            binding.swiperefresh.isRefreshing = false
        }
        // Observe the videoList LiveData
        videoViewModel.videoList.observe(this) { videos ->
            videoAdapter.submitList(videos) // Update the adapter with the new video list
        }

        checkPermissions()

    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermissions() {
        if (hasStoragePermission()) {
            videoViewModel.loadVideos()
        } else {
            showCustomPermissionDialog()
        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkSelfPermission(android.Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
        } else {
            checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun openVideoInPlayer(video: VideoModel) {
        val intent = Intent(this, PlayerActivity::class.java)
        Log.d(TAG, "openVideoInPlayer: ${video.toString()}")
        intent.putExtra("videoPath", video.uri.toString())
        startActivity(intent)
    }

    private fun showCustomPermissionDialog(isSecondAttempt: Boolean = false) {
        val message = if (isSecondAttempt) {
            "This permission is required to access your media files. Without this permission, the app cannot display videos from your gallery. Please allow it."
        } else {
            "We need access to your media files to show videos from your gallery."
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle("Permission Needed")
            .setMessage(message)
            .setPositiveButton("Allow") { dialog, _ ->
                requestStoragePermission()
                dialog.dismiss()
            }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
                if (!isSecondAttempt) {
                    showCustomPermissionDialog(isSecondAttempt = true)
                } else {
                    Toast.makeText(this, "Permission is required to use this feature!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .create()

        dialog.show()
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.READ_MEDIA_VIDEO),
                REQUEST_CODE_PERMISSION
            )
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults) // Call to super

        if (requestCode == REQUEST_CODE_PERMISSION && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "onRequestPermissionsResult: Permission Granted")
            videoViewModel.loadVideos()
        } else {
            // Permission denied, handle it
            Log.d(TAG, "onRequestPermissionsResult: Permission denied")
            Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
        }
    }
}