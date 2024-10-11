package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.VideoRepository
import com.example.myapplication.data.models.VideoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideoViewModel(private val repository: VideoRepository) : ViewModel() {

    private val _videoList = MutableLiveData<List<VideoModel>>()
    val videoList: LiveData<List<VideoModel>> get() = _videoList

    fun loadVideos() {
        viewModelScope.launch(Dispatchers.IO) {
            val videos = repository.loadVideosFromGallery()
            _videoList.postValue(videos)
        }
    }
}
