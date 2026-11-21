package com.studiomogli.app.data.api

import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.POST

data class GraphQLRequest(
    val query: String,
    val variables: Map<String, Any> = emptyMap()
)

interface AniListApi {
    @POST(".")
    suspend fun query(@Body request: GraphQLRequest): JsonObject
}

object AniListQueries {
    const val MEDIA_LIST = """
    query (${"$"}sort: [MediaSort], ${"$"}perPage: Int) {
      Page(page: 1, perPage: ${"$"}perPage) {
        media(type: ANIME, sort: ${"$"}sort, isAdult: false) {
          id
          title { english romaji }
          bannerImage
          coverImage { extraLarge large }
          description(asHtml: false)
          averageScore
          episodes
          genres
        }
      }
    }
    """

    const val SEARCH = """
    query (${"$"}search: String) {
      Page(page: 1, perPage: 20) {
        media(type: ANIME, search: ${"$"}search, isAdult: false) {
          id
          title { english romaji }
          bannerImage
          coverImage { extraLarge large }
          description(asHtml: false)
          averageScore
          episodes
          genres
        }
      }
    }
    """
}
