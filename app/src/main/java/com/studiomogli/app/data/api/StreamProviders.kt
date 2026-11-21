package com.studiomogli.app.data.api

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface DynamicStreamApi {
    @GET
    suspend fun getJson(@Url fullUrl: String): JsonElement
}
