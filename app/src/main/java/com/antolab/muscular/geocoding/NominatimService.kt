package com.antolab.muscular.geocoding

import retrofit2.*
import retrofit2.http.*

interface NominatimService {
    @GET("search")
    fun geocode(
        @Query("q")query: String,
        @Query("format")format: String = "json"
    ): Call<List<GeocodingResult>>
}