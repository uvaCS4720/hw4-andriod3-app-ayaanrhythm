package edu.nd.pmcburne.hello

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface UvaApiService {

    @GET("placemarks.json")
    suspend fun getLocations(): List<GroundsPlacemark>

    companion object {
        private const val UVA_API_URL = "https://www.cs.virginia.edu/~wxt4gm/"

        fun buildService(): UvaApiService {
            return Retrofit.Builder()
                .baseUrl(UVA_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(UvaApiService::class.java)
        }
    }
}