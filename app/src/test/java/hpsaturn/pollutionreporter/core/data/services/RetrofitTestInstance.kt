package hpsaturn.pollutionreporter.core.data.services

import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun getRetrofitTestInstance(mockWebServer: MockWebServer): Retrofit = Retrofit.Builder()
        .baseUrl(mockWebServer.url("/"))
        .addConverterFactory(GsonConverterFactory.create())
        .build()