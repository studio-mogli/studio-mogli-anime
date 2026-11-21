package com.studiomogli.app.data.model

data class AnimeItem(
    val id: Int,
    val title: String,
    val romajiTitle: String,
    val bannerUrl: String?,
    val coverUrl: String,
    val description: String,
    val score: Int?,
    val episodesCount: Int?,
    val genres: List<String>,
    val availableLanguages: List<String> = listOf("Japanese", "English", "Hindi")
)

data class EpisodeItem(
    val id: String,
    val number: Int,
    val title: String,
    val provider: String // "DirectZanix" or fallback
)

data class SubtitleTrack(
    val languageCode: String, // "en", "hi", "ja"
    val label: String,
    val url: String
)

data class StreamResult(
    val hlsUrl: String,
    val referer: String? = null,
    val subtitles: List<SubtitleTrack> = emptyList(),
    val providerUsed: String,
    val isEmbed: Boolean = false
)

data class GitHubUpdateInfo(
    val versionTag: String,
    val releaseNotes: String,
    val downloadUrl: String
)
