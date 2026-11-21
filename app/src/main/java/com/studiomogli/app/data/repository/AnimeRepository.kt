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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class AnimeRepository {

    // 45s timeouts so Render's free tier has time to wake up from sleep on the first request
    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .followRedirects(true)
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
     * Fetches episodes from your live Render Consumet API (/anime/gogoanime).
     */
    suspend fun fetchEpisodesMultiProvider(anime: AnimeItem): List<EpisodeItem> = withContext(Dispatchers.IO) {
        val base = AppConfig.CONSUMET_BASE_URL.trimEnd('/')
        val queries = listOf(anime.title, anime.romajiTitle).distinct()

        for (q in queries) {
            try {
                // Encode spaces as %20 for URL path segment
                val encoded = URLEncoder.encode(q, "UTF-8").replace("+", "%20")
                val searchJson = streamApi.getJson("$base/anime/gogoanime/$encoded").asJsonObject
                val results = searchJson.getAsJsonArray("results")

                if (results != null && results.size() > 0) {
                    // Pick the first matching anime ID
                    val animeId = results[0].asJsonObject.get("id")?.asString ?: continue
                    val infoJson = streamApi.getJson("$base/anime/gogoanime/info/$animeId").asJsonObject
                    val episodes = infoJson.getAsJsonArray("episodes")

                    if (episodes != null && episodes.size() > 0) {
                        return@withContext episodes.mapIndexed { index, el ->
                            val ep = el.asJsonObject
                            val epId = ep.get("id")?.asString ?: ""
                            val epNum = ep.get("number")?.takeIf { !it.isJsonNull }?.asInt ?: (index + 1)
                            EpisodeItem(
                                id = epId,
                                number = epNum,
                                title = "Episode $epNum",
                                provider = "Consumet"
                            )
                        }
                    }
                }
            } catch (_: Exception) { }
        }

        // Fallback sample list only if Render is unreachable or anime not found
        val count = (anime.episodesCount ?: 12).coerceAtMost(24)
        (1..count).map { num ->
            EpisodeItem(
                id = "demo-${anime.id}-$num",
                number = num,
                title = "Episode $num",
                provider = "Demo"
            )
        }
    }

    /**
     * Resolves the direct HLS (.m3u8) video stream from your live Render Consumet API.
     */
    suspend fun fetchStreamUrl(episode: EpisodeItem, category: String = "sub"): StreamResult = withContext(Dispatchers.IO) {
        if (episode.provider == "Consumet") {
            val base = AppConfig.CONSUMET_BASE_URL.trimEnd('/')
            try {
                val json = streamApi.getJson("$base/anime/gogoanime/watch/${episode.id}").asJsonObject
                val sources = json.getAsJsonArray("sources")
                val referer = json.getAsJsonObject("headers")
                    ?.get("Referer")
                    ?.takeIf { !it.isJsonNull }
                    ?.asString

                if (sources != null && sources.size() > 0) {
                    // Prefer 1080p / 720p / default quality .m3u8 source
                    var bestUrl: String? = null
                    val preferredQualities = listOf("1080p", "720p", "default", "backup")
                    for (quality in preferredQualities) {
                        val match = sources.firstOrNull { el ->
                            el.asJsonObject.get("quality")?.asString.equals(quality, ignoreCase = true)
                        }
                        if (match != null) {
                            bestUrl = match.asJsonObject.get("url")?.asString
                            break
                        }
                    }
                    if (bestUrl.isNullOrBlank()) {
                        bestUrl = sources[0].asJsonObject.get("url")?.asString
                    }

                    if (!bestUrl.isNullOrBlank()) {
                        return@withContext StreamResult(
                            hlsUrl = bestUrl,
                            referer = referer,
                            providerUsed = "Consumet (GogoAnime)",
                            isEmbed = false
                        )
                    }
                }
            } catch (_: Exception) { }
        }

        StreamResult(
            hlsUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            providerUsed = "Fallback HLS Stream",
            isEmbed = false
        )
    }

    /**
     * Checks GitHub Releases (studio-mogli/studio-mogli-anime) for a newer version.
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
