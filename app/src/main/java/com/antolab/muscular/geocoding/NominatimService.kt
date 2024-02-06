package com.antolab.muscular.geocoding

import retrofit2.*
import retrofit2.http.*
import org.osmdroid.util.GeoPoint

interface NominatimService {
    @GET("search")
    fun geocode(
        @retrofit2.http.Query("q")query: String,
        @retrofit2.http.Query("format")format: String = "json"
    ): Call<List<GeocodingResult>>

    @GET("reverse")
    fun geocodeFromCoords(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("format") format: String = "json"
    ): Call<GeocodingResult>

}