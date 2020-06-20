package hpsaturn.pollutionreporter.dashboard.data.services

import hpsaturn.pollutionreporter.dashboard.data.models.AqicnFeedResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface AqicnApiFeedService {
    @GET("feed/geo:{lat};{long}/")
    suspend fun getGeolocationFeed(@Path("lat") latitude: Double, @Path("long") longitude: Double): Response<AqicnFeedResponse>
}