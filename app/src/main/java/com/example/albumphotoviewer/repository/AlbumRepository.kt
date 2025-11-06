package com.example.albumphotoviewer.repository

import com.example.albumphotoviewer.models.Album
import com.example.albumphotoviewer.models.Photo
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface PicsumApi {
    @GET("v2/list?page=1&limit=100")
    suspend fun getPhotos(): List<Photo>
}

class AlbumRepository {
    private val api: PicsumApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://picsum.photos/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(PicsumApi::class.java)
    }

    suspend fun getAlbums(): List<Album> = withContext(Dispatchers.IO) {
        val photos = api.getPhotos()

        // Group photos by author
        val grouped = photos.groupBy { it.author }

        // Only keep authors with more than 1 photo
        val filtered = grouped.filter { it.value.size > 1 }

        filtered.keys.mapIndexed { index, name ->
            Album(id = index, title = name)
        }
    }

    suspend fun getPhotos(albumId: Int): List<Photo> = withContext(Dispatchers.IO) {
        val photos = api.getPhotos()
        val grouped = photos.groupBy { it.author }
            .filter { it.value.size > 1 } // same filtering rule here

        val authors = grouped.keys.toList()
        val selectedAuthor = authors.getOrNull(albumId) ?: return@withContext emptyList()

        grouped[selectedAuthor] ?: emptyList()
    }
}
