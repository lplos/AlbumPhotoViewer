package com.example.albumphotoviewer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.albumphotoviewer.models.Album
import com.example.albumphotoviewer.models.Photo
import com.example.albumphotoviewer.repository.AlbumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AlbumViewModel : ViewModel() {
    private val repository = AlbumRepository()
    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums
    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos
    private val _isLoading = MutableStateFlow(false)
    fun fetchAlbums() {
        viewModelScope.launch {
            _albums.value = repository.getAlbums()
        }
    }
    fun fetchPhotos(albumId: Int) {
        viewModelScope.launch {
            _isLoading.value = true                 // show loading
            _photos.value = repository.getPhotos(albumId)
            _isLoading.value = false                // hide loading
        }
    }
}
