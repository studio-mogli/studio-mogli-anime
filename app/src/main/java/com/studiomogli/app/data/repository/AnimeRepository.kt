package com.studiomogli.app.data.repository

import com.google.gson.JsonObject
import com.studiomogli.app.data.AppConfig
import com.studiomogli.app.data.api.AniListApi
import com.studiomogli.app.data.api.AniListQueries
import com.studiomogli.app.data.api.DynamicStreamApi
import com.studiomogli.app.data.api.GitHubUpdateApi
import com.studiomogli.app.data.api.GraphQLRequest
import com.studiomogli.app.data.model.AnimeItem
import com.studiomogli.app.data.model.EpisodeItem
import com.studiomogli.app.data.model.GitHubUpdateInfo
import com.studiomogli.app.data.model.StreamResult
import com.studiomogli.app.data.model.SubtitleTrack
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class AnimeRepository {

    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val aniListApi: AniListApi = Retrofit.Builder()
        .baseUrl(AppConfig.ANILIST_URL)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AniListApi::class.java)

    private val streamApi: DynamicStreamApi = Retrofit.Builder()
        .baseUrl("https://localhost/")
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(DynamicStreamApi::class.java)

    private val githubApi: GitHubUpdateApi = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GitHubUpdateApi::class.java)

    suspend fun fetchCategory(sort: String, perPage: Int = 20): List<AnimeItem> {
        return try {
            val response = aniListApi.query(
                GraphQLRequest(
                    query = AniListQueries.MEDIA_LIST,
                    variables = mapOf("sort" to listOf(sort), "perPage" to perPage)
                )
            )
            parseAniListMedia(response)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchAnime(keyword: String): List<AnimeItem> {
        return try {
            val response = aniListApi.query(
                GraphQLRequest(
                    query = AniListQueries.SEARCH,
                    variables = mapOf("search" to keyword)
                )
            )
            parseAniListMedia(response)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseAniListMedia(root: JsonObject): List<AnimeItem> {
        val list = mutableListOf<AnimeItem>()
        val mediaArray = root.getAsJsonObject("data")
            ?.getAsJsonObject("Page")
            ?.getAsJsonArray("media") ?: return list

        for (el in mediaArray) {
            val obj = el.asJsonObject
            val titleObj = obj.getAsJsonObject("title")
            val english = titleObj?.get("english")?.takeIf { !it.isJsonNull }?.asString
            val romaji = titleObj?.get("romaji")?.takeIf { !it.isJsonNull }?.asString ?: "Unknown Title"
            val coverObj = obj.getAsJsonObject("coverImage")
            val cover = coverObj?.get("extraLarge")?.takeIf { !it.isJsonNull }?.asString
                ?: coverObj?.get("large")?.takeIf { !it.isJsonNull }?.asString ?: ""
            val banner = obj.get("bannerImage")?.takeIf { !it.isJsonNull }?.asString
            val rawDesc = obj.get("description")?.takeIf { !it.isJsonNull }?.asString ?: "No synopsis available."
            val cleanDesc = rawDesc.replace(Regex("<.*?>"), "").trim()
            val score = obj.get("averageScore")?.takeIf { !it.isJsonNull }?.asInt
            val episodes = obj.get("episodes")?.takeIf { !it.isJsonNull }?.asInt
            val genres = obj.getAsJsonArray("genres")?.map { it.asString } ?: emptyList()

            list.add(
                AnimeItem(
                    id = obj.get("id").asInt,
                    title = english ?: romaji,
                    romajiTitle = romaji,
                    bannerUrl = banner,
                    coverUrl = cover,
                    description = cleanDesc,
                    score = score,
                    episodesCount = episodes,
                    genres = genres
                )
            )
        }
        return list
    }

    /**
     * Multi-provider episode fetcher for maximum catalog coverage:
     * Tries Aniwatch-API (HiAnime) -> Consumet -> Anify in order.
     */
    suspend fun fetchEpisodesMultiProvider(anime: AnimeItem): List<EpisodeItem> {
        val encoded = URLEncoder.encode(anime.title, "UTF-8")

        // 1. Try Aniwatch-API (HiAnime)
        try {
            val base = AppConfig.aniwatchBaseUrl.trimEnd('/')
            val searchJson = streamApi.getJson("$base/api/v2/hianime/search?q=$encoded&page=1").asJsonObject
            val animes = searchJson.getAsJsonObject("data")?.getAsJsonArray("animes")
            if (animes != null && animes.size() > 0) {
                val id = animes[0].asJsonObject.get("id").asString
                val epJson = streamApi.getJson("$base/api/v2/hianime/anime/$id/episodes").asJsonObject
                val episodes = epJson.getAsJsonObject("data")?.getAsJsonArray("episodes")
                if (episodes != null && episodes.size() > 0) {
                    return episodes.mapIndexed { index, el ->
                        val ep = el.asJsonObject
                        EpisodeItem(
                            id = ep.get("episodeId").asString,
                            number = ep.get("number")?.asInt ?: (index + 1),
                            title = ep.get("title")?.takeIf { !it.isJsonNull }?.asString ?: "Episode ${index + 1}",
                            provider = "Aniwatch"
                        )
                    }
                }
            }
        } catch (_: Exception) { }

        // 2. Fallback to Consumet
        try {
            val base = AppConfig.consumetBaseUrl.trimEnd('/')
            val searchJson = streamApi.getJson("$base/anime/gogoanime/$encoded").asJsonObject
            val results = searchJson.getAsJsonArray("results")
            if (results != null && results.size() > 0) {
                val id = results[0].asJsonObject.get("id").asString
                val infoJson = streamApi.getJson("$base/anime/gogoanime/info/$id").asJsonObject
                val episodes = infoJson.getAsJsonArray("episodes")
                if (episodes != null && episodes.size() > 0) {
                    return episodes.mapIndexed { index, el ->
                        val ep = el.asJsonObject
                        EpisodeItem(
                            id = ep.get("id").asString,
                            number = ep.get("number")?.asInt ?: (index + 1),
                            title = "Episode ${ep.get("number")?.asInt ?: (index + 1)}",
                            provider = "Consumet"
                        )
                    }
                }
            }
        } catch (_: Exception) { }

        // 3. Fallback to Anify (direct AniList ID mapping)
        try {
            val base = AppConfig.anifyBaseUrl.trimEnd('/')
            val epArray = streamApi.getJson("$base/episodes/${anime.id}").asJsonArray
            if (epArray.size() > 0) {
                val firstProvider = epArray[0].asJsonObject
                val eps = firstProvider.getAsJsonArray("episodes")
                if (eps != null && eps.size() > 0) {
                    return eps.mapIndexed { index, el ->
                        val ep = el.asJsonObject
                        EpisodeItem(
                            id = ep.get("id").asString,
                            number = ep.get("number")?.asInt ?: (index + 1),
                            title = ep.get("title")?.takeIf { !it.isJsonNull }?.asString ?: "Episode ${index + 1}",
                            provider = "Anify"
                        )
                    }
                }
            }
        } catch (_: Exception) { }

        // Fallback sample episode list if all upstream public mirrors are unreachable
        val count = (anime.episodesCount ?: 12).coerceAtMost(24)
        return (1..count).map { num ->
            EpisodeItem(
                id = "demo-${anime.id}-$num",
                number = num,
                title = "Episode $num",
                provider = "Demo"
            )
        }
    }

    /**
     * Resolves HLS (.m3u8) stream and subtitle tracks from the selected provider.
     * category: "sub" (Japanese) or "dub" (English/Hindi when available)
     */
    suspend fun fetchStreamUrl(episode: EpisodeItem, category: String = "sub"): StreamResult {
        try {
            when (episode.provider) {
                "Aniwatch" -> {
                    val base = AppConfig.aniwatchBaseUrl.trimEnd('/')
                    val json = streamApi.getJson(
                        "$base/api/v2/hianime/episode/sources?animeEpisodeId=${episode.id}&category=$category"
                    ).asJsonObject
                    val data = json.getAsJsonObject("data")
                    val sources = data?.getAsJsonArray("sources")
                    val url = sources?.get(0)?.asJsonObject?.get("url")?.asString
                    val referer = data?.getAsJsonObject("headers")?.get("Referer")?.asString
                    val subs = data?.getAsJsonArray("tracks")?.mapNotNull { el ->
                        val obj = el.asJsonObject
                        val file = obj.get("file")?.takeIf { !it.isJsonNull }?.asString ?: return@mapNotNull null
                        val label = obj.get("label")?.takeIf { !it.isJsonNull }?.asString ?: "English"
                        SubtitleTrack(
                            languageCode = mapLangCode(label),
                            label = label,
                            url = file
                        )
                    } ?: emptyList()

                    if (!url.isNullOrBlank()) {
                        return StreamResult(hlsUrl = url, referer = referer, subtitles = subs, providerUsed = "Aniwatch-API")
                    }
                }

                "Consumet" -> {
                    val base = AppConfig.consumetBaseUrl.trimEnd('/')
                    val json = streamApi.getJson("$base/anime/gogoanime/watch/${episode.id}").asJsonObject
                    val sources = json.getAsJsonArray("sources")
                    val url = sources?.get(0)?.asJsonObject?.get("url")?.asString
                    val referer = json.getAsJsonObject("headers")?.get("Referer")?.asString
                    if (!url.isNullOrBlank()) {
                        return StreamResult(hlsUrl = url, referer = referer, providerUsed = "Consumet")
                    }
                }

                "Anify" -> {
                    val base = AppConfig.anifyBaseUrl.trimEnd('/')
                    val json = streamApi.getJson("$base/sources?providerId=gogoanime&watchId=${episode.id}").asJsonObject
                    val sources = json.getAsJsonArray("sources")
                    val url = sources?.get(0)?.asJsonObject?.get("url")?.asString
                    if (!url.isNullOrBlank()) {
                        return StreamResult(hlsUrl = url, providerUsed = "Anify")
                    }
                }
            }
        } catch (_: Exception) { }

        // Safe public HLS test stream fallback so the player works immediately even offline from mirrors
        return StreamResult(
            hlsUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            providerUsed = "Fallback HLS Stream"
        )
    }

    private fun mapLangCode(label: String): String {
        val lower = label.lowercase()
        return when {
            "hindi" in lower -> "hi"
            "japan" in lower -> "ja"
            else -> "en"
        }
    }

    /**
     * Checks GitHub Releases for a newer version than BuildConfig.VERSION_NAME.
     * Returns null if already up-to-date or if repo is not configured yet.
     */
    suspend fun checkForGitHubUpdate(): GitHubUpdateInfo? {
        if (AppConfig.GITHUB_OWNER == "YOUR_GITHUB_USERNAME") return null
        return try {
            val json = githubApi.getLatestRelease(AppConfig.GITHUB_OWNER, AppConfig.GITHUB_REPO)
            val tag = json.get("tag_name")?.asString ?: return null
            val cleanLatest = tag.removePrefix("v").trim()
            val cleanCurrent = AppConfig.CURRENT_VERSION.removePrefix("v").trim()

            if (isNewerVersion(cleanLatest, cleanCurrent)) {
                val assets = json.getAsJsonArray("assets")
                var apkUrl = json.get("html_url")?.asString ?: ""
                if (assets != null) {
                    for (asset in assets) {
                        val obj = asset.asJsonObject
                        val name = obj.get("name")?.asString ?: ""
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkUrl = obj.get("browser_download_url")?.asString ?: apkUrl
                            break
                        }
                    }
                }
                val notes = json.get("body")?.takeIf { !it.isJsonNull }?.asString ?: "A new update is available."
                GitHubUpdateInfo(versionTag = tag, releaseNotes = notes, downloadUrl = apkUrl)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}
