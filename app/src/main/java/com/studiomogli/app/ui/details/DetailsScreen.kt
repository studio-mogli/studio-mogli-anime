package com.studiomogli.app.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.studiomogli.app.data.model.EpisodeItem
import com.studiomogli.app.ui.home.HomeViewModel
import com.studiomogli.app.ui.theme.NetflixBlack
import com.studiomogli.app.ui.theme.NetflixRed
import com.studiomogli.app.ui.theme.NetflixSurface
import com.studiomogli.app.ui.theme.TextSecondary

@Composable
fun DetailsScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit,
    onPlayEpisode: (EpisodeItem, String, String) -> Unit
) {
    val anime by viewModel.selectedAnime.collectAsState()
    val episodes by viewModel.episodes.collectAsState()
    val isLoadingEpisodes by viewModel.isLoadingEpisodes.collectAsState()

    var selectedAudioMode by remember { mutableStateOf("Japanese (Sub)") }
    val audioModes = listOf("Japanese (Sub)", "English (Dub)", "Hindi (Sub/Dub)")

    val item = anime ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixBlack),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp)
            ) {
                AsyncImage(
                    model = item.bannerUrl ?: item.coverUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.4f), NetflixBlack)
                            )
                        )
                )
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = buildString {
                        item.score?.let { append("★ $it% Match  •  ") }
                        append(item.genres.take(3).joinToString(", "))
                    },
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = item.description,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Preferred Anime Language",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    audioModes.forEach { mode ->
                        FilterChip(
                            selected = selectedAudioMode == mode,
                            onClick = { selectedAudioMode = mode },
                            label = { Text(mode, color = Color.White, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NetflixRed,
                                containerColor = NetflixSurface
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Episodes",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (isLoadingEpisodes) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NetflixRed)
                }
            }
        } else {
            items(episodes) { ep ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = NetflixSurface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                        .clickable {
                            val category = if (selectedAudioMode.contains("Dub") && !selectedAudioMode.contains("Hindi")) "dub" else "sub"
                            val langCode = when {
                                selectedAudioMode.contains("Hindi") -> "hi"
                                selectedAudioMode.contains("Japanese") -> "ja"
                                else -> "en"
                            }
                            onPlayEpisode(ep, category, langCode)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Episode ${ep.number}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "${ep.title} • Source: ${ep.provider}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NetflixRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}
