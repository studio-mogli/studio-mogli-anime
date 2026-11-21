package com.studiomogli.app.data

import com.studiomogli.app.BuildConfig

object AppConfig {
    // Official AniList GraphQL endpoint for metadata (posters, banners, synopses)
    const val ANILIST_URL = "https://graphql.anilist.co/"

    // =========================================================================
    // DIRECT ON-DEVICE SCRAPER TARGET (Runs from User's Phone IP - No 403!)
    // =========================================================================
    const val DIRECT_SCRAPE_BASE_URL = "https://zanix.tr"

    // Optional fallback backend URL (Render/Vercel)
    const val HIANIME_API_BASE_URL = "https://hianime-api-y1pr.onrender.com"

    // Studio Mogli links
    const val STUDIO_MOGLI_URL = "https://studiomogli.com"
    const val REFER_AND_EARN_URL = "https://studiomogli.com/refer-and-earn/"

    // GitHub Release Auto-Updater settings (studio-mogli/studio-mogli-anime)
    const val GITHUB_OWNER = BuildConfig.GITHUB_OWNER
    const val GITHUB_REPO = BuildConfig.GITHUB_REPO
    const val CURRENT_VERSION = BuildConfig.VERSION_NAME
}
