package com.example.albumphotoviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.albumphotoviewer.viewmodel.AlbumViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val albumViewModel: AlbumViewModel = viewModel()
            NavHost(navController = navController, startDestination = "albums") {

                composable("albums") {
                    AlbumListScreen(albumViewModel, navController)
                }

                composable(
                    route = "photos/{albumId}?showLoading={showLoading}",
                    arguments = listOf(
                        navArgument("albumId") { type = NavType.IntType },
                        navArgument("showLoading") { type = NavType.BoolType; defaultValue = false }
                    )
                ) { backStackEntry ->
                    val albumId = backStackEntry.arguments?.getInt("albumId") ?: 0
                    val showLoading = backStackEntry.arguments?.getBoolean("showLoading") ?: false
                    PhotoListScreen(albumId, albumViewModel, navController, showLoading)
                }

                composable("photo_fullscreen/{photoUrl}") { backStackEntry ->
                    val encoded = backStackEntry.arguments?.getString("photoUrl") ?: ""
                    val decoded = java.net.URLDecoder.decode(
                        encoded,
                        java.nio.charset.StandardCharsets.UTF_8.toString()
                    )
                    FullScreenPhoto(photoUrl = decoded, navController = navController)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumListScreen(viewModel: AlbumViewModel, navController: NavHostController) {
    val albums by viewModel.albums.collectAsState()
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.fetchAlbums()
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Albums") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                LazyColumn {
                    items(albums.size) { index ->
                        val album = albums[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("photos/${album.id}?showLoading=true")
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data("https://picsum.photos/id/${index + 1}/100/100")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = album.title,
                                modifier = Modifier
                                    .size(64.dp)
                                    .padding(end = 12.dp),
                                contentScale = ContentScale.Crop
                            )

                            Text(
                                text = album.title,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Divider()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoListScreen(
    albumId: Int,
    viewModel: AlbumViewModel,
    navController: NavHostController,
    showLoading: Boolean = false
) {
    val albums by viewModel.albums.collectAsState()
    val photos by viewModel.photos.collectAsState()
    var loading by remember { mutableStateOf(showLoading) }

    val albumTitle = albums.firstOrNull { it.id == albumId }?.title ?: "Photos"

    LaunchedEffect(albumId) {
        viewModel.fetchPhotos(albumId)
        if (loading) {
            delay(1500) // simulate loading only if triggered
            loading = false
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(albumTitle) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (loading) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Loading", color = Color.Gray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.padding(8.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(photos) { photo ->
                        AsyncImage(
                            model = photo.download_url,
                            contentDescription = photo.author,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                                .height(150.dp)
                                .clickable {
                                    val encoded = java.net.URLEncoder.encode(
                                        photo.download_url,
                                        java.nio.charset.StandardCharsets.UTF_8.toString()
                                    )
                                    navController.navigate("photo_fullscreen/$encoded")
                                }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenPhoto(photoUrl: String, navController: NavHostController) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        offset += panChange
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .transformable(transformableState)
    ) {
        AsyncImage(
            model = photoUrl,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .fillMaxSize()
        )
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}
