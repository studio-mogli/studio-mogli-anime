package com.studiomogli.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.studiomogli.app.data.model.AnimeItem
import com.studiomogli.app.ui.theme.NetflixBlack
import com.studiomogli.app.ui.theme.NetflixRed
import com.studiomogli.app.ui.theme.NetflixSurface
import com.studiomogli.app.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAnimeClick: (AnimeItem) -> Unit
) {
    val trending by viewModel.trending.collectAsState()
    val popular by viewModel.popular.collectAsState()
    val topRated by viewModel.topRated.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedLang by remember { mutableStateOf("All") }
    val audioLanguages = listOf("All", "Japanese", "English", "Hindi")

    if (isLoading && trending.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NetflixBlack),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = NetflixRed)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixBlack),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // Hero Banner
        val featured = trending.firstOrNull()
        if (featured != null && searchQuery.isBlank()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(430.dp)
                ) {
                    AsyncImage(
                        model = featured.bannerUrl ?: featured.coverUrl,
                        contentDescription = featured.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.45f),
                                        Color.Transparent,
                                        NetflixBlack
                                    )
                                )
                            )
                    )
                    // Brand title top-left
                    Text(
                        text = "STUDIO MOGLI",
                        color = NetflixRed,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(start = 16.dp, top = 16.dp)
                            .align(Alignment.TopStart)
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = featured.genres.take(3).joinToString(" • "),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = featured.title,
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onAnimeClick(featured) },
                            colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Watch Now", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            item { Spacer(modifier = Modifier.statusBarsPadding().height(64.dp)) }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.search(it)
                },
                placeholder = { Text("Search anime titles...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.search("")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NetflixRed,
                    unfocusedBorderColor = Color(0xFF333333),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = NetflixSurface,
                    unfocusedContainerColor = NetflixSurface
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }

        // Anime Audio / Subtitle Filter Chips (UI stays English)
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(audioLanguages) { lang ->
                    FilterChip(
                        selected = selectedLang == lang,
                        onClick = { selectedLang = lang },
                        label = { Text(lang, color = Color.White, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NetflixRed,
                            containerColor = NetflixSurface
                        )
                    )
                }
            }
        }

        if (searchQuery.isNotBlank()) {
            item {
                AnimeCarouselRow(
                    title = "Search Results",
                    items = searchResults,
                    onAnimeClick = onAnimeClick
                )
            }
        } else {
            item {
                AnimeCarouselRow(
                    title = "Trending Now",
                    items = trending,
                    onAnimeClick = onAnimeClick
                )
            }
            item {
                AnimeCarouselRow(
                    title = "Most Popular",
                    items = popular,
                    onAnimeClick = onAnimeClick
                )
            }
            item {
                AnimeCarouselRow(
                    title = "Top Rated Anime",
                    items = topRated,
                    onAnimeClick = onAnimeClick
                )
            }
        }
    }
}

@Composable
private fun AnimeCarouselRow(
    title: String,
    items: List<AnimeItem>,
    onAnimeClick: (AnimeItem) -> Unit
) {
    if (items.isEmpty()) return
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { anime ->
                Column(
                    modifier = Modifier
                        .width(122.dp)
                        .clickable { onAnimeClick(anime) }
                ) {
                    AsyncImage(
                        model = anime.coverUrl,
                        contentDescription = anime.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(122.dp)
                            .height(178.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = anime.title,
                        color = Color.White,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
