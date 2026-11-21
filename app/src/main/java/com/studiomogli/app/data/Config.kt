package com.studiomogli.app.data

import com.studiomogli.app.BuildConfig

object AppConfig {
    // Official AniList GraphQL endpoint for metadata (posters, banners, synopses)
    const val ANILIST_URL = "https://graphql.anilist.co/"

    // =========================================================================
    // YOUR LIVE RENDER CONSUMET API (Working /anime/gogoanime provider)
    // =========================================================================
    const val CONSUMET_BASE_URL = "https://api-consumet-org-5kz6.onrender.com"

    // Studio Mogli links
    const val STUDIO_MOGLI_URL = "https://studiomogli.com"
    const val REFER_AND_EARN_URL = "https://studiomogli.com/refer-and-earn/"

    // GitHub Release Auto-Updater settings (studio-mogli/studio-mogli-anime)
    const val GITHUB_OWNER = BuildConfig.GITHUB_OWNER
    const val GITHUB_REPO = BuildConfig.GITHUB_REPO
    const val CURRENT_VERSION = BuildConfig.VERSION_NAME
}
