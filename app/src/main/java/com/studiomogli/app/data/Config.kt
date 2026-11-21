package com.studiomogli.app.data

import com.studiomogli.app.BuildConfig

object AppConfig {
    // Official AniList GraphQL endpoint for metadata (posters, banners, synopses)
    const val ANILIST_URL = "https://graphql.anilist.co/"

    // Multi-provider streaming endpoints (replace with your own self-hosted instances if public mirrors rotate)
    var aniwatchBaseUrl = "https://aniwatch-api-net.vercel.app/"
    var consumetBaseUrl = "https://api.consumet.org/"
    var anifyBaseUrl = "https://anify.eltik.cc/"

    // Studio Mogli links
    const val STUDIO_MOGLI_URL = "https://studiomogli.com"
    const val REFER_AND_EARN_URL = "https://studiomogli.com/refer-and-earn/"

    // GitHub Release Auto-Updater settings
    const val GITHUB_OWNER = BuildConfig.GITHUB_OWNER
    const val GITHUB_REPO = BuildConfig.GITHUB_REPO
    const val CURRENT_VERSION = BuildConfig.VERSION_NAME
}
