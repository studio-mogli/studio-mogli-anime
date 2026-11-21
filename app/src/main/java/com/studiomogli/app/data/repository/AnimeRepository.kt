package com.studiomogli.app.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.studiomogli.app.data.AppConfig
import com.studiomogli.app.data.api.AniListApi
import com.studiomogli.app.data.api.AniListQueries
import com.studiomogli.app.data.api.GitHubUpdateApi
import com.studiomogli.app.data.api.GraphQLRequest
import com.studiomogli.app.data.model.AnimeItem
import com.studiomogli.app.data.model.EpisodeItem
import com.studiomogli.app.data.model.GitHubUpdateInfo
import com.studiomogli.app.data.model.StreamResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class AnimeRepository {

    private val gson = Gson()

    private val userAgent =
        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"

    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val aniListApi: AniListApi = Retrofit.Builder()
        .baseUrl(AppConfig.ANILIST_URL)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AniListApi::class.java)

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

    private fun httpGet(url: String, referer: String? = null, isAjax: Boolean = false): String? {
        val builder = Request.Builder()
            .url(url)
            .header("User-Agent", userAgent)
            .header("Accept-Language", "en-US,en;q=0.9")

        if (referer != null) {
            builder.header("Referer", referer)
        }
        if (isAjax) {
            builder.header("X-Requested-With", "XMLHttpRequest")
            builder.header("Accept", "application/json, text/javascript, */*; q=0.01")
        } else {
            builder.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
        }

        return okHttp.newCall(builder.build()).execute().use { resp ->
            if (resp.isSuccessful) resp.body?.string() else null
        }
    }

    /**
     * Direct on-device scraper for zanix.tr (runs from the user's phone IP, bypassing datacenter 403s).
     */
    suspend fun fetchEpisodesMultiProvider(anime: AnimeItem): List<EpisodeItem> = withContext(Dispatchers.IO) {
        val base = AppConfig.DIRECT_SCRAPE_BASE_URL.trimEnd('/')
        val queries = listOf(anime.title, anime.romajiTitle).distinct()

        for (q in queries) {
            try {
                val encoded = URLEncoder.encode(q, "UTF-8")
                val searchHtml = httpGet("$base/search?keyword=$encoded", referer = "$base/") ?: continue
                val searchDoc = Jsoup.parse(searchHtml)

                // Find first matching anime link (e.g., /watch/naruto-677 or /naruto-677)
                val firstLink = searchDoc.select(".film_list-wrap .flw-item .film-name a, .flw-item a.film-poster-ahref")
                    .firstOrNull()
                    ?.attr("href")
                    ?: continue

                // Extract numeric ID from end of slug (e.g., "naruto-677" -> "677")
                val slug = firstLink.substringBefore("?").trimEnd('/')
                val numericId = slug.substringAfterLast("-")
                if (numericId.all { it.isDigit() } && numericId.isNotEmpty()) {
                    val epJsonStr = httpGet(
                        "$base/ajax/v2/episode/list/$numericId",
                        referer = "$base$firstLink",
                        isAjax = true
                    ) ?: continue

                    val epJson = gson.fromJson(epJsonStr, JsonObject::class.java)
                    val epHtml = epJson.get("html")?.takeIf { !it.isJsonNull }?.asString ?: continue
                    val epDoc = Jsoup.parse(epHtml)
                    val epElements = epDoc.select("a.ssl-item.ep-item")

                    if (epElements.isNotEmpty()) {
                        return@withContext epElements.mapIndexed { index, el ->
                            val dataId = el.attr("data-id")
                            val num = el.attr("data-number").toIntOrNull() ?: (index + 1)
                            val title = el.attr("title").ifBlank { "Episode $num" }
                            EpisodeItem(
                                id = dataId,
                                number = num,
                                title = title,
                                provider = "DirectZanix"
                            )
                        }
                    }
                }
            } catch (_: Exception) { }
        }

        // Fallback sample list only if network/site is unreachable
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
     * Fetches the episode server & embed/HLS stream directly from zanix.tr on-device.
     */
    suspend fun fetchStreamUrl(episode: EpisodeItem, category: String = "sub"): StreamResult = withContext(Dispatchers.IO) {
        if (episode.provider == "DirectZanix") {
            val base = AppConfig.DIRECT_SCRAPE_BASE_URL.trimEnd('/')
            try {
                // 1. Get available servers for this episode
                val serversJsonStr = httpGet(
                    "$base/ajax/v2/episode/servers?episodeId=${episode.id}",
                    referer = "$base/",
                    isAjax = true
                )
                if (serversJsonStr != null) {
                    val serversJson = gson.fromJson(serversJsonStr, JsonObject::class.java)
                    val serversHtml = serversJson.get("html")?.takeIf { !it.isJsonNull }?.asString
                    if (serversHtml != null) {
                        val doc = Jsoup.parse(serversHtml)
                        // Prefer requested category ("sub" or "dub"), fallback to any available server
                        val targetBlock = doc.select(".servers-$category .server-item").ifEmpty {
                            doc.select(".server-item")
                        }
                        val serverId = targetBlock.firstOrNull()?.attr("data-id")

                        if (!serverId.isNullOrBlank()) {
                            // 2. Get source link for the chosen server
                            val sourceJsonStr = httpGet(
                                "$base/ajax/v2/episode/sources?id=$serverId",
                                referer = "$base/",
                                isAjax = true
                            )
                            if (sourceJsonStr != null) {
                                val sourceJson = gson.fromJson(sourceJsonStr, JsonObject::class.java)
                                val link = sourceJson.get("link")?.takeIf { !it.isJsonNull }?.asString
                                if (!link.isNullOrBlank()) {
                                    val isDirectM3u8 = link.contains(".m3u8", ignoreCase = true)
                                    return@withContext StreamResult(
                                        hlsUrl = link,
                                        referer = "$base/",
                                        providerUsed = "DirectZanix",
                                        isEmbed = !isDirectM3u8
                                    )
                                }
                            }
                        }
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
