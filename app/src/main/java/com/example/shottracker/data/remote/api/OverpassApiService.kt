package com.example.shottracker.data.remote.api

import com.example.shottracker.data.remote.dto.OverpassResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Url

interface OverpassApiService {

    @FormUrlEncoded
    @POST("interpreter")
    suspend fun query(@Field("data") data: String): OverpassResponse

    /** Query a specific interpreter endpoint (used to rotate across mirrors on failure). */
    @FormUrlEncoded
    @POST
    suspend fun queryUrl(@Url url: String, @Field("data") data: String): OverpassResponse

    companion object {
        const val BASE_URL = "https://overpass-api.de/api/"

        /**
         * Interpreter endpoints tried in order. The main server 504s under load on the first
         * cold request, so we fall back to mirrors that are usually less busy.
         */
        val ENDPOINTS = listOf(
            "https://overpass-api.de/api/interpreter",
            "https://overpass.kumi.systems/api/interpreter",
            "https://overpass.private.coffee/api/interpreter"
        )
    }
}
