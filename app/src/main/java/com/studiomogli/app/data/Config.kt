package com.studiomogli.app.data

import com.studiomogli.app.BuildConfig

object AppConfig {
    // Official AniList GraphQL endpoint for metadata (posters, banners, synopses)
    const val ANILIST_URL = "https://graphql.anilist.co/"

    // =========================================================================
    // STREAMING API MIRRORS (PASTE NEW OPEN-SOURCE OR SELF-HOSTED URLs HERE)
    // =========================================================================
    // Primary: Open-source Anime-API (HiAnime / AniWatch scraper) mirrors
    val animeApiMirrors = listOf(
        "https://api-anime-rouge.vercel.app",
        "https://aninescraper.vercel.app"
    )

    // Secondary fallbacks (Consumet & Anify)
    var consumetBaseUrl = "https://api.consumet.org"
    var anifyBaseUrl = "https://anify.eltik.cc"

    // Studio Mogli links
    const val STUDIO_MOGLI_URL = "https://studiomogli.com"
    const val REFER_AND_EARN_URL = "https://studiomogli.com/refer-and-earn/"

    // GitHub Release Auto-Updater settings (studio-mogli/studio-mogli-anime)
    const val GITHUB_OWNER = BuildConfig.GITHUB_OWNER
    const val GITHUB_REPO = BuildConfig.GITHUB_REPO
    const val CURRENT_VERSION = BuildConfig.VERSION_NAME
}
