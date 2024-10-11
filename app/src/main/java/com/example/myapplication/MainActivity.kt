package com.example.myapplication

import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.data.VideoModel
import com.example.myapplication.databinding.ActivityMainBinding
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(this@MainActivity) {}
        }
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

        val gridLayoutManager = GridLayoutManager(this, 3)

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

        showCustomPermissionDialog()

        binding.swiperefresh.setOnRefreshListener {
            loadVideosFromGallery()
            binding.swiperefresh.isRefreshing = false
        }


    }

    private fun openVideoInPlayer(video: VideoModel) {
        val intent = Intent(this, PlayerActivity::class.java)
        Log.d(TAG, "openVideoInPlayer: ${video.toString()}")
        intent.putExtra("videoPath", video.uri.toString())
        startActivity(intent)
    }

    private fun showCustomPermissionDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Permission Needed")
            .setMessage("We need access to your media files to show videos from your gallery.")
            .setPositiveButton("Allow") { dialog, _ ->
                requestStoragePermission()
                dialog.dismiss()
            }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
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
            // Permission granted, proceed with loading videos
            loadVideosFromGallery()
        } else {
            // Permission denied, handle it
            Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadVideosFromGallery() {
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_MODIFIED
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_MODIFIED} DESC"

        val cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val dateModifiedColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn)
                val size = it.getLong(sizeColumn)
                val duration = it.getLong(durationColumn)
                val dateModified = it.getLong(dateModifiedColumn)

                val uri =
                    ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                videoList.add(VideoModel(name, size, duration, dateModified, uri))
            }
            videoAdapter.notifyDataSetChanged()
        }

    }


}