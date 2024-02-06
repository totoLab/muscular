package com.antolab.muscular.geocoding

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NominatimClient {
    private const val URL = "https://nominatim.openstreetmap.org/"

    fun create() : NominatimService {
        val retrofit = Retrofit.Builder().baseUrl(URL).addConverterFactory(GsonConverterFactory.create()).build()
        return retrofit.create(NominatimService::class.java)
    }
}